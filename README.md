# 도서 추천 백엔드 서비스

대규모 트래픽을 고려한 도서 추천 서비스입니다.  
JWT 기반 인증/인가, Redis 캐시, Outbox 패턴 기반 Kafka 이벤트 처리까지 직접 설계하고 구현한 **실무형 백엔드 포트폴리오 프로젝트**입니다.

---

## 프로젝트 개요

- **목표**: 사용자가 본 책을 제외하고 도서를 추천하는 서비스
- **역할**: 개인 프로젝트 — 요구사항 정의 → 아키텍처 설계 → 개발 → 부하 테스트/운영까지 전 과정 100% 직접 진행

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Build Tool | Gradle |
| Database | MySQL |
| Cache / 순위 집계 | Redis (String, Sorted Set) |
| Message Queue | Kafka |
| Security | Spring Security, JWT (jjwt 0.12.3) |
| Persistence | Spring Data JPA, Hibernate |
| Scheduler | Spring Scheduling |
| Observability | Prometheus, Grafana, ELK Stack (Elasticsearch + Logstash + Kibana) |
| Resilience | Resilience4j (RateLimiter) |

---

## 핵심 성과

- **Redis 캐시 도입**: k6 부하 테스트(VU 100) 기준 평균 응답시간 **29.9ms → 8.6ms**
- **Outbox 패턴**: `@Transactional` 내 Outbox 저장으로 DB 트랜잭션과 Kafka 발행 원자성 보장, 이벤트 유실 방지
- **실시간 인기 도서**: 좋아요 이벤트를 Kafka로 비동기 전달하고 Redis Sorted Set에 점수 기반으로 적재. Redis Pipeline으로 `ZADD` · `ZREMRANGE` · `EXPIRE`를 단일 네트워크 왕복으로 처리해 인기 순위 갱신 비용을 최소화, 상위 10개 유지 및 TTL 관리
- **이벤트 드리븐 설계**: `EventType` + `EventHandler` 패턴으로 신규 이벤트 추가 시 기존 코드 수정 없이 구현체만 추가하는 OCP 기반 확장 구조
- **운영 안정성**: ShedLock으로 분산 환경에서 배치 스케줄러 중복 실행 방지

---

## 주요 기능

### 회원 관리 (`member`)
- `POST /v1/member` 회원가입 — 중복 아이디 체크, 비밀번호 확인, BCrypt 암호화
- `PUT /v1/member/:loginId` 회원정보 수정
- Spring Security + JWT 기반 로그인/인가
- `AuthenticationFilter` — 로그인 성공 시 JWT 발급
- `JwtAuthorizationFilter` — 요청마다 JWT 검증, `/v1/member`는 비인증 허용

### 도서/추천 관리 (`aladin`)
- `GET /v1/aladin/books` 전체 도서 목록 조회 — Redis 캐시(TTL 24h) 적용, 캐시 MISS 시 DB 폴백
- `GET /v1/aladin/books/recommend/user` 사용자별 개인화 추천 — 히스토리 기반 필터링

### 좋아요 & 실시간 인기 도서 (`like`, `hotbook`)
- `POST /v1/like/{itemId}` 좋아요 — 중복 방지, LikeCount 집계, Outbox 이벤트 발행
- `GET /v1/hot-books/books/date/{dateStr}` 날짜별 인기 도서 조회
- Kafka `book_like` 토픽 → `HotBookConsumer` → Redis Sorted Set 갱신
- 점수 계산: `좋아요 수 × 3`, 상위 10개 유지, TTL 10일

### 알람 시스템 (`outbox`)
- `@Transactional` 내 Outbox 저장 → `AFTER_COMMIT` 이후 Kafka 발행으로 원자성 보장
- `EventType` enum + `EventHandler` 인터페이스 기반 이벤트 처리 — OCP 적용
- 발행 실패 시 FAILED 상태로 별도 관리, 재처리 지원
- Consumer 수동 ACK (`MANUAL`) + DLT(Dead Letter Topic)로 메시지 유실 방지
- 현재 지원 이벤트: `MEMBER_JOIN`, `MEMBER_MODIFY`, `BOOK_LIKE`

### 운영/안정성
- `Resilience4j RateLimiter` — 알라딘 외부 API 호출 제한, Fallback 처리
- Prometheus + Grafana — JVM, API 응답시간, HikariCP 메트릭 수집 및 시각화
- ELK Stack — Logstash 기반 구조화 로그 수집 및 Kibana 조회

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
    │                   Outbox 저장 (@Transactional)
    │                        │
    │                        ▼
    │                   Kafka 발행 (AFTER_COMMIT)
    │                        │
    │                        ▼
    │                   알람 처리 (MemberJoin/Modify Handler)
    │
    ├── JWT 유효 ────▶ 도서 API (GET /v1/aladin/**)
    │                        │
    │                        ▼
    │                   Redis 캐시 확인 (TTL 24h)
    │                   캐시 HIT ──▶ 응답 반환
    │                   캐시 MISS ──▶ MySQL 조회 → Redis 갱신 → 응답 반환
    │
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
  └── Outbox 저장 (MySQL)
    │
    ▼  [AFTER_COMMIT]
OutboxProducer
  ├── 발행 성공 ──▶ Kafka topic: book_like ──▶ HotBookConsumer
  │                                                    │
  │                                                    ▼
  │                                          Redis Sorted Set 갱신
  │                                          (ZADD + ZREMRANGE + EXPIRE)
  │                                          상위 10개 유지, TTL 10일
  │
  └── 발행 실패 ──▶ FAILED 상태 저장 (OutboxStatusUpdater, REQUIRES_NEW)
                    재처리 대기

GET /v1/hot-books/books/date/{dateStr}
    │
    ▼
Redis ZREVRANGE ──▶ 상위 10개 bookId 조회 ──▶ 도서 상세 조회 후 응답
```

---

## 패키지 구조

```
src/main/java/com/bkrc/bkrcv3/
├── adapter/          # 공통 예외 핸들러
├── ai/               # AI 클라이언트 (도서 코멘트 생성)
├── aladin/           # 도서/추천 도메인
│   ├── application/  # Controller, Service, Repository, DTO
│   ├── client/       # 알라딘 외부 API 클라이언트
│   └── entity/       # 도메인 엔티티
├── batch/            # 배치 스케줄러
├── common/           # 공통 이벤트, 유틸
├── config/           # Async, ShedLock 설정
├── exception/        # 공통 예외 클래스
├── gpt/              # GPT 클라이언트
├── history/          # 도서 조회 히스토리
├── hotbook/          # 실시간 인기 도서
├── like/             # 좋아요
├── member/           # 회원, 인증/인가
└── outbox/           # Outbox 패턴, 이벤트 핸들러
```