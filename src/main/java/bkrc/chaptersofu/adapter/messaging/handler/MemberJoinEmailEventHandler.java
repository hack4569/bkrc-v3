package bkrc.chaptersofu.adapter.messaging.handler;

import bkrc.chaptersofu.application.required.EmailEventHandler;
import bkrc.chaptersofu.application.shared.event.Event;
import bkrc.chaptersofu.application.shared.event.EventType;
import bkrc.chaptersofu.application.member.event.payload.MemberJoinEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MemberJoinEmailEventHandler implements EmailEventHandler<MemberJoinEventPayload> {

    @Override
    public void handle(Event<MemberJoinEventPayload> event) {
        MemberJoinEventPayload payload = event.getPayload();
        log.info("{}님 가입을 환영합니다. 가입날짜 : {}", payload.getLoginId(), payload.getCreated());
    }

    @Override
    public boolean supports(Event<MemberJoinEventPayload> event) {
        return event.getType() == EventType.MEMBER_JOIN;
    }
}
