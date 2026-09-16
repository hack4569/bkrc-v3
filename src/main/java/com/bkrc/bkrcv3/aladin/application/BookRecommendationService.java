package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendForUserRequest;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.BookComment;
import com.bkrc.bkrcv3.common.constants.RcmdConst;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.history.application.HistoryService;
import com.bkrc.bkrcv3.history.entity.History;
import com.bkrc.bkrcv3.member.application.response.RecommendView;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookRecommendationService {

    private static final Map<String, Integer> COMMENT_TYPE_ORDER = Map.of(
            "phrase", 6,
            "description", 3,
            "descriptionInsight", 2,
            "aiRecommend", 4,
            "mdRecommend", 5,
            "user", 1,
            "toc", 7
    );

    private final AladinService aladinService;
    private final HistoryService historyService;

    public List<RecommendView> getRecommendBooksForUser(
            @Nullable Long memberId,
            AladinRecommendForUserRequest request
    ) {
        List<History> histories = memberId == null
                ? request.getHistories()
                : historyService.getHistoryByMemberId(memberId);

        var response = aladinService.findAll();
        if (response == null || response.getCount() == 0) {
            throw new BusinessException(ErrorCode.ALADIN_NOT_FOUND);
        }

        List<AladinBook> books = response.getAladinBookResponseList().stream()
                .map(AladinBook::toEntity)
                .toList();
        List<AladinBook> filteredBooks = filterForUser(books, histories);

        if (filteredBooks.isEmpty() && memberId != null) {
            historyService.deleteHistoryByMemberId(memberId);
            filteredBooks = books;
        }

        return filteredBooks.stream()
                .limit(RcmdConst.SHOW_BOOKS_COUNT)
                .map(this::toRecommendView)
                .toList();
    }

    private List<AladinBook> filterForUser(List<AladinBook> books, List<History> histories) {
        return books.stream()
                .filter(AladinBook.historyFilter(histories))
                .toList();
    }

    private RecommendView toRecommendView(AladinBook book) {
        return RecommendView.builder()
                .itemId(book.getItemId())
                .title(book.getTitle())
                .link(book.getLink())
                .cover(book.getCover())
                .recommendCommentList(sortBookComments(book.getBookCommentList()))
                .author(book.getAuthor())
                .categoryName(book.getCategoryName())
                .build();
    }

    private List<BookComment> sortBookComments(List<BookComment> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return List.of();
        }

        return comments.stream()
                .sorted(Comparator
                        .comparingInt((BookComment comment) ->
                                COMMENT_TYPE_ORDER.getOrDefault(comment.getType(), Integer.MAX_VALUE))
                        .thenComparing(
                                BookComment::getBookCommentId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                .toList();
    }
}
