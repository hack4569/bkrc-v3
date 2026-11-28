# 계층별 규칙 (bkrc-v3)

SKILL.md의 체크리스트만으로 판단이 애매할 때 읽는다. 각 규칙에는 이유를 적었다. 규칙이 딱 맞지 않는 상황에서는 이유를 기준으로 판단한다.

## 목차
1. 엔티티 (domain)
2. 예외와 ErrorCode
3. 포트와 요청 DTO (application/x/provided)
4. 서비스 (application/x)
5. Repository
6. REST API와 응답 DTO (adapter/webapi)
7. 보안 (adapter/security/WebSecurity)
8. 테스트
9. 하지 말 것

## 1. 엔티티

- 위치: `domain/x/X.java`. JPA 애노테이션은 허용한다(프로젝트 관례). 하지만 Spring 서비스나 `application` 타입은 참조하지 않는다.
- `@Entity`, `@Table(name = "x")`, `@Getter`. 테이블 이름은 소문자 스네이크다. 컬럼은 `CamelCaseToUnderscoresNamingStrategy`가 자동 변환하므로 `@Column(name=...)`은 꼭 필요할 때만 쓴다.
- 기본 생성자는 `protected X() {}`. JPA용이고, 외부에서 빈 객체를 만들지 못하게 하려는 것이다.
- 생성은 정적 팩토리 `public static X create(...)`. 생성 시 불변식(필수값, 범위)을 여기서 검증하고, 기본 상태값(예: `active = true`)도 여기서 정한다.
- 수정은 `public void modify(...)`처럼 의도가 드러나는 메서드로 한다. 상태 전이는 `close()`, `approve()`처럼 이름을 붙인다. setter는 두지 않는다. 변경 지점을 한곳에 모아야 규칙이 새지 않는다.
- 소유자 검증이 필요하면 엔티티에 `boolean isOwnedBy(Long memberId)`를 둔다. 서비스가 이 메서드로 판단한다.
- ID:
  - 기본은 `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long xId;` (Coupon 방식)
  - Snowflake가 필요하면 `@Id private Long xId;`로 두고 서비스에서 `snowflake.nextId()`를 팩토리에 넘긴다 (Member 방식).
- 생성·수정 시각이 필요하면 `BaseEntity`를 상속한다. Auditing으로 `created`/`updated`가 채워진다.
- 연관 관계는 `@ManyToOne(fetch = FetchType.LAZY)`를 기본으로 한다. 양방향 `@OneToMany`는 필요할 때만 쓴다. 기본 EAGER는 목록 조회에서 N+1을 만든다.
- 문자열 컬럼은 `@Column(nullable = ..., length = ...)`로 DB 제약을 명시한다. `ddl-auto: update`가 이 값으로 테이블을 만든다.
- 클래스 Javadoc에 이 엔티티가 무엇을 나타내는지, 중요한 규칙은 무엇인지 한국어로 적는다(Coupon 참고).

## 2. 예외와 ErrorCode

- `ErrorCode` enum에 도메인 항목을 추가한다. 형식은 `X_NOT_FOUND(HttpStatus.NOT_FOUND, "E<접두어><번호>", "한국어 메시지")`.
  - 접두어는 기존과 겹치지 않게 정한다(기존: AL, U, L, H, R, C, AU, CM). 추가하기 전에 파일을 열어 사용 중인 접두어를 확인한다.
  - 상태 코드: 없음 404, 중복 409, 상태상 불가 409, 권한 없음 403, 입력 오류 400.
- 예외 클래스는 `domain/x/`에 두고 `BusinessException`을 상속한다. `ApiControllerAdvice`가 `BusinessException`을 `ProblemDetail`로 변환하므로 컨트롤러에서 따로 잡지 않는다.
- 없는 리소스: `XNotFoundException(Long xId)` → `super(ErrorCode.X_NOT_FOUND, "xId=" + xId)`. 식별자를 메시지에 넣어야 로그로 추적할 수 있다.
- 권한 없음 같은 다른 실패는 `XException(ErrorCode)` 하나로 묶어도 된다(`LikeException`, `HistoryException` 방식).

