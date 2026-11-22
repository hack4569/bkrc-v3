package bkrc.chaptersofu.application.bkrc;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookException;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.application.history.provided.HistoryFinder;
import bkrc.chaptersofu.application.history.provided.HistoryRegister;
import bkrc.chaptersofu.domain.history.History;
import bkrc.chaptersofu.application.aladin.provided.BookRecommender;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookRecommendationService implements BookRecommender {

    private final BookCacheFinder bookCacheFinder;
    private final HistoryFinder historyFinder;
    private final HistoryRegister historyRegister;

    @Override
    public List<Book> getRecommendBooksForUser(
            @Nullable Long memberId
    ) {
        List<History> histories = memberId == null
                ? null
                : historyFinder.getHistoryByMemberId(memberId);

        var books = bookCacheFinder.findAll();
        if (books == null || books.isEmpty()) {
            throw new BookException(ErrorCode.BOOK_NOT_FOUND, "추천할 책이 없습니다.");
        }

        List<Book> filteredBooks = filterForUser(books, histories);

        if (filteredBooks.isEmpty() && memberId != null) {
            historyRegister.deleteHistoryByMemberId(memberId);
            filteredBooks = books;
        }

        return filteredBooks;
    }

    private List<Book> filterForUser(List<Book> books, List<History> histories) {
        LocalDate today = LocalDate.now();
        return books.stream()
                .filter(book -> book.hasNotBeenViewedBefore(histories, today))
                .toList();
    }
}
