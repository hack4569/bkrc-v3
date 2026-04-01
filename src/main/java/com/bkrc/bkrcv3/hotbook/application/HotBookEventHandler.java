package com.bkrc.bkrcv3.hotbook.application;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.payload.EventPayload;

public interface HotBookEventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
    Integer findBookId(Event<T> event);
}
