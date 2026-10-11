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
        List<AladinBook> books = List.of(mock(AladinBook.class), mock(AladinBook.class));
        when(aladinFinder.findAll()).thenReturn(books);

        assertThat(service.getRecommendBooksForUser(null)).containsExactlyElementsOf(books);
        verifyNoInteractions(historyFinder, historyRegister);
    }

    @Test
    void 추천할_도서가_없으면_예외가_발생한다() {
        when(aladinFinder.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.getRecommendBooksForUser(1L))
                .isInstanceOf(AladinException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALADIN_NOT_FOUND);
    }
}
