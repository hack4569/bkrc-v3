# Chaptersofu 개발 가이드

이 문서는 `bkrc-v3` 저장소의 개발 및 코드 리뷰 기준이다. 사람의 리뷰뿐 아니라 n8n 기반 자동 코드 리뷰 파이프라인이 동일한 기준으로 변경 사항을 평가할 수 있도록 작성했다.

> 이 문서에서 **필수**는 위반 시 수정이 필요한 규칙이고, **권장**은 특별한 이유가 없다면 따라야 하는 규칙이다. 자동 리뷰는 변경된 코드와 그로 인해 직접 영향을 받는 코드만 평가하며, 기존 코드의 문제를 해당 변경의 결함으로 보고하지 않는다.

## 1. 기술 스택

- Java 21
- Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Security
- Spring Data JPA, MySQL 8
- Spring Data Redis, Redis Lua Script
- RabbitMQ, Outbox Pattern
- Spring Batch, ShedLock
- Resilience4j
- JWT
- Spring Boot Actuator, Micrometer, Prometheus, Grafana
- SpringDoc OpenAPI
- JUnit 5, Mockito, ArchUnit, H2, JaCoCo, k6
- Gradle Wrapper

정확한 버전과 의존성은 `build.gradle`을 기준으로 한다. 문서와 빌드 파일이 다르면 빌드 파일이 우선한다.

## 2. 아키텍처

### 2.1 기본 구조

프로젝트는 헥사고날 아키텍처와 도메인 모델 패턴을 따른다.

```text
외부 요청/시스템
    ↓
adapter
    ↓
application
    ↓
domain
```

의존성은 바깥 계층에서 안쪽 계층으로만 향해야 한다.

- `domain`: 핵심 비즈니스 상태와 규칙
- `application`: 유스케이스, 트랜잭션, 포트, 흐름 조율
- `adapter`: HTTP, DB, Redis, RabbitMQ, 외부 API, 보안, 배치 등 기술 구현

`src/test/java/bkrc/chaptersofu/ArchitectureTest.java`가 다음 규칙을 자동 검증한다.

- `domain`은 `application` 또는 `adapter`에 의존하면 안 된다.
- `application`은 `adapter`에 의존하면 안 된다.
- 다른 계층은 `adapter`에 의존하면 안 된다.
- 도메인 슬라이스 사이에 순환 의존이 없어야 한다.
- 애플리케이션 슬라이스 사이에 순환 의존이 없어야 한다.

### 2.2 패키지 구조

기준 루트 패키지는 `bkrc.chaptersofu`이다.

```text
bkrc.chaptersofu
├── domain
│   ├── aladin
│   ├── coupon
│   ├── history
│   ├── like
│   ├── member
│   ├── recommendation
│   ├── common
│   └── exception
├── application
│   ├── <feature>
│   │   ├── provided       # 인바운드 포트 및 유스케이스 요청 모델
│   │   ├── event          # 기능별 이벤트 payload
│   │   ├── *Service       # 유스케이스 구현
│   │   └── *Repository    # 아웃바운드 포트
│   ├── required           # 여러 기능이 공유하는 아웃바운드 포트
│   ├── shared             # 애플리케이션 공통 이벤트 모델
│   └── outbox
└── adapter
    ├── webapi             # REST API와 응답 DTO
    ├── client             # Aladin, OpenAI 등 외부 API
    ├── messaging          # RabbitMQ consumer, publisher, handler
    ├── batch              # Spring Batch
    ├── security           # JWT 인증과 인가
    └── config             # 기술 설정
```

## 3. 계층별 개발 규칙

### 3.1 Domain

필수 규칙:

- 비즈니스 상태 변경은 의미가 드러나는 도메인 메서드로 수행한다. 공개 setter를 추가하지 않는다.
- 엔티티 생성 규칙이 있다면 생성자 호출보다 이름 있는 정적 팩터리 메서드를 사용한다.
- 도메인은 웹 요청/응답 DTO, 컨트롤러, RabbitMQ, Redis 클라이언트, 외부 API 클라이언트를 직접 참조하지 않는다.
- 도메인이 기술 기능을 필요로 하면 `PasswordEncoder`처럼 도메인 언어로 된 인터페이스에 의존한다.
- 핵심 불변식은 컨트롤러가 아니라 도메인 또는 해당 유스케이스 경계에서 검증한다.
- JPA 엔티티 연관관계는 기본적으로 지연 로딩을 고려하고, 컬렉션 및 양방향 관계 추가 시 조회 비용과 직렬화 순환을 검토한다.

권장 규칙:

