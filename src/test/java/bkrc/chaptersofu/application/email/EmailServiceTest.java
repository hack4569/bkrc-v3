package bkrc.chaptersofu.application.email;

import bkrc.chaptersofu.application.required.EmailEventHandler;
import bkrc.chaptersofu.application.shared.event.Event;
import bkrc.chaptersofu.application.shared.event.EventPayload;
import bkrc.chaptersofu.application.shared.event.EventType;
import bkrc.chaptersofu.domain.common.shared.EventHandlerException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    @SuppressWarnings("unchecked")
    @Test
    void 지원하는_핸들러에_이벤트를_전달한다() {
        EmailEventHandler<EventPayload> unsupported = mock(EmailEventHandler.class);
        EmailEventHandler<EventPayload> supported = mock(EmailEventHandler.class);
        Event<EventPayload> event = Event.of(EventType.MEMBER_JOIN, mock(EventPayload.class));
        when(unsupported.supports(event)).thenReturn(false);
        when(supported.supports(event)).thenReturn(true);

        new EmailService(List.of(unsupported, supported)).handleEvent(event);

        verify(supported).handle(event);
        verify(unsupported, never()).handle(event);
    }

    @Test
    void 지원하는_핸들러가_없으면_예외가_발생한다() {
        Event<EventPayload> event = Event.of(EventType.MEMBER_JOIN, mock(EventPayload.class));
        EmailService service = new EmailService(List.of());

        assertThatThrownBy(() -> service.handleEvent(event))
                .isInstanceOf(EventHandlerException.class);
    }
}
