package com.bkrc.bkrcv3.adapter.webapi;

import com.bkrc.bkrcv3.adapter.webapi.dto.RecommendView;
import com.bkrc.bkrcv3.aladin.application.provided.AladinFinder;
import com.bkrc.bkrcv3.aladin.application.provided.BookRecommender;
import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendForUserRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookPageResponse;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookSearchResponse;
import com.bkrc.bkrcv3.aladin.domain.AladinBook;
import com.bkrc.bkrcv3.common.constants.RcmdConst;
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

    @GetMapping("/v1/aladin/books")
    public AladinBookPageResponse getAllBooks() {
        return aladinFinder.findAll();
    }

    @GetMapping("/v1/aladin/books/search")
    public List<AladinBookSearchResponse> searchBooks(@RequestParam String query) {
        List<AladinBook> books = query == null || query.isBlank() ? List.of() : aladinFinder.searchBooks(query);
        return books.stream()
                .map(AladinBookSearchResponse::from)
                .toList();
    }
}