- 엔티티의 식별자와 생성·수정 시각 공통 동작은 `BaseEntity`와 기존 ID 생성 정책을 재사용한다.
- 값 자체에 검증과 동등성 의미가 있으면 값 객체 또는 `record`로 표현한다.
- 다른 애그리거트의 내부 상태를 직접 변경하지 않고 애플리케이션 서비스에서 유스케이스를 조율한다.

### 3.2 Application

필수 규칙:

- 외부에서 호출하는 유스케이스는 `<feature>/provided`의 인터페이스로 노출한다.
- 컨트롤러와 메시지 소비자는 구체 서비스 클래스 대신 provided 포트에 의존한다.
- DB, 캐시, 외부 API, 메시지 발행 기능은 인터페이스를 통해 사용한다. 구현 세부사항을 application 계층에 유입하지 않는다.
- 쓰기 유스케이스의 원자성이 필요하면 `@Transactional` 경계를 애플리케이션 서비스에 둔다.
- 비즈니스 규칙은 도메인에 두고, 애플리케이션 서비스는 조회·중복 확인·호출 순서·트랜잭션을 조율한다.
- 조회 결과가 없거나 비즈니스 조건을 만족하지 않으면 의미 있는 도메인/비즈니스 예외를 사용한다. `null`이나 포괄적인 `RuntimeException`으로 대신하지 않는다.

권장 규칙:

- 변경 책임과 조회 책임이 커지면 `*ModifyService`와 `*QueryService`처럼 분리한다.
- 생성자 주입과 `final` 필드를 사용한다. 기존 코드 스타일과 맞는 경우 Lombok `@RequiredArgsConstructor`를 사용한다.
- 유스케이스 요청 모델은 변경 가능한 JavaBean보다 `record`를 우선한다.

## 5. 코딩 관례

- 클래스와 메서드 이름은 책임이 드러나게 짓는다.
- 인바운드 포트: `<대상><행위>` 또는 조회 책임을 나타내는 이름 (`MemberRegister`, `MemberFinder`)
- 서비스: `<대상><책임>Service` (`MemberModifyService`, `CouponQueryService`)
- Repository 포트: `<대상>Repository`
- 외부 클라이언트: `<외부시스템>Client`
- 응답 DTO: `<대상><행위>Response`
- 이벤트 payload: `<사건>EventPayload`
- 테스트: 대상 클래스 또는 유스케이스 이름 + `Test`
- 생성자 주입을 사용하며 필드 주입을 사용하지 않는다.
- 사용하지 않는 코드, 장기간 주석 처리된 구현, `System.out/err` 디버깅 출력을 새로 추가하지 않는다.
- 로그는 SLF4J를 사용하고 문자열 연결보다 placeholder를 사용한다.
- 단순 위임 메서드라도 불필요한 임시 변수와 중복 변환을 만들지 않는다.
- 코드 포맷은 주변 코드와 일치시킨다. 포맷만 바꾸는 대규모 변경을 기능 변경과 섞지 않는다.

## 6. 테스트 및 검증

### 6.1 테스트 원칙

- 버그 수정에는 가능하면 실패를 재현하는 회귀 테스트를 먼저 추가한다.
- 도메인 규칙은 빠른 단위 테스트로 검증한다.
- 애플리케이션 서비스 테스트는 provided 포트 단위로 정상 흐름과 주요 예외 흐름을 검증한다.
- 웹/보안 변경은 요청 검증, 상태 코드, 인증·인가, 응답 계약을 검증한다.
- DB, Redis, RabbitMQ, 외부 API 경계 변경은 단위 테스트만으로 증명할 수 없는 통합 동작을 검토한다.
- 시간, 동시성, 재시도에 의존하는 테스트는 임의의 `sleep`보다 제어 가능한 시계, 동기화 수단, polling을 사용한다.
- 테스트는 실행 순서와 외부 운영 서비스에 의존하지 않아야 한다.

테스트 픽스처는 `MemberFixture`, `AladinBookTestFixture`처럼 재사용하되 테스트의 핵심 조건이 숨겨지지 않게 한다.

### 6.2 로컬 검증 명령

변경 범위가 작으면 관련 테스트를 먼저 실행하고, 최종적으로 전체 테스트를 수행한다.

```bash
./gradlew test
git diff --check
```

아키텍처만 빠르게 확인할 때:

```bash
./gradlew test --tests bkrc.chaptersofu.ArchitectureTest
```

검증을 실행하지 못했거나 인프라 의존성 때문에 일부만 실행했다면 리뷰 결과에 그 사실과 범위를 명시한다.

## 7. 설정 및 운영 변경

