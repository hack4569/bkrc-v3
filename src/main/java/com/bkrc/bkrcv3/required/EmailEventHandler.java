package com.bkrc.bkrcv3.required;

import com.bkrc.bkrcv3.common.event.Event;

public interface EmailEventHandler<T extends EventPayload > {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
