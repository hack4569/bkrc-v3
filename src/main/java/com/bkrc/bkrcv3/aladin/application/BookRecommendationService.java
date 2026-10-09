package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendForUserRequest;
import com.bkrc.bkrcv3.aladin.domain.AladinBook;
import com.bkrc.bkrcv3.aladin.domain.BookComment;
import com.bkrc.bkrcv3.common.constants.RcmdConst;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.aladin.domain.AladinException;
import com.bkrc.bkrcv3.history.application.provided.HistoryFinder;
import com.bkrc.bkrcv3.history.application.provided.HistoryRegister;
import com.bkrc.bkrcv3.history.domain.History;
import com.bkrc.bkrcv3.adapter.webapi.dto.RecommendView;
import com.bkrc.bkrcv3.aladin.application.provided.BookRecommender;
import com.bkrc.bkrcv3.aladin.application.provided.AladinFinder;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookRecommendationService implements BookRecommender {

    private final AladinFinder aladinFinder;
    private final HistoryFinder historyFinder;
    private final HistoryRegister historyRegister;

    @Override
    public List<AladinBook> getRecommendBooksForUser(
            @Nullable Long memberId
    ) {
        List<History> histories = memberId == null
                ? null
                : historyFinder.getHistoryByMemberId(memberId);

        var response = aladinFinder.findAll();
        if (response == null || response.getCount() == 0) {
            throw new AladinException(ErrorCode.ALADIN_NOT_FOUND);
        }

        List<AladinBook> books = response.getAladinBookResponseList().stream()
                .map(AladinBook::toEntity)
                .toList();
        List<AladinBook> filteredBooks = filterForUser(books, histories);

        if (filteredBooks.isEmpty() && memberId != null) {
            historyRegister.deleteHistoryByMemberId(memberId);
            filteredBooks = books;
        }

        return filteredBooks;
    }

    private List<AladinBook> filterForUser(List<AladinBook> books, List<History> histories) {
        return books.stream()
                .filter(AladinBook.historyFilter(histories))
                .toList();
    }
}
