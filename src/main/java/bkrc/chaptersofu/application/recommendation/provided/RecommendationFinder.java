package bkrc.chaptersofu.application.recommendation.provided;

import bkrc.chaptersofu.domain.recommendation.BookRecommendation;

import java.util.List;

public interface RecommendationFinder {
    List<BookRecommendation> getMyRecommendations(Long memberId);
    BookRecommendation get(Long memberId, Integer itemId);
}
