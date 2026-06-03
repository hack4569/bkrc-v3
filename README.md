# 📚 BKRC (Book Recommendation Service) v4

알라딘 API 기반 도서 추천 서비스입니다. 
이벤트 기반 아키텍처와 다양한 인프라 기술을 활용하여 안정적인 알림 발송, 인기책, GPT 기반 도서 추천 기능을 제공합니다.

---

## 🛠 Tech Stack

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5, Spring Batch, Spring Security |
| Database | MySQL 8, Redis 7 |
| Message Broker | RabbitMQ 4.0 |
| ID 생성 | Snowflake ID |
| 서킷브레이커 | Resilience4j |
| 인증 | JWT |
| 외부 API | 알라딘 Open API, OpenAI GPT API |
| CI/CD | Jenkins, Docker, Docker Compose |
| API 문서 | SpringDoc OpenAPI (Swagger) |

---

## 🚀 주요 기능

### 1. 도서 추천 (알라딘 API)
- 알라딘 Open API를 통해 사용자에게 보여줄 도서 정보를 조회하고 가공 후 DB/Cache에 저장
- Resilience4j RateLimiter로 알라딘 API 호출 보호
- Redis 캐시로 응답 속도 최적화

### 2. Hot Book 랭킹
- 도서 좋아요 이벤트를 RabbitMQ를 통해 비동기 처리
- Redis Sorted Set으로 실시간 인기 도서 Top 10 계산

### 3. 이메일 알림
- 회원가입 / 정보 수정 시 이메일 알림 발송
- Outbox 테이블로 메시지 유실 방지 보장
- DLQ + 재시도 정책 (3회, 1초 간격)으로 장애 복원력 확보

### 4. Spring Batch
- 알라딘 API에서 도서 데이터를 배치로 수집 및 DB 적재
- Redis 캐시 갱신 Tasklet 포함

```