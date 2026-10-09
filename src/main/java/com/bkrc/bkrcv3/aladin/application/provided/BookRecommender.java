package com.bkrc.bkrcv3.aladin.application.provided;

import com.bkrc.bkrcv3.adapter.webapi.dto.RecommendView;
import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendForUserRequest;
import com.bkrc.bkrcv3.aladin.domain.AladinBook;

import java.util.List;

public interface BookRecommender {
    List<AladinBook> getRecommendBooksForUser(Long memberId);
}
