package bkrc.chaptersofu.application.coupon;

import bkrc.chaptersofu.application.shared.event.Event;
import bkrc.chaptersofu.application.shared.event.EventType;
import bkrc.chaptersofu.application.coupon.provided.CouponRegister;
import bkrc.chaptersofu.application.coupon.event.payload.CouponIssuedEventPayload;
import bkrc.chaptersofu.application.messaging.RabbitMQConfig;
import bkrc.chaptersofu.application.outbox.Outbox;
import bkrc.chaptersofu.application.outbox.OutboxEvent;
import bkrc.chaptersofu.application.outbox.OutboxRepository;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.CouponException;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CouponModifyService implements CouponRegister {
    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRedisRepository couponRedisRepository;
    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock couponClock;
    private final Snowflake snowflake;

    @Override
    public MemberCoupon download(Long couponId, Long memberId) {
        LocalDateTime now = LocalDateTime.now(couponClock);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));
        long databaseIssuedCount = initialIssuedCount(couponId);
        couponRedisRepository.issue(coupon, memberId, now, databaseIssuedCount);
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

        return MemberCoupon.issue(memberCouponId, couponId, memberId, now);
    }

    @Override
    public MemberCoupon use(Long memberCouponId, Long memberId) {
        MemberCoupon memberCoupon = memberCouponRepository
                .findByMemberCouponIdAndMemberId(memberCouponId, memberId)
                .orElseThrow(() -> new CouponException(ErrorCode.MEMBER_COUPON_NOT_FOUND));
        Coupon coupon = couponRepository.findById(memberCoupon.getCouponId())
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));
        memberCoupon.use(LocalDateTime.now(couponClock), coupon);
        return memberCoupon;
    }

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
}
