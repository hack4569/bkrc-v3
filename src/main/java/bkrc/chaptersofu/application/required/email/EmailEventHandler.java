package bkrc.chaptersofu.application.required.email;

import bkrc.chaptersofu.application.required.event.EventPayload;
import bkrc.chaptersofu.application.required.event.Event;

public interface EmailEventHandler<T extends EventPayload > {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
