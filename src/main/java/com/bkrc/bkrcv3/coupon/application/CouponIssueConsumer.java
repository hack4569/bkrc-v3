package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.adapter.payload.CouponIssuedEventPayload;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
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
            log.info("Coupon issue event already processed - memberCouponId={}", payload.getMemberCouponId());
        }
    }
}
