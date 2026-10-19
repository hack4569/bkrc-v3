package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.aladin.AladinException;
import bkrc.chaptersofu.application.history.provided.HistoryFinder;
import bkrc.chaptersofu.application.history.provided.HistoryRegister;
import bkrc.chaptersofu.domain.history.History;
import bkrc.chaptersofu.application.aladin.provided.BookRecommender;
import bkrc.chaptersofu.application.aladin.provided.AladinBookCacheFinder;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookRecommendationService implements BookRecommender {

    private final AladinBookCacheFinder aladinBookCacheFinder;
    private final HistoryFinder historyFinder;
    private final HistoryRegister historyRegister;

    @Override
    public List<AladinBook> getRecommendBooksForUser(
            @Nullable Long memberId
    ) {
        List<History> histories = memberId == null
                ? null
                : historyFinder.getHistoryByMemberId(memberId);

        var books = aladinBookCacheFinder.findAll();
        if (books == null || books.isEmpty()) {
            throw new AladinException(ErrorCode.ALADIN_NOT_FOUND);
        }

        List<AladinBook> filteredBooks = filterForUser(books, histories);

        if (filteredBooks.isEmpty() && memberId != null) {
            historyRegister.deleteHistoryByMemberId(memberId);
            filteredBooks = books;
        }

        return filteredBooks;
    }

    private List<AladinBook> filterForUser(List<AladinBook> books, List<History> histories) {
        LocalDate today = LocalDate.now();
        return books.stream()
                .filter(book -> book.hasNotBeenViewedBefore(histories, today))
                .toList();
    }
}