## 3. 포트와 요청 DTO

- `XFinder`(조회)와 `XRegister`(등록·수정·삭제)로 나눈다. 컨트롤러와 다른 도메인은 서비스 구현체가 아니라 이 인터페이스에 의존한다. 조회와 변경을 나누면 의존하는 쪽이 필요한 권한만 갖게 된다.
- 메서드 이름 관례:
  - 조회: `getX(Long xId)`, `getXList(...)` 또는 `getXs(Pageable)`
  - 변경: `register(XRegisterRequest, Long memberId)`, `modify(Long xId, XModifyRequest, Long memberId)`, `delete(Long xId, Long memberId)`
- 포트는 **도메인 엔티티를 반환**한다. 응답 DTO 변환은 adapter의 책임이다(`MemberRegister`가 `Member`를 반환하는 것과 같다).
- 요청 DTO는 `provided` 패키지의 record로 만든다. 각 필드에 `@Schema(description, example)`와 Bean Validation(`@NotBlank`, `@NotNull`, `@Size`, `@Min`/`@Max`)을 붙이고, 검증 메시지는 한국어로 쓴다.
- 작성자 ID 같은 인증 정보는 요청 DTO에 넣지 않는다. 컨트롤러가 `@AuthenticationPrincipal`로 받아 별도 파라미터로 넘긴다. 클라이언트가 다른 사람의 ID를 보내는 위조를 막기 위해서다.

## 4. 서비스

- `XQueryService implements XFinder`: `@Service @Transactional(readOnly = true) @Validated @RequiredArgsConstructor`.
- `XModifyService implements XRegister`: `@Service @Transactional @Validated @RequiredArgsConstructor @Slf4j`.
- 단건 조회는 `repository.findById(id).orElseThrow(() -> new XNotFoundException(id))`.
- 수정·삭제는 조회 → 소유자 검증 → 엔티티 메서드 호출 순서로 한다. 수정은 JPA 변경 감지로 반영되므로 `save()`를 다시 호출하지 않아도 된다. 기존 Member 코드는 호출하지만, 새 코드에서는 생략해도 된다.
- 변경이 성공했을 때의 로그는 `AfterCommitExecutor.execute(() -> log.info("X_REGISTERED xId={}", ...))`처럼 **커밋 후에** 남긴다. 롤백된 작업이 성공 로그로 남는 것을 막기 위해서다. 비밀번호, 토큰, 개인정보는 로그에 남기지 않는다.
- 다른 도메인 데이터가 필요하면 그 도메인의 `provided` 포트(`MemberFinder` 등)를 주입한다. 다른 도메인의 Repository를 직접 쓰지 않는다.
- 이벤트(RabbitMQ 알림 등)가 필요하면 `MemberModifyService.saveMember`의 `OutboxEventRecorder` 패턴을 따른다. 기본 CRUD에는 넣지 않는다.

## 5. Repository

- `application/x/XRepository.java`: `@Repository public interface XRepository extends JpaRepository<X, Long>`.
- 메서드 이름 기반 쿼리를 먼저 쓰고, 복잡하면 `@Query`(JPQL)를 쓴다. 파라미터는 `@Param`으로 바인딩한다. 문자열 결합은 쓰지 않는다.
- 연관 엔티티를 함께 쓰는 목록 조회는 `JOIN FETCH` 또는 `@EntityGraph`로 N+1을 막는다. 페이징과 컬렉션 fetch join을 함께 쓰면 메모리 페이징이 일어나므로 피한다.

## 6. REST API와 응답 DTO

- `adapter/webapi/XApi.java`: `@Tag(name = "한글명 (X)", description = ...)`, `@RestController`, `@RequestMapping("/v1/{{path}}")`, `@RequiredArgsConstructor`.
- 의존성은 `XFinder`와 `XRegister` 포트만 둔다.
- 엔드포인트 관례:

