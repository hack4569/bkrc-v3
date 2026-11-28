package bkrc.chaptersofu.application.coupon;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.CouponException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis Lua 스크립트 결과 코드를 도메인 예외로 변환하는 규칙을 검증한다.
 * 실제 Redis 없이 StringRedisTemplate을 목으로 대체하므로 스크립트 자체의 동작은 검증 대상이 아니다.
 */
class CouponRedisRepositoryTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 28, 12, 0);

    private StringRedisTemplate redisTemplate;
    private CouponRedisRepository repository;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        repository = new CouponRedisRepository(redisTemplate);
        coupon = Coupon.create("테스트 쿠폰", NOW.minusDays(1), NOW.plusDays(1),
                NOW.minusDays(1), NOW.plusDays(1), 10);
        // IDENTITY 전략이라 영속화 전에는 ID가 없으므로 Redis 키 생성을 위해 직접 지정한다.
        ReflectionTestUtils.setField(coupon, "couponId", 1L);
    }

    @Test
    void 발급_스크립트가_성공하면_발급_순번을_반환한다() {
        givenScriptResult(List.of(1L, 3L));

        int issuedCount = repository.issue(coupon, 10L, NOW, 0L);

        assertThat(issuedCount).isEqualTo(3);
        // Redis Cluster에서도 같은 슬롯에 배치되도록 hash tag가 포함된 키를 사용해야 한다.
        verify(redisTemplate).execute(any(RedisScript.class),
                eq(List.of("coupon::{1}::state", "coupon::{1}::members")), any(Object[].class));
    }

    @ParameterizedTest
    @CsvSource({
            "-2, COUPON_NOT_DOWNLOADABLE",
            "-3, COUPON_ALREADY_ISSUED",
            "-4, COUPON_OUT_OF_STOCK",
            "99, SERVER_ERROR"
    })
    void 발급_실패_코드는_대응하는_예외로_변환한다(long code, ErrorCode expected) {
        givenScriptResult(List.of(code, 0L));

        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> repository.issue(coupon, 10L, NOW, 0L));

        assertThat(exception.getErrorCode()).isEqualTo(expected);
    }

    @Test
    void 스크립트_결과가_비정상이면_서버_오류로_처리한다() {
        givenScriptResult(List.of(1L));

        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> repository.issue(coupon, 10L, NOW, 0L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SERVER_ERROR);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 남은_재고는_Redis_카운터를_우선하고_없으면_DB_발급수를_사용한다() {
        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        when(hashOperations.get("coupon::{1}::state", "issuedCount")).thenReturn("4");
        assertThat(repository.remainingStock(coupon, 0L)).isEqualTo(6);

        when(hashOperations.get("coupon::{1}::state", "issuedCount")).thenReturn(null);
        assertThat(repository.remainingStock(coupon, 7L)).isEqualTo(3);

        // 발급 수가 재고보다 많아도 음수 재고가 노출되지 않아야 한다.
        assertThat(repository.remainingStock(coupon, 20L)).isZero();
    }

    @Test
    void 초기화_여부는_상태_키_존재로_판단한다() {
        when(redisTemplate.hasKey("coupon::{1}::state")).thenReturn(true);

        assertThat(repository.isInitialized(1L)).isTrue();
        assertThat(repository.isInitialized(2L)).isFalse();
    }

    @Test
    void 롤백과_중복_보상은_각각의_키로_스크립트를_실행한다() {
        repository.rollback(1L, 10L);
        repository.compensateDuplicate(1L, 10L, 100L);

        verify(redisTemplate).execute(any(RedisScript.class),
                eq(List.of("coupon::{1}::state", "coupon::{1}::members")), any(Object[].class));
        verify(redisTemplate).execute(any(RedisScript.class),
                eq(List.of("coupon::{1}::state", "coupon::{1}::members",
                        "coupon::{1}::duplicate-compensations")), any(Object[].class));
    }

    private void givenScriptResult(List<?> result) {
        // execute는 가변 인자 메서드이므로 인자 배열 전체를 매칭해야 스텁이 적용된다.
        doReturn(result).when(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }
}
