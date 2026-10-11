package bkrc.chaptersofu.application.required;

import bkrc.chaptersofu.application.shared.event.EventPayload;
import bkrc.chaptersofu.application.shared.event.Event;

public interface HotBookEventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
    Integer findBookId(Event<T> event);
}
