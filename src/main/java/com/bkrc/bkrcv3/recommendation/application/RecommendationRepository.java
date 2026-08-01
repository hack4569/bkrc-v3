package com.bkrc.bkrcv3.recommendation.application;

import com.bkrc.bkrcv3.recommendation.entity.BookRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<BookRecommendation, Long> {
    List<BookRecommendation> findByMemberMemberIdOrderByCreatedDesc(Long memberId);
    Optional<BookRecommendation> findByItemIdAndMemberMemberId(Integer itemId, Long memberId);
}
