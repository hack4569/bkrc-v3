package bkrc.chaptersofu.adapter.webapi;

import bkrc.chaptersofu.adapter.client.aladin.dto.RecommendView;
import bkrc.chaptersofu.application.aladin.provided.AladinFinder;
import bkrc.chaptersofu.application.aladin.provided.BookRecommender;
import bkrc.chaptersofu.adapter.client.aladin.dto.AladinBookSearchResponse;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.common.constants.RcmdConst;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AladinApi {
    private final AladinFinder aladinFinder;
    private final BookRecommender bookRecommender;

    @GetMapping("/v1/aladin/books/recommend/user")
    public List<RecommendView> getRecommendBooksForUser(
            @AuthenticationPrincipal Long memberId) {
        var filteredBooks = bookRecommender.getRecommendBooksForUser(memberId);
        return filteredBooks.stream()
                .limit(RcmdConst.SHOW_BOOKS_COUNT)
                .map(RecommendView::from)
                .toList();
    }

    @GetMapping("/v1/aladin/books/search")
    public List<AladinBookSearchResponse> searchBooks(@RequestParam String query) {
        List<AladinBook> books = query == null || query.isBlank() ? List.of() : aladinFinder.searchBooks(query);
        return books.stream()
                .map(AladinBookSearchResponse::from)
                .toList();
    }
}
