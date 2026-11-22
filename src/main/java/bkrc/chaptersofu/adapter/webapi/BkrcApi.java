package bkrc.chaptersofu.adapter.webapi;

import bkrc.chaptersofu.adapter.client.aladin.dto.RecommendView;
import bkrc.chaptersofu.adapter.webapi.dto.BookSearchResponse;
import bkrc.chaptersofu.application.aladin.provided.BookRecommender;
import bkrc.chaptersofu.adapter.client.aladin.dto.AladinBookSearchResponse;
import bkrc.chaptersofu.application.bkrc.provided.BkrcFinder;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.common.constants.RcmdConst;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BkrcApi {
    private final BookRecommender bookRecommender;
    private final BkrcFinder bkrcFinder;

    @GetMapping("/v1/books/recommend/user")
    public List<RecommendView> getRecommendBooksForUser(
            @AuthenticationPrincipal Long memberId) {
        var filteredBooks = bookRecommender.getRecommendBooksForUser(memberId);
        return filteredBooks.stream()
                .limit(RcmdConst.SHOW_BOOKS_COUNT)
                .map(RecommendView::from)
                .toList();
    }

    @GetMapping("/v1/books/search")
    public List<BookSearchResponse> searchBooks(@RequestParam String query) {
        List<Book> books = query == null || query.isBlank() ? List.of() : bkrcFinder.searchBooks(query);
        return books.stream()
                .map(BookSearchResponse::from)
                .toList();
    }
}
