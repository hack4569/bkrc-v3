package bkrc.chaptersofu.application.coupon;

import bkrc.chaptersofu.application.messaging.RabbitMQConfig;

import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.application.coupon.provided.CouponRegister;
import bkrc.chaptersofu.application.coupon.event.payload.CouponIssuedEventPayload;
import bkrc.chaptersofu.application.required.outbox.Outbox;
import bkrc.chaptersofu.application.required.outbox.OutboxEventRecorder;
import bkrc.chaptersofu.application.support.AfterCommitExecutor;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.CouponException;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final OutboxEventRecorder eventRecorder;
    private final Clock couponClock;
    private final Snowflake snowflake;

    @Override
    public MemberCoupon download(Long couponId, Long memberId) {
        LocalDateTime now = LocalDateTime.now(couponClock);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND, "couponId=" + couponId));
        long databaseIssuedCount = initialIssuedCount(couponId);
        couponRedisRepository.issue(coupon, memberId, now, databaseIssuedCount);
        registerRedisRollback(couponId, memberId);

        long memberCouponId = snowflake.nextId();
        Outbox outbox = eventRecorder.record(EventType.COUPON_ISSUED,
                RabbitMQConfig.COUPON_DIRECT_EXCHANGE, RabbitMQConfig.COUPON_ISSUE_ROUTING_KEY,
                new CouponIssuedEventPayload(memberCouponId, couponId, memberId, now));

        MemberCoupon issued = MemberCoupon.issue(memberCouponId, couponId, memberId, now);
        // 이 시점은 최종 DB 발급이 아니라 Redis 예약과 Outbox 저장이 완료된 상태입니다.
        AfterCommitExecutor.execute(() -> log.info(
                "COUPON_DOWNLOAD_RESERVED memberCouponId={} couponId={} memberId={} outboxId={}",
                memberCouponId, couponId, memberId, outbox.getOutboxId()
        ));
        return issued;
    }

    @Override
    public MemberCoupon use(Long memberCouponId, Long memberId) {
        MemberCoupon memberCoupon = memberCouponRepository
                .findByMemberCouponIdAndMemberId(memberCouponId, memberId)
                .orElseThrow(() -> new CouponException(
                        ErrorCode.MEMBER_COUPON_NOT_FOUND,
                        "memberCouponId=" + memberCouponId + ", memberId=" + memberId
                ));
        Coupon coupon = couponRepository.findById(memberCoupon.getCouponId())
                .orElseThrow(() -> new CouponException(
                        ErrorCode.COUPON_NOT_FOUND,
                        "couponId=" + memberCoupon.getCouponId() + ", memberCouponId=" + memberCouponId
                ));
        memberCoupon.use(LocalDateTime.now(couponClock), coupon);
        // 쿠폰 사용 상태 변경이 실제 커밋된 이후 완료 로그를 남깁니다.
        AfterCommitExecutor.execute(() -> log.info(
                "COUPON_USED memberCouponId={} couponId={} memberId={}",
                memberCouponId, memberCoupon.getCouponId(), memberId
        ));
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
