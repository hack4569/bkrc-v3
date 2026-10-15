package bkrc.chaptersofu.adapter.messaging.handler;

import bkrc.chaptersofu.application.required.email.EmailEventHandler;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.application.required.event.payload.MemberModifyEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MemberModifyEmailEventHandler implements EmailEventHandler<MemberModifyEventPayload> {

    @Override
    public void handle(Event<MemberModifyEventPayload> event) {
        MemberModifyEventPayload payload = event.getPayload();
        log.info("{}님의 회원수정이 완료되었습니다. 수정날짜 : {}", payload.getLoginId(), payload.getUpdated());
    }

    @Override
    public boolean supports(Event<MemberModifyEventPayload> event) {
        return event.getType() == EventType.MEMBER_MODIFY;
    }
}
