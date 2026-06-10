package com.bkrc.bkrcv3.adapter.eventhandler;

import com.bkrc.bkrcv3.required.EmailEventHandler;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.adapter.payload.MemberWithdrawEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MemberWithdrawEmailEventHandler implements EmailEventHandler<MemberWithdrawEventPayload> {

    @Override
    public void handle(Event<MemberWithdrawEventPayload> event) {
        MemberWithdrawEventPayload payload = event.getPayload();
        log.info("{}님이 회원 탈퇴하였습니다. 탈퇴일시 : {}", payload.getLoginId(), payload.getWithdrawnAt());
    }

    @Override
    public boolean supports(Event<MemberWithdrawEventPayload> event) {
        return event.getType() == EventType.MEMBER_WITHDRAW;
    }
}
