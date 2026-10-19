package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.application.aladin.provided.AladinFinder;
import bkrc.chaptersofu.application.history.provided.HistoryFinder;
import bkrc.chaptersofu.application.history.provided.HistoryRegister;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.AladinException;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookRecommendationServiceTest {
    @Mock AladinFinder aladinFinder;
    @Mock HistoryFinder historyFinder;
    @Mock HistoryRegister historyRegister;
    @InjectMocks BookRecommendationService service;

    @Test
    void 비회원에게는_전체_도서를_추천한다() {
        AladinBook firstBook = mock(AladinBook.class);
        AladinBook secondBook = mock(AladinBook.class);
        when(firstBook.hasNotBeenViewedBefore(isNull(), any(LocalDate.class))).thenReturn(true);
        when(secondBook.hasNotBeenViewedBefore(isNull(), any(LocalDate.class))).thenReturn(true);
        List<AladinBook> books = List.of(firstBook, secondBook);
        when(aladinFinder.findAll()).thenReturn(books);

        assertThat(service.getRecommendBooksForUser(null)).containsExactlyElementsOf(books);
        verifyNoInteractions(historyFinder, historyRegister);
    }

    @Test
    void 추천할_도서가_없으면_예외가_발생한다() {
        when(historyFinder.getHistoryByMemberId(1L)).thenReturn(List.of());
        when(aladinFinder.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.getRecommendBooksForUser(1L))
                .isInstanceOf(AladinException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALADIN_NOT_FOUND);
    }

    @Test
    void 회원에게는_과거에_조회하지_않은_도서만_추천한다() {
        AladinBook viewedBook = mock(AladinBook.class);
        AladinBook newBook = mock(AladinBook.class);
        var history = mock(bkrc.chaptersofu.domain.history.History.class);
        List<bkrc.chaptersofu.domain.history.History> histories = List.of(history);
        when(viewedBook.hasNotBeenViewedBefore(eq(histories), any(LocalDate.class))).thenReturn(false);
        when(newBook.hasNotBeenViewedBefore(eq(histories), any(LocalDate.class))).thenReturn(true);
        when(historyFinder.getHistoryByMemberId(10L)).thenReturn(histories);
        when(aladinFinder.findAll()).thenReturn(List.of(viewedBook, newBook));

        assertThat(service.getRecommendBooksForUser(10L)).containsExactly(newBook);
        verify(historyRegister, never()).deleteHistoryByMemberId(anyLong());
    }

    @Test
    void 모든_도서를_조회한_회원은_이력을_삭제하고_전체_도서를_추천한다() {
        AladinBook book = mock(AladinBook.class);
        var history = mock(bkrc.chaptersofu.domain.history.History.class);
        List<bkrc.chaptersofu.domain.history.History> histories = List.of(history);
        when(book.hasNotBeenViewedBefore(eq(histories), any(LocalDate.class))).thenReturn(false);
        when(historyFinder.getHistoryByMemberId(10L)).thenReturn(histories);
        when(aladinFinder.findAll()).thenReturn(List.of(book));

        assertThat(service.getRecommendBooksForUser(10L)).containsExactly(book);
        verify(historyRegister).deleteHistoryByMemberId(10L);
    }
}