| 기능 | 메서드·경로 | 반환 |
|---|---|---|
| 등록 | `POST /v1/{path}` | `XResponse` |
| 단건 조회 | `GET /v1/{path}/{xId}` | `XResponse` |
| 목록 조회 | `GET /v1/{path}` | `List<XResponse>` 또는 `Page<XResponse>` |
| 수정 | `PUT /v1/{path}/{xId}` | `XResponse` |
| 삭제 | `DELETE /v1/{path}/{xId}` | `void` |

- 모든 메서드에 `@Operation(summary, description)`을 붙이고, 실패 응답은 `@ApiResponses`에 `ProblemDetail` 스키마로 문서화한다(LikeApi 참고).
- 요청 본문은 `@RequestBody @Valid`. 로그인 회원은 `@Parameter(hidden = true) @AuthenticationPrincipal Long memberId`.
- 응답 DTO는 `adapter/webapi/dto/XResponse.java`의 record로 만들고, `public static XResponse of(X x)`로 변환한다. 엔티티나 연관 엔티티를 그대로 담지 않는다. 지연 로딩 예외와 내부 필드 노출을 막기 위해서다. 날짜는 `BaseEntity`의 `getCreatedDateTime()` 같은 포맷 메서드를 활용한다.

## 7. 보안

- `WebSecurity`의 `authorizeHttpRequests`는 마지막이 `anyRequest().permitAll()`이다. **새 API는 등록하지 않으면 공개**된다.
- 변경 API(POST/PUT/DELETE)는 `.requestMatchers(HttpMethod.POST, "/v1/{path}").authenticated()`처럼 메서드별로 추가한다. `anyRequest()`보다 위에 둬야 한다. 기존 쿠폰 규칙처럼 이유를 한국어 주석으로 남긴다.
- 인증만으로 끝나지 않는다. 소유자 검증은 서비스에서 해야 한다(4절).

## 8. 테스트

- **도메인 단위 테스트** `test/.../domain/x/XTest.java`: 스프링 없이 `create()`의 기본값과 검증 실패, `modify()`, `isOwnedBy()`를 검증한다. 외부 협력 객체는 `mock()`으로 만든다.
- **포트 테스트** `test/.../application/x/provided/XRegisterTest.java`, `XFinderTest.java`:
  - `@CommonApiTest`(SpringBootTest + test 프로필)와 `@Transactional`(`jakarta.transaction.Transactional`, 기존 테스트와 동일)을 붙인다.
  - 서비스 구현체가 아니라 포트(`XRegister`, `XFinder`)를 `@Autowired`로 주입해 검증한다. 구현이 바뀌어도 계약 테스트가 유지된다.
  - 저장 후 `entityManager.flush(); entityManager.clear();`를 한 뒤 다시 조회한다. 1차 캐시가 아니라 실제 DB에서 읽은 값을 검증하기 위해서다.
  - 실패 검증은 `catchThrowableOfType(XNotFoundException.class, () -> ...)`로 잡고 `getErrorCode()`를 비교한다.
  - 최소 시나리오: 등록 성공, 단건 조회, 없는 ID 조회 실패, 수정 성공, 다른 회원의 수정·삭제 실패, 삭제 후 조회 실패.
  - 회원이 필요하면 `MemberRegister.saveMember(new MemberRegisterRequest(...))`로 만든다.
- 테스트 메서드 이름은 한국어 snake 형식(`없는_리뷰를_조회하면_예외가_발생한다`). given/when/then 구분이나 의도는 한국어 주석으로 적는다.
- Fixture가 여러 테스트에서 반복되면 `domain/x/XFixture.java`에 정적 팩토리로 만든다. 비어 있는 Fixture 클래스는 만들지 않는다.

## 9. 하지 말 것

- 컨트롤러에서 Repository를 직접 호출하기
- 엔티티에 `@Setter`나 public 기본 생성자 두기
- 엔티티를 응답으로 직접 반환하기
- 요청 DTO에 `memberId`를 넣어 클라이언트가 보내게 하기
- 주석 처리된 코드를 남기기 (기준 파일에 있어도 따라 하지 않는다)
- 다른 도메인의 Repository를 주입하기 (포트를 쓴다)
