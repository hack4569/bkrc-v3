package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.adapter.payload.CouponIssuedEventPayload;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.coupon.domain.MemberCoupon;
import com.bkrc.bkrcv3.required.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class CouponIssueConsumer {
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRedisRepository couponRedisRepository;

    /** RabbitMQ 재전송이 발생해도 같은 발급 내역을 한 번만 저장합니다. */
    @RabbitListener(queues = RabbitMQConfig.COUPON_ISSUE_QUEUE)
    @Transactional
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event == null || !(event.getPayload() instanceof CouponIssuedEventPayload payload)) {
            throw new IllegalArgumentException("유효하지 않은 쿠폰 발급 이벤트입니다.");
        }

        int inserted = memberCouponRepository.insertIfAbsent(payload.getMemberCouponId(), payload.getCouponId(),
                payload.getMemberId(), payload.getIssuedAt());
        if (inserted == 0) {
            MemberCoupon existing = memberCouponRepository
                    .findByCouponIdAndMemberId(payload.getCouponId(), payload.getMemberId())
                    .orElseThrow(() -> new IllegalStateException("중복 처리된 쿠폰 발급 내역을 찾을 수 없습니다."));

            if (existing.getMemberCouponId().equals(payload.getMemberCouponId())) {
                log.info("이미 처리된 쿠폰 발급 이벤트입니다. memberCouponId={}", payload.getMemberCouponId());
                return;
            }

            couponRedisRepository.compensateDuplicate(payload.getCouponId(), payload.getMemberId(),
                    payload.getMemberCouponId());
            log.info("중복 쿠폰 발급 예약을 Redis에서 보상했습니다. 요청 memberCouponId={}, " +
                            "기존 memberCouponId={}",
                    payload.getMemberCouponId(), existing.getMemberCouponId());
        }
    }
}
