---
name: crud-scaffold
description: bkrc-v3에서 새 도메인의 CRUD(JPA 엔티티, 등록/조회/수정/삭제 서비스, provided 포트, Repository, REST API, Request/Response DTO, ErrorCode, 테스트)를 이 프로젝트의 헥사고날 구조와 기존 member/coupon 모듈 스타일 그대로 만들 때 사용한다. "OO 관리 기능 만들어줘", "OO 엔티티/테이블 추가", "OO API 만들어줘", "등록·수정·삭제·목록 조회 기능", "게시판/리뷰/댓글/공지 기능 추가"처럼 CRUD라는 단어가 없어도 새 엔티티와 그 기본 기능을 만드는 요청이면 반드시 사용한다. 기존 도메인에 필드나 API 하나를 추가하는 작업에도 계층 규칙 확인용으로 사용한다.
---

# bkrc-v3 CRUD 스캐폴딩

새 도메인의 CRUD를 **기존 코드와 구별되지 않게** 만드는 것이 목표다. 모듈마다 구조, 이름, 예외, 테스트 방식이 같아야 리뷰와 유지보수가 쉬워진다.

이 문서는 규칙과 절차만 담는다. 실제 코드 스타일은 항상 저장소의 기준 파일을 읽고 따른다. skill 안에 코드를 복사해 두면 코드가 바뀌었을 때 skill만 낡기 때문이다. 기준 파일과 이 문서가 다르면 **기준 파일을 따르고**, 차이를 사용자에게 알린다.

## 1. 시작 전에 확정할 입력

요청에 없는 항목만 한 번에 모아 질문한다. 사소한 항목은 아래 기본값을 쓰고 완료 보고에 명시한다.

| 항목 | 기본값 |
|---|---|
| 도메인 이름 (영문 단수 PascalCase, 예: `Review`) | 필수 |
| 필드, 타입, 필수 여부, 길이·범위 검증 | 필수 |
| 필요한 기능 (등록/단건 조회/목록 조회/수정/삭제) | 전부 |
| 목록 조회 페이징 | 데이터가 계속 쌓이는 도메인이면 `Pageable`, 아니면 `List` |
| 연관 관계 (예: Member N:1, Book N:1) | 없음 |
| 소유자 개념 (작성자만 수정·삭제 가능한가) | 회원이 만드는 데이터면 있음 |
| 인증 필요 여부 | 조회는 공개, 변경은 인증 |
| ID 생성 방식 | `IDENTITY` (외부에 순번이 노출되면 안 되거나 분산 생성이 필요하면 `Snowflake`) |

## 2. 기준 파일 (작성 전에 반드시 읽는다)

경로는 `src/main/java/bkrc/chaptersofu/`, 테스트는 `src/test/java/bkrc/chaptersofu/` 기준이다.

| 만들 것 | 읽을 기준 파일 | 따를 점 |
|---|---|---|
| 엔티티 | `domain/coupon/Coupon.java` | `protected` 기본 생성자, 정적 팩토리 `create()`, 상태 변경은 의미 있는 메서드로 |
| 감사 필드 | `domain/common/shared/BaseEntity.java` | 생성·수정 시각이 필요하면 상속 (`domain/member/Member.java` 참고) |
| 도메인 예외 | `domain/member/MemberNotFoundException.java`, `domain/common/shared/ErrorCode.java` | `BusinessException` 상속 + `ErrorCode` 항목 추가 |
| 포트 | `application/member/provided/MemberFinder.java`, `MemberRegister.java` | 조회 `XFinder`, 변경 `XRegister` 인터페이스 |
| 요청 DTO | `application/member/provided/MemberRegisterRequest.java` | `provided` 패키지의 record + `@Schema` + Bean Validation |
| 서비스 | `application/member/MemberQueryService.java`, `MemberModifyService.java` | 클래스 레벨 `@Transactional(readOnly = true)` / `@Transactional`, `@Validated`, `AfterCommitExecutor` 로그 |
| Repository | `application/member/MemberRepository.java` | `application/{x}` 패키지의 `JpaRepository` |
| API | `adapter/webapi/MemberApi.java`, `CouponApi.java` | `@Tag`/`@Operation`/`@ApiResponses`, `@Valid`, `@AuthenticationPrincipal Long memberId` |
| 응답 DTO | `adapter/webapi/dto/MemberModifyResponse.java` | record + 정적 팩토리 `of(엔티티)` |
| 도메인 테스트 | `test/.../domain/member/MemberTest.java` | 스프링 없는 단위 테스트, 협력 객체는 Mockito |
| 포트 테스트 | `test/.../application/member/provided/MemberRegisterTest.java` | `@CommonApiTest` + `@Transactional`, `flush/clear`, `catchThrowableOfType` + `ErrorCode` 검증 |

기준 파일에 남아 있는 주석 처리된 코드, 비어 있는 Fixture, `Member`의 `public` 기본 생성자 같은 흔적은 **따라 하지 않는다**.

계층별 세부 규칙은 `references/layer-rules.md`에 있다. 처음 이 skill을 쓰거나 판단이 애매하면 먼저 읽는다.

## 3. 생성할 파일 (도메인 `X`, 패키지명 `x`)

