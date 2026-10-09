package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.adapter.payload.CouponIssuedEventPayload;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.coupon.domain.Coupon;
import com.bkrc.bkrcv3.coupon.domain.CouponException;
import com.bkrc.bkrcv3.coupon.domain.MemberCoupon;
import com.bkrc.bkrcv3.coupon.application.provided.CouponFinder;
import com.bkrc.bkrcv3.coupon.application.provided.CouponRegister;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/** 쿠폰 노출, 발급, 보유 내역 조회 및 사용 처리를 담당합니다. */
public class CouponService implements CouponFinder, CouponRegister {
    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRedisRepository couponRedisRepository;
    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock couponClock;
    private final Snowflake snowflake;

    @Transactional(readOnly = true)
    @Override
    public List<CouponResponse> getDownloadable() {
        // 서버 기본 타임존과 무관하게 쿠폰 정책은 한국 시각을 사용합니다.
        return couponRepository.findVisible(LocalDateTime.now(couponClock)).stream()
                .map(coupon -> CouponResponse.from(coupon, remainingStock(coupon)))
                .filter(coupon -> coupon.remainingStock() > 0)
                .toList();
    }

    /** Redis Lua로 발급을 예약하고, DB 저장 명령은 Outbox를 통해 RabbitMQ로 전달합니다. */
    @Transactional
    @Override
    public CouponResponse.Issued download(Long couponId, Long memberId) {
        LocalDateTime now = LocalDateTime.now(couponClock);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));
        long databaseIssuedCount = initialIssuedCount(couponId);
        int remainingStock = couponRedisRepository.issue(coupon, memberId, now, databaseIssuedCount);
        registerRedisRollback(couponId, memberId);
        long memberCouponId = snowflake.nextId();
        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.COUPON_ISSUED,
                RabbitMQConfig.COUPON_DIRECT_EXCHANGE,
                RabbitMQConfig.COUPON_ISSUE_ROUTING_KEY,
                Event.of(EventType.COUPON_ISSUED,
                        new CouponIssuedEventPayload(memberCouponId, couponId, memberId, now)).toJson()
        ));
        eventPublisher.publishEvent(OutboxEvent.of(outbox));

        // DB 반영은 비동기이므로 Redis가 승인한 결과와 미리 생성한 ID로 즉시 응답합니다.
        return CouponResponse.Issued.pending(memberCouponId, coupon, now, remainingStock);
    }

    private int remainingStock(Coupon coupon) {
        return couponRedisRepository.remainingStock(coupon, initialIssuedCount(coupon.getCouponId()));
    }

    /** Redis 상태가 유실되었거나 최초 접근일 때만 DB 발급 건수로 카운터를 복구합니다. */
    private long initialIssuedCount(Long couponId) {
        if (couponRedisRepository.isInitialized(couponId)) {
            return 0L;
        }
        return memberCouponRepository.countByCouponId(couponId);
    }

    private void registerRedisRollback(Long couponId, Long memberId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    try {
                        couponRedisRepository.rollback(couponId, memberId);
                    } catch (RuntimeException exception) {
                        log.error("Coupon Redis compensation failed - couponId={}, memberId={}",
                                couponId, memberId, exception);
                    }
                }
            }
        });
    }

    @Transactional(readOnly = true)
    @Override
    public List<CouponResponse.Issued> getMine(Long memberId) {
        List<MemberCoupon> issuedCoupons = memberCouponRepository.findByMemberIdOrderByIssuedAtDesc(memberId);
        Map<Long, Coupon> couponsById = couponRepository.findAllById(
                        issuedCoupons.stream().map(MemberCoupon::getCouponId).distinct().toList())
                .stream().collect(Collectors.toMap(Coupon::getCouponId, Function.identity()));
        return issuedCoupons.stream()
                .filter(value -> couponsById.containsKey(value.getCouponId()))
                .map(value -> CouponResponse.Issued.from(value, couponsById.get(value.getCouponId())))
                .toList();
    }

    @Transactional
    @Override
    public CouponResponse.Issued use(Long memberCouponId, Long memberId) {
        // memberId를 조건에 포함해 본인에게 발급된 쿠폰만 사용할 수 있게 합니다.
        MemberCoupon value = memberCouponRepository.findByMemberCouponIdAndMemberId(memberCouponId, memberId)
                .orElseThrow(() -> new CouponException(ErrorCode.MEMBER_COUPON_NOT_FOUND));
        Coupon coupon = couponRepository.findById(value.getCouponId())
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));
        value.use(LocalDateTime.now(couponClock), coupon);
        return CouponResponse.Issued.from(value, coupon);
    }
}
