package com.bkrc.bkrcv3.adapter.eventhandler;

import com.bkrc.bkrcv3.required.EmailEventHandler;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.adapter.payload.MemberModifyEventPayload;
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