```
domain/x/X.java                                 엔티티
domain/x/XNotFoundException.java                (필요 시 XException 등 추가)
domain/common/shared/ErrorCode.java             X_NOT_FOUND 등 항목 추가 (기존 파일 수정)
application/x/provided/XFinder.java             조회 포트
application/x/provided/XRegister.java           변경 포트
application/x/provided/XRegisterRequest.java    등록 요청
application/x/provided/XModifyRequest.java      수정 요청 (수정 기능이 있을 때)
application/x/XRepository.java
application/x/XQueryService.java                implements XFinder
application/x/XModifyService.java               implements XRegister
adapter/webapi/XApi.java
adapter/webapi/dto/XResponse.java
adapter/security/WebSecurity.java               인증 규칙 추가 (기존 파일 수정)
test: domain/x/XTest.java, application/x/provided/XRegisterTest.java, XFinderTest.java
```

반복되는 뼈대는 `assets/templates/*.java.tmpl`에 있다. `{{X}}`(PascalCase), `{{x}}`(패키지·변수용 camelCase), `{{X_UPPER}}`(ErrorCode용 대문자 스네이크), `{{path}}`(URL 경로, 복수형 kebab-case, 예: `reviews`)를 바꿔 쓰고, `TODO` 표시 부분은 도메인에 맞게 채운다. 템플릿은 출발점일 뿐이므로 필요 없는 메서드는 지우고 필요한 것은 추가한다.

## 4. 작업 순서

1. 1절의 입력을 확정하고, AGENTS.md 규칙에 따라 **구현 전에 설계와 장단점을 간단히 설명**한다(테이블 구조, ID 방식, 인증 범위, 페이징 여부).
2. 2절 기준 파일을 읽는다.
3. 안쪽 계층부터 만든다: 엔티티·예외·ErrorCode → 도메인 테스트 → 포트·요청 DTO → Repository → 서비스 → 포트 테스트 → API·응답 DTO → WebSecurity.
   안쪽부터 만들면 바깥 계층을 만들 때 이미 확정된 타입만 참조하게 되어 수정이 줄어든다.
4. 새 코드에는 AGENTS.md 규칙대로 목적·이유·주의점을 설명하는 **한국어 주석**을 단다.
5. 테스트를 실행한다: `./gradlew test --tests "bkrc.chaptersofu.domain.x.*" --tests "bkrc.chaptersofu.application.x.*"`. 실패하면 고치고, 끝내 실패하면 출력과 함께 그대로 보고한다.
6. 5절 체크리스트를 확인하고 완료 보고를 한다.

참고: 이 저장소에는 `src/main/java`에 새 클래스를 만들면 테스트 작성을 요구하는 PostToolUse 훅이 있다. 이 skill의 순서대로 테스트를 함께 만들면 훅 지시와 충돌하지 않는다. 포트 테스트가 서비스 테스트를 대신하므로, 훅이 `XQueryServiceTest`/`XModifyServiceTest`를 요구하면 "provided 포트 테스트로 검증함"이라고 설명하고 건너뛴다.

## 5. 완료 체크리스트

- [ ] 의존 방향: `domain`은 `application`·`adapter`를 import하지 않는다. `application`은 `adapter`를 import하지 않는다.
- [ ] 엔티티가 API 응답으로 직접 나가지 않고 `XResponse.of()`로 변환된다.
- [ ] 엔티티 setter가 없고, 상태 변경은 `modify(...)`처럼 의미 있는 메서드로만 한다.
- [ ] 조회 서비스는 `readOnly = true`, 변경 서비스는 `@Transactional`.
- [ ] 없는 ID 조회 시 `XNotFoundException`(→ 404)이 발생하고, `ErrorCode` 코드가 기존 코드와 겹치지 않는다.
- [ ] 소유자가 있는 데이터는 수정·삭제 시 `memberId`로 소유자를 검증한다. 남의 데이터를 수정할 수 있으면 보안 결함이다.
- [ ] `WebSecurity`에 변경 API의 인증 규칙을 추가했다. 기본값이 `anyRequest().permitAll()`이라 추가하지 않으면 **인증 없이 호출된다**.
- [ ] 요청 DTO에 Bean Validation이 있고 컨트롤러에서 `@Valid`를 붙였다.
- [ ] 목록 조회가 연관 엔티티를 사용하면 N+1이 생기지 않는다 (fetch join 또는 `@EntityGraph`).
- [ ] 테스트: 도메인 단위 테스트, 포트 테스트(정상 + 없는 ID + 검증 실패 + 소유자 아님) 작성 및 통과.
- [ ] 한국어 주석이 목적·이유·주의점을 설명한다.

## 6. 완료 보고 형식

1. 생성·수정한 파일 목록 (계층별)
2. 추가한 API 목록 (메서드, 경로, 인증 여부)
3. 테스트 실행 결과 (통과/실패 수, 실패 시 출력)
4. 기본값으로 정한 항목 (1절 표 중 사용자가 지정하지 않은 것)
5. 장점, 단점, 제약 사항 (AGENTS.md 규칙). 예: `ddl-auto: update`로 테이블이 자동 생성되지만 운영 DB의 컬럼 변경·삭제는 반영되지 않으므로 필요하면 `src/main/resources/db/`에 SQL을 추가해야 함
