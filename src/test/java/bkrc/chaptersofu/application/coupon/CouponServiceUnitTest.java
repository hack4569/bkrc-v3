package bkrc.chaptersofu.application.coupon;

import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.application.required.outbox.Outbox;
import bkrc.chaptersofu.application.required.outbox.OutboxEventRecorder;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.CouponException;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 쿠폰 발급/조회 서비스의 분기와 트랜잭션 완료 후 Redis 보상 로직을 검증한다.
 * Redis 롤백은 트랜잭션 동기화 콜백에 등록되므로 테스트에서 동기화를 직접 활성화해 콜백을 호출한다.
 */
class CouponServiceUnitTest {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 28, 12, 0);

    private CouponRepository couponRepository;
    private MemberCouponRepository memberCouponRepository;
    private CouponRedisRepository couponRedisRepository;
    private OutboxEventRecorder eventRecorder;
    private Snowflake snowflake;
    private CouponModifyService modifyService;
    private CouponQueryService queryService;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        memberCouponRepository = mock(MemberCouponRepository.class);
        couponRedisRepository = mock(CouponRedisRepository.class);
        eventRecorder = mock(OutboxEventRecorder.class);
        snowflake = mock(Snowflake.class);
        // 기간 판단이 실행 시각에 흔들리지 않도록 고정 시계를 주입한다.
        Clock clock = Clock.fixed(NOW.atZone(SEOUL).toInstant(), SEOUL);
        modifyService = new CouponModifyService(couponRepository, memberCouponRepository,
                couponRedisRepository, eventRecorder, clock, snowflake);
        queryService = new CouponQueryService(couponRepository, memberCouponRepository,
                couponRedisRepository, clock);
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        // 스레드 로컬 상태가 다른 테스트로 새지 않도록 반드시 정리한다.
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void 쿠폰을_다운로드하면_Redis_예약_후_Outbox를_기록한다() {
        Coupon coupon = coupon(1L, 10);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRedisRepository.isInitialized(1L)).thenReturn(false);
        when(memberCouponRepository.countByCouponId(1L)).thenReturn(2L);
        when(snowflake.nextId()).thenReturn(500L);
        when(eventRecorder.record(any(), any(), any(), any())).thenReturn(mock(Outbox.class));

        MemberCoupon issued = modifyService.download(1L, 10L);

        assertThat(issued.getMemberCouponId()).isEqualTo(500L);
        assertThat(issued.getIssuedAt()).isEqualTo(NOW);
        // Redis 카운터가 없으면 DB 발급 수로 초기화해야 재고가 중복 계산되지 않는다.
        verify(couponRedisRepository).issue(coupon, 10L, NOW, 2L);
        verify(eventRecorder).record(eq(EventType.COUPON_ISSUED), any(), any(), any());
    }

    @Test
    void 트랜잭션이_롤백되면_Redis_예약을_되돌린다() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon(1L, 10)));
        when(couponRedisRepository.isInitialized(1L)).thenReturn(true);
        when(eventRecorder.record(any(), any(), any(), any())).thenReturn(mock(Outbox.class));
        modifyService.download(1L, 10L);

        // 보상 실패가 발생해도 예외가 전파되지 않고 로그만 남겨야 한다.
        doThrow(new IllegalStateException("redis down")).when(couponRedisRepository).rollback(1L, 10L);
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(couponRedisRepository).rollback(1L, 10L);
        verify(memberCouponRepository, never()).countByCouponId(any());
    }

    @Test
    void 트랜잭션이_커밋되면_Redis_예약을_유지한다() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon(1L, 10)));
        when(eventRecorder.record(any(), any(), any(), any())).thenReturn(mock(Outbox.class));
        modifyService.download(1L, 10L);

        completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

        verify(couponRedisRepository, never()).rollback(any(), any());
    }

    @Test
    void 존재하지_않는_쿠폰은_다운로드할_수_없다() {
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> modifyService.download(1L, 10L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COUPON_NOT_FOUND);
    }

    @Test
    void 쿠폰_원본이_삭제된_회원_쿠폰은_사용할_수_없다() {
        when(memberCouponRepository.findByMemberCouponIdAndMemberId(500L, 10L))
                .thenReturn(Optional.of(MemberCoupon.issue(500L, 1L, 10L, NOW)));
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> modifyService.use(500L, 10L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COUPON_NOT_FOUND);
    }

    @Test
    void 다운로드_가능한_쿠폰은_남은_재고가_있는_것만_반환한다() {
        Coupon available = coupon(1L, 10);
        Coupon soldOut = coupon(2L, 10);
        when(couponRepository.findVisible(NOW)).thenReturn(List.of(available, soldOut));
        when(couponRedisRepository.isInitialized(1L)).thenReturn(true);
        when(memberCouponRepository.countByCouponId(2L)).thenReturn(10L);
        when(couponRedisRepository.remainingStock(available, 0L)).thenReturn(3);
        when(couponRedisRepository.remainingStock(soldOut, 10L)).thenReturn(0);

        assertThat(queryService.getDownloadable()).containsExactly(available);
    }

    @Test
    void 쿠폰_단건_조회시_없으면_예외가_발생한다() {
        Coupon coupon = coupon(1L, 10);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        assertThat(queryService.getCoupon(1L)).isSameAs(coupon);
        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> queryService.getCoupon(2L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COUPON_NOT_FOUND);
    }

    private void completeTransaction(int status) {
        // 실제 트랜잭션 매니저가 호출하는 순서대로 커밋 후/완료 후 콜백을 실행한다.
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            synchronizations.forEach(TransactionSynchronization::afterCommit);
        }
        synchronizations.forEach(synchronization -> synchronization.afterCompletion(status));
    }

    private Coupon coupon(Long couponId, int stock) {
        Coupon coupon = Coupon.create("테스트 쿠폰", NOW.minusDays(1), NOW.plusDays(1),
                NOW.minusDays(1), NOW.plusDays(1), stock);
        ReflectionTestUtils.setField(coupon, "couponId", couponId);
        return coupon;
    }
}
