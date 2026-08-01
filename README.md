# 📚 BKRC (Book Recommendation Service) v4

알라딘 API 기반 도서 추천 서비스입니다. 
## 🎯목료
- 기획, 설계, 개발, 운영 전 과정을 혼자 진행하면서 실무 경험을 확장하고 기술 이해도를 높이기 위함.

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