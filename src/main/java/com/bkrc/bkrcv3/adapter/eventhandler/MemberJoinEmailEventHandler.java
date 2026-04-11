package com.bkrc.bkrcv3.adapter.eventhandler;

import com.bkrc.bkrcv3.required.EmailEventHandler;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.adapter.payload.MemberJoinEventPayload;
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
