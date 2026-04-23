# 도서 추천 백엔드 서비스 (bkrc-v4)

대규모 트래픽을 고려한 도서 추천 서비스입니다.  
JWT 기반 인증/인가, Redis 캐싱, Outbox 패턴 기반 RabbitMQ 이벤트 처리, 
ELK 스택 기반 로그 수집, Prometheus/Grafana 모니터링까지 직접 설계하고 구현하였습니다.

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Build Tool | Gradle |
| Database | MySQL |
| Cache / 순위 집계 | Redis (String, Sorted Set) |
| Message Queue | RabbitMQ |
| Security | Spring Security, JWT |
| Persistence | Spring Data JPA, Hibernate |
| Observability | Prometheus, Grafana, ELK Stack (Elasticsearch + Logstash + Kibana) |
| Resilience | Resilience4j (RateLimiter) |
| 외부 API | 알라딘 Open API, OpenAI GPT API |

---

## 핵심 성과

- **Redis 캐시 도입**: k6 부하 테스트(VU 100) 기준 평균 응답시간 **29.9ms → 8.6ms**로 개선
- **Outbox 패턴**: `@Transactional` 내 Outbox 저장 후 `AFTER_COMMIT` 훅에서 RabbitMQ 발행 — DB 트랜잭션과 메시지 발행의 원자성 보장, 이벤트 유실 방지
- **Exchange 책임 분리**: 알림용 `notificationExchange`와 인기 도서용 `hotbookExchange`를 분리해 도메인별 메시지 라우팅 독립성 확보
- **실시간 인기 도서**: 좋아요 이벤트를 RabbitMQ로 비동기 전달하고 Redis Sorted Set에 점수 기반으로 적재. 상위 10개 유지 및 TTL 10일 관리
- **이벤트 드리븐 설계**: `EventType` + `EventHandler` 패턴으로 신규 이벤트 추가 시 기존 코드 수정 없이 구현체만 추가하는 OCP 기반 확장 구조

---

## 시스템 플로우

### 1. 회원·도서 API 플로우

```
클라이언트
    │
    ▼
JWT 인증 필터 (JwtAuthorizationFilter)
    ├── 비인증 허용 ──▶ 회원 API (POST/PUT /v1/member)
    │                        │
    │                        ▼
    │                   Outbox 저장 (@Transactional, MySQL)
    │                        │
    │                        ▼  [AFTER_COMMIT]
    │                   OutboxProducer
    │                        │
    │                        ▼
    │               notificationExchange (Direct Exchange)
    │               ├── routing key: member.join   ──▶ joinQueue
    │               └── routing key: member.modify ──▶ modifyQueue
    │                        │
    │                        ▼
    │                EmailConsumer → EmailService (이메일 발송)
    │
    ├── JWT 유효 ────▶ 도서 API (GET /v1/aladin/**)
    │                        │
    │                        ▼
    │                Redis 캐시 확인 (TTL 24h)
    │                캐시 HIT  ──▶ 응답 반환 (cacheHitCounter++)
    │                캐시 MISS ──▶ MySQL 조회 → Redis 갱신 → 응답
    │                             (cacheMissCounter++)
    └── JWT 없음 ───▶ 401 Unauthorized
```

### 2. 좋아요 → 인기 도서 플로우

```
POST /v1/like/{itemId}
    │
    ▼
LikeService.like()  [@Transactional]
  ├── Like 저장 (MySQL)
  ├── LikeCount 증가 (MySQL)
  └── Outbox 저장 (routing key: hotbook.like, MySQL)
    │
    ▼  [AFTER_COMMIT]
OutboxProducer.handleOutboxEvent()
    │
    ▼
hotbookExchange (Direct Exchange)
  └── routing key: hotbook.like ──▶ likeQueue
                                         │
                                         ▼
                                   HotBookConsumer
                                         │
                                         ▼
                                   Redis Sorted Set 갱신
                                   score = 좋아요 수 × 3
                                   상위 10개 유지, TTL 10일

※ likeQueue 소비 3회 실패 시:
   deadLetterExchange ──▶ deadLetterQueue (DLQ)

GET /v1/hot-books/books/date/{dateStr}
    │
    ▼
Redis ZREVRANGE ──▶ 상위 10개 bookId 조회 ──▶ 도서 상세 조회 후 응답
```

### 3. RabbitMQ 토폴로지

```
notificationExchange (Direct Exchange)
  ├── routing key: member.join   ──▶ joinQueue
  └── routing key: member.modify ──▶ modifyQueue

hotbookExchange (Direct Exchange)
  └── routing key: hotbook.like  ──▶ likeQueue

※ joinQueue / modifyQueue / likeQueue 모두 DLX 설정:
   소비 3회 실패 → deadLetterExchange ──▶ deadLetterQueue
```

---

## 주요 기능

### 회원 관리 (`member`)
- `POST /v1/member` 회원가입 — 중복 아이디 체크, BCrypt 암호화
- `PUT /v1/member/:loginId` 회원정보 수정
- Spring Security + JWT 기반 로그인/인가
- `AuthenticationFilter` — 로그인 성공 시 JWT 발급 (응답 헤더 `token`)
- `JwtAuthorizationFilter` — 요청마다 JWT 검증

### 도서/추천 관리 (`aladin`)
- `GET /v1/aladin/books` 전체 도서 목록 조회 — Redis 캐시(TTL 24h) 적용, 캐시 MISS 시 DB 폴백
- `GET /v1/aladin/books/recommend/user` 사용자별 개인화 추천 — 조회 히스토리 기반 필터링 + GPT 코멘트 생성
- Resilience4j `RateLimiter` — 알라딘 외부 API 호출 제한 (30초당 1,000건), Fallback 처리

### 좋아요 & 실시간 인기 도서 (`like`, `hotbook`)
- `POST /v1/like/{itemId}` 좋아요 — 중복 방지, LikeCount 집계, Outbox 이벤트 발행
- `GET /v1/hot-books/books/date/{dateStr}` 날짜별 인기 도서 조회
- `hotbookExchange` → `likeQueue`(routing key: `hotbook.like`) → `HotBookConsumer` → Redis Sorted Set 갱신
- 점수: 좋아요 수 × 3, 상위 10개 유지, TTL 10일

### 알림 시스템 (`email`, `outbox`)
- `@Transactional` 내 Outbox 저장 → `AFTER_COMMIT` 이후 RabbitMQ 발행으로 원자성 보장
- `notificationExchange` → `joinQueue`(routing key: `member.join`) / `modifyQueue`(routing key: `member.modify`)
- Dead Letter Queue(DLQ) 구성 — 소비 3회 실패 시 `deadLetterExchange` → `deadLetterQueue`로 자동 이동
- `EmailConsumer` — `joinQueue` / `modifyQueue` 구독, 회원가입·정보수정 이메일 발송
- 현재 지원 이벤트: `MEMBER_JOIN`, `MEMBER_MODIFY`, `BOOK_LIKE`

### 배치 (`batch`)
- 매일 11:10(KST) 알라딘 API에서 신규 도서 수집 및 DB/Redis 저장
- ShedLock(Redis 기반)으로 다중 인스턴스 환경에서 중복 실행 방지

### 운영/관측성
- **Prometheus + Grafana** — JVM, API 응답시간, HikariCP, 커스텀 캐시 HIT/MISS 메트릭 수집·시각화
- **ELK Stack** — Logstash(logstash-logback-encoder) 기반 구조화 로그 수집, Kibana 조회
- **HikariCP** 커넥션 풀 세밀 튜닝 (max 16, idle 5, timeout 3s)

---