- `application.yml`에는 공통 기본값을, 환경별 차이는 해당 프로필 파일 또는 환경변수에 둔다.
- 운영 데이터베이스 스키마 변경은 애플리케이션 코드만 수정하지 말고 마이그레이션 SQL과 호환 순서를 함께 검토한다.
- 설정 키를 변경하면 사용 코드, 배포 환경변수, Docker Compose/Jenkins 설정, 문서를 함께 확인한다.
- 타임아웃, 커넥션 풀, consumer concurrency, retry 횟수 변경은 장애 시 부하 증폭 가능성을 검토한다.
- 관측 가능한 주요 흐름에는 실패 원인을 찾을 수 있는 로그 또는 메트릭을 둔다.
- 로그 레벨과 health detail 공개 범위가 운영 환경에서 민감정보를 노출하지 않는지 확인한다.

## 8. 변경 유형별 리뷰 체크리스트

### API 변경

- [ ] 입력값 검증이 있는가?
- [ ] 인증된 사용자 식별자를 신뢰할 수 있는 경로에서 얻는가?
- [ ] 도메인 엔티티를 직접 노출하지 않는가?
- [ ] 상태 코드와 `ProblemDetail` 오류 계약이 일관적인가?
- [ ] 기존 클라이언트와의 호환성을 검토했는가?
- [ ] API 또는 보안 테스트가 있는가?

### DB/JPA 변경

- [ ] 트랜잭션 경계가 올바른가?
- [ ] 마이그레이션 및 롤백/호환 순서를 고려했는가?
- [ ] 유니크 제약, null 허용 여부, 인덱스가 비즈니스 규칙과 맞는가?
- [ ] N+1 또는 무제한 조회 가능성이 없는가?
- [ ] 경쟁 조건과 유실 업데이트 가능성을 검토했는가?

### Redis/동시성 변경

- [ ] 여러 명령 사이에 경쟁 조건이 없는가?
- [ ] TTL과 캐시 무효화 정책이 있는가?
- [ ] DB와 불일치할 때 보상 또는 복구할 수 있는가?
- [ ] 락의 만료, 소유권 확인, 실패 경로가 안전한가?

### 메시징/Outbox 변경

- [ ] 이벤트 저장과 비즈니스 변경의 원자성이 보장되는가?
- [ ] consumer가 중복 메시지에 안전한가?
- [ ] retry 후 DLQ 또는 복구 경로가 있는가?
- [ ] 예외를 삼켜 잘못 ack하지 않는가?
- [ ] payload 변경이 하위 호환되는가?

### 외부 API 변경

- [ ] 연결 및 읽기 타임아웃이 있는가?
- [ ] retry가 요청을 중복 수행해도 안전한가?
- [ ] circuit breaker/fallback이 실패를 정상 결과로 위장하지 않는가?
- [ ] 외부 DTO가 내부 계층으로 누출되지 않는가?

## 9. n8n 자동 코드 리뷰 계약

### 9.1 리뷰 입력

가능하면 n8n 파이프라인은 리뷰 모델에 다음 자료를 제공한다.

1. 이 개발 가이드 전문
2. PR 제목과 설명
3. base SHA와 head SHA
4. 파일별 unified diff와 변경 줄 번호
5. 변경 파일의 전체 내용 또는 diff 주변 문맥
6. 관련 테스트 결과와 빌드 로그
7. `build.gradle`, `ArchitectureTest.java` 등 변경과 직접 관련된 기준 파일

diff가 잘렸거나 생성 파일·대형 파일이라 문맥이 부족하면 모델은 단정하지 않고 `needs_context`로 표시해야 한다.

### 9.2 리뷰 범위

자동 리뷰는 다음 순서로 평가한다.

1. 컴파일 오류, 명백한 런타임 오류
2. 데이터 유실, 중복 처리, 트랜잭션 및 동시성 결함
3. 인증·인가, secret 노출, 입력 검증 등 보안 문제
4. 계층 의존성과 포트 경계 위반
5. API, 이벤트, DB 스키마의 호환성 문제
6. 예외 처리, retry/DLQ, 관측 가능성 문제
7. 누락되거나 잘못된 테스트

다음은 원칙적으로 지적하지 않는다.

- 변경 줄과 인과관계가 없는 기존 문제
- 자동 포매터로만 해결되는 사소한 취향 차이
- 실제 오류 시나리오를 설명할 수 없는 막연한 개선 제안
- 코드나 설정에서 확인할 수 없는 추측
- 같은 원인에 대한 중복 코멘트

### 9.3 심각도

