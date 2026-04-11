package com.bkrc.bkrcv3.required;

import com.bkrc.bkrcv3.common.event.Event;

public interface HotBookEventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
    Integer findBookId(Event<T> event);
}
