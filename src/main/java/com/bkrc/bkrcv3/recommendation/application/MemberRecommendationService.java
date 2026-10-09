package com.bkrc.bkrcv3.recommendation.application;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.exception.MemberNotFoundException;
import com.bkrc.bkrcv3.member.application.MemberRepository;
import com.bkrc.bkrcv3.member.domain.Member;
import com.bkrc.bkrcv3.recommendation.application.request.CreateRecommendationRequest;
import com.bkrc.bkrcv3.recommendation.domain.BookRecommendation;
import com.bkrc.bkrcv3.recommendation.domain.RecommendationException;
import com.bkrc.bkrcv3.recommendation.application.provided.RecommendationFinder;
import com.bkrc.bkrcv3.recommendation.application.provided.RecommendationRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberRecommendationService implements RecommendationFinder, RecommendationRegister {
    private final RecommendationRepository recommendationRepository;
    private final MemberRepository memberRepository;
    private final Snowflake snowflake;

    @Transactional(readOnly = true)
    @Override
    public List<BookRecommendation> getMyRecommendations(Long memberId) {
        return recommendationRepository.findByMemberMemberIdOrderByCreatedDesc(memberId);
    }

    @Transactional
    @Override
    public BookRecommendation create(Long memberId, CreateRecommendationRequest request) {
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        var saved = recommendationRepository.save(BookRecommendation.create(snowflake.nextId(), request.itemId(),
                request.cover(), request.title(), request.link(), request.recommendation(), member));
        return saved;
    }

    @Transactional(readOnly = true)
    @Override
    public BookRecommendation get(Long memberId, Integer itemId) {
        return recommendationRepository.findByItemIdAndMemberMemberId(itemId, memberId)
                .orElseThrow(() -> new RecommendationException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Transactional
    @Override
    public BookRecommendation update(Long memberId, Integer itemId, String content) {
        var value = recommendationRepository.findByItemIdAndMemberMemberId(itemId, memberId)
                .orElseThrow(() -> new RecommendationException(ErrorCode.BOOK_NOT_FOUND));
        if (!"N".equals(value.getApproved())) {
            throw new RecommendationException(ErrorCode.RECOMMENDATION_NOT_EDITABLE);
        }
        value.updateRecommendation(content);
        return value;
    }
}