| 등급 | 의미 | 예시 |
| --- | --- | --- |
| `P0` | 즉시 배포 차단. 광범위한 장애·데이터 손실·치명적 보안 사고가 확실함 | 인증 완전 우회, 운영 secret 노출, 대규모 데이터 삭제 |
| `P1` | 배포 전 수정 필수. 일반적인 입력이나 운영 조건에서 중대한 장애가 발생함 | 중복 쿠폰 지급, 메시지 유실, 권한 검증 누락 |
| `P2` | 수정 필요. 특정 조건에서 기능 오동작 또는 유지보수상 실질적 문제가 발생함 | 누락된 validation, 잘못된 상태 코드, 비멱등 consumer |
| `P3` | 낮은 위험의 개선 사항. 동작에는 즉각적인 영향이 작음 | 유용한 테스트 누락, 진단을 어렵게 하는 로그 부족 |

근거가 부족한 항목에는 심각도를 부여해 결함으로 보고하지 않는다.

### 9.4 Finding 작성 기준

각 finding은 다음 조건을 모두 만족해야 한다.

- 변경된 파일과 가능한 한 변경된 줄을 가리킨다.
- 어떤 입력 또는 실행 순서에서 문제가 발생하는지 설명한다.
- 예상 동작과 실제 동작의 차이를 설명한다.
- 이 가이드의 어떤 규칙과 관련되는지 표시한다.
- 짧고 실행 가능한 수정 방향을 제공한다.
- 하나의 finding에는 하나의 근본 원인만 담는다.

### 9.5 권장 JSON 출력

n8n에서 파싱하기 쉽도록 모델의 최종 출력은 설명 문장이나 Markdown fence 없이 다음 JSON 객체 하나로 제한하는 것을 권장한다.

```json
{
  "schema_version": "1.0",
  "summary": "변경 사항에 대한 한 문장 요약",
  "verdict": "approve | request_changes | needs_context",
  "findings": [
    {
      "severity": "P0 | P1 | P2 | P3",
      "category": "correctness | security | architecture | transaction | concurrency | messaging | api | persistence | performance | test | operations",
      "title": "짧고 구체적인 제목",
      "file": "저장소 기준 상대 경로",
      "line": 123,
      "end_line": 123,
      "evidence": "문제가 발생하는 조건과 코드 근거",
      "impact": "사용자 또는 시스템에 미치는 영향",
      "rule": "이 문서의 관련 절 제목",
      "suggestion": "최소 수정 방향",
      "confidence": "high | medium"
    }
  ],
  "checks": {
    "tests_provided": true,
    "tests_passed": true,
    "architecture_checked": true,
    "security_checked": true
  },
  "missing_context": []
}
```

판정 규칙:

- `P0` 또는 `P1`이 하나라도 있으면 `request_changes`
- 확실한 `P2`가 있으면 기본적으로 `request_changes`
- `P3`만 있거나 finding이 없으면 `approve`
- 판단에 필수인 diff 또는 문맥이 없으면 `needs_context`
- finding이 없으면 `findings`는 빈 배열로 반환
- `line`은 diff의 새 파일 기준 줄 번호를 사용
- `confidence: low`인 추측성 항목은 finding에 포함하지 않고 필요하면 `missing_context`에 기록

### 9.6 자동 리뷰 프롬프트 핵심 문구

```text
당신은 Chaptersofu 저장소의 코드 리뷰어다.
제공된 개발 가이드를 판단 기준으로 사용하되, PR diff가 실제로 새로 만든 문제만 보고한다.
각 지적은 재현 가능한 입력 또는 실행 흐름과 코드 근거가 있어야 한다.
변경되지 않은 기존 문제, 취향성 의견, 근거 없는 추측, 중복 지적은 제외한다.
출력은 지정된 JSON 스키마를 만족하는 JSON 객체 하나만 반환한다.
정보가 부족하면 결함을 추측하지 말고 verdict를 needs_context로 설정한다.
```

## 10. 현재 기준의 한계

- 이 문서는 현재 저장소 구조와 설정을 기준으로 작성되었으며 모든 관례가 자동화되어 있지는 않다.
- ArchUnit이 강제하는 규칙과 이 문서의 리뷰 규칙을 구분한다. 자동 리뷰는 ArchUnit이 보장하지 않는 포트 사용, 트랜잭션, 멱등성, 보안 등을 보완한다.
- 기존 코드가 이 문서의 모든 권장 규칙을 만족한다는 뜻은 아니다. 신규 및 변경 코드는 규칙을 지향하되, 관련 없는 기존 코드를 한 PR에서 대규모로 정리하지 않는다.
- 아키텍처나 운영 정책을 변경하면 코드와 함께 이 문서 및 n8n 프롬프트/스키마도 갱신한다.
