package bkrc.chaptersofu.application.recommendation;

import bkrc.chaptersofu.application.recommendation.provided.CreateRecommendationRequest;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.domain.member.MemberNotFoundException;
import bkrc.chaptersofu.application.member.MemberRepository;
import bkrc.chaptersofu.domain.recommendation.BookRecommendation;
import bkrc.chaptersofu.domain.recommendation.RecommendationException;
import bkrc.chaptersofu.application.recommendation.provided.RecommendationFinder;
import bkrc.chaptersofu.application.recommendation.provided.RecommendationRegister;
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
                .orElseThrow(() -> new RecommendationException(
                        ErrorCode.BOOK_NOT_FOUND,
                        "bookId=" + itemId + ", memberId=" + memberId
                ));
    }

    @Transactional
    @Override
    public BookRecommendation update(Long memberId, Integer itemId, String content) {
        var value = recommendationRepository.findByItemIdAndMemberMemberId(itemId, memberId)
                .orElseThrow(() -> new RecommendationException(
                        ErrorCode.BOOK_NOT_FOUND,
                        "bookId=" + itemId + ", memberId=" + memberId
                ));
        if (!"N".equals(value.getApproved())) {
            throw new RecommendationException(
                    ErrorCode.RECOMMENDATION_NOT_EDITABLE,
                    "bookId=" + itemId + ", memberId=" + memberId + ", approved=" + value.getApproved()
            );
        }
        value.updateRecommendation(content);
        return value;
    }
}
