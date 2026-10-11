package bkrc.chaptersofu.application.hotbook;

import bkrc.chaptersofu.application.aladin.provided.AladinFinder;
import bkrc.chaptersofu.application.required.HotBookEventHandler;
import bkrc.chaptersofu.application.shared.event.Event;
import bkrc.chaptersofu.application.shared.event.EventPayload;
import bkrc.chaptersofu.application.shared.event.EventType;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.common.shared.EventHandlerException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HotBookServiceTest {
    @SuppressWarnings("unchecked")
    @Test
    void 이벤트를_처리하고_핫북_점수를_저장한다() {
        HotBookEventHandler<EventPayload> handler = mock(HotBookEventHandler.class);
        HotBookCalculator calculator = mock(HotBookCalculator.class);
        HotBookRepository repository = mock(HotBookRepository.class);
        AladinFinder finder = mock(AladinFinder.class);
        Event<EventPayload> event = Event.of(EventType.BOOK_LIKE, mock(EventPayload.class));
        when(handler.supports(event)).thenReturn(true);
        when(handler.findBookId(event)).thenReturn(12);
        when(calculator.calculate(12)).thenReturn(9L);

        new HotBookService(List.of(handler), calculator, repository, finder).handleEvent(event);

        verify(handler).handle(event);
        verify(repository).add(eq(12), any(LocalDateTime.class), eq(9L), eq(10L), eq(Duration.ofDays(10)));
    }

    @Test
    void 핫북_ID를_도서로_변환하고_없는_도서는_제외한다() {
        HotBookRepository repository = mock(HotBookRepository.class);
        AladinFinder finder = mock(AladinFinder.class);
        AladinBook book = mock(AladinBook.class);
        when(repository.readAll("20261011")).thenReturn(List.of(1, 2));
        when(finder.getAladinBook(1)).thenReturn(book);
        when(finder.getAladinBook(2)).thenReturn(null);
        HotBookService service = new HotBookService(List.of(), mock(HotBookCalculator.class), repository, finder);

        assertThat(service.readAll("20261011")).containsExactly(book);
    }

    @Test
    void 이벤트_핸들러가_없으면_예외가_발생한다() {
        HotBookService service = new HotBookService(List.of(), mock(HotBookCalculator.class),
                mock(HotBookRepository.class), mock(AladinFinder.class));
        Event<EventPayload> event = Event.of(EventType.BOOK_LIKE, mock(EventPayload.class));

        assertThatThrownBy(() -> service.handleEvent(event)).isInstanceOf(EventHandlerException.class);
    }
}
