package com.bkrc.bkrcv3.outbox.eventHandler;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.payload.EventPayload;

public interface EventHandler<T extends EventPayload > {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
