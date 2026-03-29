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

    Client --> JWTFilter{JWT 인증 필터\nJwtAuthorizationFilter}

    JWTFilter -->|POST /v1/member\n회원가입/수정 허용| MemberAPI
    JWTFilter -->|JWT 유효| AladinController
    JWTFilter -->|JWT 없음 / 만료| Error401[401 Unauthorized]

    subgraph MemberAPI[회원 API - UserController]
        Register[POST /v1/member\n회원가입]
        Modify[PUT /v1/member/:loginId\n회원정보 수정]
    end

    Register -->|@Transactional| OutboxJoin[(NotificationOutbox\nMEMBER_JOIN PENDING 저장)]
    Modify -->|@Transactional| OutboxModify[(NotificationOutbox\nMEMBER_MODIFY PENDING 저장)]

    subgraph AladinController[AladinController]
        Books[GET /v1/aladin/books\n전체 도서 조회]
        RecommendUser[GET /v1/aladin/books/recommend/user\n사용자 맞춤 추천]
    end

    Books -->|캐시 HIT| Redis[(Redis TTL 24h)]
    Books -->|캐시 MISS| MySQL[(MySQL DB)]
    MySQL --> Redis

    RecommendUser --> Redis
    RecommendUser --> HistoryDB[(History DB\n히스토리 필터링)]

subgraph OutboxFlow[Outbox + Kafka 알람 - AFTER_COMMIT]
Producer[NotificationProducer\nAFTER_COMMIT → Kafka 발행]
Consumer[NotificationConsumer\nmember_join / member_modify]
Handler[EventHandler\nMemberJoin / MemberModify]
end

OutboxJoin --> Producer
OutboxModify --> Producer
Producer -->|발행 성공| Consumer
Producer -->|발행 실패| Failed[(FAILED\n5분마다 재처리)]
Failed --> Producer
Consumer --> Handler
Handler --> Alarm[알람 발송]
```

