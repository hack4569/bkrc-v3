# 도서 추천 백엔드 서비스

알라딘 API를 활용해 사용자 맞춤 도서를 추천하는 백엔드 서비스입니다.
JWT 기반 인증/인가, Redis 캐시, Outbox 패턴 기반 Kafka 이벤트 알람까지 직접 설계하고 구현한 **실무형 백엔드 포트폴리오 프로젝트**입니다.

---

## 프로젝트 개요

- **목표**: 사용자가 본 책을 제외하고 개인화된 도서를 추천하는 서비스
- **역할**: 개인 프로젝트 — 요구사항 정의 → 아키텍처 설계 → 개발 → 부하 테스트/운영까지 전 과정 100% 직접 진행

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Build Tool | Gradle |
| Database | MySQL |
| Cache | Redis |
| Message Queue | Kafka |
| Security | Spring Security, JWT (jjwt 0.12.3) |
| Persistence | Spring Data JPA, Hibernate |
| Scheduler | Spring Scheduling, ShedLock 

---

## 핵심 성과

- **Redis 캐시 도입**: k6 부하 테스트(VU 100) 기준 평균 응답시간 **29.9ms → 8.6ms**
- **Outbox 패턴**: `@Transactional` 내 Outbox 저장으로 DB 트랜잭션과 Kafka 발행 원자성 보장, 알람 유실 방지
- **이벤트 드리븐 설계**: `EventType` + `EventHandler` 패턴으로 신규 이벤트 추가 시 기존 코드 수정 없이 구현체만 추가하는 OCP 기반 확장 구조
- **운영 안정성**: ShedLock으로 분산 환경에서 배치 스케줄러 중복 실행 방지

## 주요 기능

### 회원 관리 (`member`)
- `POST /v1/member` 회원가입 — 중복 아이디 체크, 비밀번호 확인, BCrypt 암호화
- `PUT /v1/member/:loginId` 회원정보 수정
- Spring Security + JWT 기반 로그인/인가
- `AuthenticationFilter` — 로그인 성공 시 JWT 발급
- `JwtAuthorizationFilter` — 요청마다 JWT 검증, `/v1/member`는 비인증 허용

### 도서/추천 관리 (`aladin`)
- `GET /v1/aladin/books` 전체 도서 목록 조회 — Redis 캐시(TTL 24h) 적용
- `GET /v1/aladin/books/recommend/user` 사용자별 개인화 추천 — 히스토리 기반 필터링

### 알람 시스템 (`notification`)
- `NotificationOutbox` + `@Transactional`으로 DB 저장과 Kafka 발행 원자성 보장
- `EventType` enum + `EventHandler` 인터페이스 기반 이벤트 처리 — OCP 적용
- `NotificationProducer` 5초마다 PENDING 조회 후 Kafka 발행, 최대 3회 재시도
- `NotificationConsumer` Acknowledgment 기반 수동 커밋으로 메시지 유실 방지
- 현재 지원 이벤트: `MEMBER_JOIN`, `MEMBER_MODIFY`

### 운영/안정성
- `ShedLock` — Redis 기반 분산 배치 락, 다중 서버 환경 중복 실행 방지
- `HikariCP` 커넥션 풀 튜닝 (maxPoolSize: 16, connectionTimeout: 3s)
- `CallerRunsPolicy` — 스레드풀 포화 시 요청 스레드가 직접 처리, 데이터 유실 방지


## 1. 시스템 플로우차트

