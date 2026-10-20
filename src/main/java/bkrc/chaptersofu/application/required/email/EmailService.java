package bkrc.chaptersofu.application.required.email;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.EventHandlerException;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventPayload;
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
                .orElseThrow(() -> new EventHandlerException(
                        ErrorCode.EVENT_HANDLER_NOT_FOUND,
                        "eventType=" + String.valueOf(event.getType())
                ));
    }
}
