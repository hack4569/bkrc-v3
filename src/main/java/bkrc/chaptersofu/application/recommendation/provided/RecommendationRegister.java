package bkrc.chaptersofu.application.recommendation.provided;

import bkrc.chaptersofu.domain.recommendation.BookRecommendation;

public interface RecommendationRegister {
    BookRecommendation create(Long memberId, CreateRecommendationRequest request);
    BookRecommendation update(Long memberId, Integer itemId, String content);
}