```mermaid
flowchart TD
    Client([클라이언트])

    %% ── 인증 진입점 ──────────────────────────────
    Client -->|POST /login| LoginFilter

    subgraph Auth[인증 - Spring Security]
        LoginFilter[AuthenticationFilter\n자격증명 파싱]
        LoginFilter --> LoadUser[UserServiceImpl\nloadUserByUsername]
        LoadUser --> BCrypt{BCrypt\n비밀번호 검증}
        BCrypt -->|일치| JwtIssue[JWT 발급\nHS256 · 10일]
        BCrypt -->|불일치| E401A[401 Unauthorized]
        JwtIssue --> JwtResponse[Authorization: Bearer token]
    end

    %% ── JWT 필터 ─────────────────────────────────
    Client -->|이후 요청| JWTFilter{JwtAuthorizationFilter\nJWT 검증}
    JWTFilter -->|유효| Router{엔드포인트 라우팅}
    JWTFilter -->|만료/없음| E401B[401 Unauthorized]

    %% ── 회원 API ──────────────────────────────────
    Router -->|POST /v1/member| Register
    Router -->|PUT /v1/member/:loginId| Modify

    subgraph MemberAPI[회원 관리 - UserController]
        Register[회원가입\n중복ID · 비밀번호 확인 · BCrypt]
        Modify[회원정보 수정\n기존 비밀번호 검증]
    end

    Register -->|@Transactional\nMember 저장 + Outbox PENDING| OutboxJoin[(Outbox\nMEMBER_JOIN)]
    Modify -->|@Transactional\nMember 저장 + Outbox PENDING| OutboxModify[(Outbox\nMEMBER_MODIFY)]

    %% ── 도서 API ──────────────────────────────────
    Router -->|GET /v1/aladin/books| Books
    Router -->|GET /v1/aladin/books/recommend/user| RecommendUser

    subgraph AladinAPI[도서 - AladinController]
        Books[전체 도서 조회]
        RecommendUser[사용자 맞춤 추천]
    end

    Books -->|캐시 HIT| RedisBooks[(Redis\naladin:books:all · TTL 24h)]
    Books -->|캐시 MISS| MySQL[(MySQL\nAladinBook + BookComment)]
    MySQL -->|결과 캐싱| RedisBooks

    RecommendUser --> AladinAPI_External[Aladin 외부 API\nResilience4j Rate Limiter\n1000req/30s]
    RecommendUser --> HistoryDB[(MySQL\nHistory 조회)]
    AladinAPI_External --> Filter[필터링\n① 이미 본 책 제외\n② 허용 카테고리\n③ 출간일 90일 이내]
    HistoryDB --> Filter
    Filter --> RecommendResult[추천 도서 목록 반환]

    %% ── 좋아요 API ────────────────────────────────
    Router -->|POST /v1/like/:itemId| LikeController

    subgraph LikeAPI[좋아요 - LikeController]
        LikeController[좋아요 처리]
        LikeController --> LikeSave[(MySQL\nLike + LikeCount 저장)]
        LikeController --> OutboxLike[(Outbox\nBOOK_LIKE PENDING)]
    end

    %% ── 핫북 API ──────────────────────────────────
    Router -->|GET /v1/hot-articles/articles/date/:date| HotBookAPI

    subgraph HotBookAPI[실시간 랭킹 - HotBookController]
        HotBookController[날짜별 인기 도서 조회\nTop 10]
        HotBookController --> RedisRanking[(Redis Sorted Set\nhot:books:yyyyMMdd · TTL 10일)]
    end

    %% ── Outbox + Kafka 이벤트 흐름 ────────────────
    subgraph OutboxFlow[Outbox + Kafka 이벤트 파이프라인]
        OutboxProducer[OutboxProducer\n@TransactionalEventListener AFTER_COMMIT\nPENDING → Kafka 발행]
        KafkaBroker[[Kafka Broker\nmember_join\nmember_modify\nbook_like]]
        OutboxConsumer[OutboxConsumer\nKafka Listener]
        OutboxService[OutboxService\n이벤트 타입별 라우팅]
        MemberJoinHandler[MemberJoinEventHandler]
        MemberModifyHandler[MemberModifyEventHandler]
        LikeHandler[LikeEventHandler\nRedis 좋아요 수 갱신]
    end

    OutboxJoin --> OutboxProducer
    OutboxModify --> OutboxProducer
    OutboxLike --> OutboxProducer

    OutboxProducer -->|발행 성공 → PUBLISHED| KafkaBroker
    OutboxProducer -->|발행 실패 → FAILED\n최대 3회 재시도| OutboxProducer

    KafkaBroker --> OutboxConsumer
    OutboxConsumer --> OutboxService
    OutboxService --> MemberJoinHandler
    OutboxService --> MemberModifyHandler
    OutboxService --> LikeHandler

    %% ── HotBook 이벤트 처리 ───────────────────────
    KafkaBroker -->|book_like| HotBookConsumer[HotBookConsumer\nKafka Listener]
    HotBookConsumer --> HotBookCalculator[HotBookCalculator\n점수 계산]
    HotBookCalculator -->|파이프라인 업서트| RedisRanking

    LikeHandler --> RedisLike[(Redis\n좋아요 수 · TTL 자정까지)]
```

