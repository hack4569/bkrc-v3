package bkrc.chaptersofu.adapter.messaging.consumer;

import bkrc.chaptersofu.application.messaging.RabbitMQConfig;
import bkrc.chaptersofu.application.shared.event.Event;
import bkrc.chaptersofu.application.shared.event.EventPayload;
import bkrc.chaptersofu.application.email.EmailService;
import bkrc.chaptersofu.application.member.event.payload.MemberJoinEventPayload;
import bkrc.chaptersofu.application.member.event.payload.MemberModifyEventPayload;
import bkrc.chaptersofu.application.member.event.payload.MemberWithdrawEventPayload;
import bkrc.chaptersofu.application.shared.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {
    private final EmailService emailService;

    @RabbitListener(
        queues = {
                RabbitMQConfig.JOIN_QUEUE,
                RabbitMQConfig.MODIFY_QUEUE,
                RabbitMQConfig.WITHDRAW_QUEUE
        }
    )
    public void listen(String message) {
        Event<EventPayload> event = deserialize(message);
        if (event != null) {
            emailService.handleEvent(event);
        }
    }

    private Event<EventPayload> deserialize(String message) {
        EventType eventType = Event.typeFromJson(message);
        if (eventType == null) {
            return null;
        }

        return switch (eventType) {
            case MEMBER_JOIN -> Event.fromJson(message, MemberJoinEventPayload.class);
            case MEMBER_MODIFY -> Event.fromJson(message, MemberModifyEventPayload.class);
            case MEMBER_WITHDRAW -> Event.fromJson(message, MemberWithdrawEventPayload.class);
            default -> null;
        };
    }
}
