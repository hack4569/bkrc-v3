package com.bkrc.bkrcv3.notification.service;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.payload.EventPayload;
import com.bkrc.bkrcv3.notification.eventHandler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final List<EventHandler> eventHandlers;
    public void handleEvent(Event<EventPayload> event) {
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        eventHandler.handle(event);
    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }
}
