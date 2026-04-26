package com.bkrc.bkrcv3.email.application;

import com.bkrc.bkrcv3.adapter.eventhandler.MemberJoinEmailEventHandler;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.adapter.payload.MemberJoinEventPayload;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.required.EventPayload;
import com.bkrc.bkrcv3.required.EmailEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final List<EmailEventHandler> emailEventHandlers;
    public void handleEvent(Event<EventPayload> event) {
        EmailEventHandler<EventPayload> emailEventHandler = findEventHandler(event);
        emailEventHandler.handle(event);
    }

    private EmailEventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return emailEventHandlers.stream()
                .filter(emailEventHandler -> emailEventHandler.supports(event))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_HANDLER_NOT_FOUND));
    }
}
