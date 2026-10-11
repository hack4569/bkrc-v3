package bkrc.chaptersofu.application.email;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.shared.event.EventHandlerException;
import bkrc.chaptersofu.domain.shared.event.Event;
import bkrc.chaptersofu.application.required.EventPayload;
import bkrc.chaptersofu.application.required.EmailEventHandler;
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
                .orElseThrow(() -> new EventHandlerException(ErrorCode.EVENT_HANDLER_NOT_FOUND));
    }
}
