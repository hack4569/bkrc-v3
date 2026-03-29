package com.bkrc.bkrcv3.notification.consumer;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.common.event.payload.EventPayload;
import com.bkrc.bkrcv3.notification.outbox.NotificationOutbox;
import com.bkrc.bkrcv3.notification.outbox.NotificationOutboxRepository;
import com.bkrc.bkrcv3.notification.service.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final NotificationOutboxRepository outboxRepository;

    @RabbitListener(queues = EventType.Queue.MEMBER_JOIN)
    public void listenMemberJoin(String message,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        handle(message, channel, tag);
    }

    @RabbitListener(queues = EventType.Queue.MEMBER_MODIFY)
    public void listenMemberModify(String message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        handle(message, channel, tag);
    }

    private void handle(String message, Channel channel, long tag) throws IOException {
        Event<EventPayload> event = Event.fromJson(message);

        if (event == null) {
            log.warn("[Consumer] 이벤트 파싱 실패 message={}", message);
            channel.basicAck(tag, false);
            return;
        }

        try {
            notificationService.handleEvent(event);

            // 알람 처리 완료 후 Outbox 삭제
            outboxRepository.deleteById(event.getOutboxId());

            channel.basicAck(tag, false); // 처리 완료
            log.info("[Consumer] 처리 완료 eventType={}", event.getType());

        } catch (Exception e) {
            channel.basicNack(tag, false, true); // 실패 시 재큐
            log.error("[Consumer] 처리 실패 eventType={}", event.getType(), e);
        }
    }
}