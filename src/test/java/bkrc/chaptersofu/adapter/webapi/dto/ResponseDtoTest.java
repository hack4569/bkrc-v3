package bkrc.chaptersofu.adapter.webapi.dto;

import bkrc.chaptersofu.adapter.client.aladin.dto.RecommendView;
import bkrc.chaptersofu.domain.aladin.BookComment;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookFixture;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
import bkrc.chaptersofu.domain.like.Like;
import bkrc.chaptersofu.domain.recommendation.BookRecommendation;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 엔티티를 API 응답으로 변환할 때 필드가 누락되거나 뒤바뀌지 않는지 검증한다.
 */
class ResponseDtoTest {
    private final Book book = BookFixture.book(1L, "데미안", "헤세", "국내도서>소설", "2020-01-01");

    @Test
    void 도서_응답은_엔티티_필드를_그대로_복사한다() {
        BookResponse response = BookResponse.from(book);
        BookSearchResponse search = BookSearchResponse.from(book);

        assertThat(response.bookId()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("데미안");
        assertThat(response.mallName()).isEqualTo(book.getMallName());
        assertThat(response.categoryName()).isEqualTo("국내도서>소설");
        assertThat(search).isEqualTo(new BookSearchResponse(1L, book.getItemId(), book.getMallName(),
                "데미안", "헤세", book.getCover(), book.getLink()));
    }

    @Test
    void 추천_뷰는_저자와_카테고리를_축약하고_코멘트를_정해진_순서로_정렬한다() {
        BookComment toc = comment(1L, "toc");
        BookComment user = comment(2L, "user");
        BookComment phraseLater = comment(5L, "phrase");
        BookComment phraseEarlier = comment(3L, "phrase");
        BookComment unknown = comment(4L, "unknown");
        Book target = BookFixture.book(1L, "데미안", "헤세,옮긴이,감수", "국내도서>소설>독일", "2020-01-01",
                null, null, null, null, List.of(toc, user, phraseLater, phraseEarlier, unknown));

        RecommendView view = RecommendView.from(target);

        assertThat(view.getAuthor()).isEqualTo("헤세 외 2명");
        assertThat(view.getCategoryName()).isEqualTo("소설");
        // 정의되지 않은 타입은 마지막에 두고, 같은 타입은 ID 순으로 정렬해야 화면 순서가 안정적이다.
        assertThat(view.getRecommendCommentList())
                .containsExactly(user, phraseEarlier, phraseLater, toc, unknown);
    }

    @Test
    void 추천_뷰는_구분자가_없으면_원본_값을_사용한다() {
        RecommendView view = RecommendView.from(BookFixture.book(1L, "제목", "헤세", "소설", "2020-01-01"));

        assertThat(view.getAuthor()).isEqualTo("헤세");
        assertThat(view.getCategoryName()).isEqualTo("소설");
        assertThat(view.getRecommendCommentList()).isEmpty();
    }

    @Test
    void 쿠폰_응답은_남은_재고와_발급_정보를_계산한다() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 28, 12, 0);
        Coupon coupon = Coupon.create("쿠폰", now, now.plusDays(1), now, now.plusDays(1), 10);
        ReflectionTestUtils.setField(coupon, "couponId", 1L);
        MemberCoupon memberCoupon = MemberCoupon.issue(100L, 1L, 10L, now);

        CouponResponse response = CouponResponse.from(coupon);
        CouponResponse.Issued issued = CouponResponse.Issued.from(memberCoupon, coupon);

        assertThat(response.remainingStock()).isEqualTo(10);
        assertThat(response.name()).isEqualTo("쿠폰");
        assertThat(issued.memberCouponId()).isEqualTo(100L);
        assertThat(issued.couponId()).isEqualTo(1L);
        assertThat(issued.issuedAt()).isEqualTo(now);
        assertThat(issued.usedAt()).isNull();
    }

    @Test
    void 좋아요와_추천_응답은_연관_도서_정보를_포함한다() {
        MyLikeResponse like = MyLikeResponse.of(Like.create(9L, book, null));
        MyRecommendationResponse recommendation = MyRecommendationResponse.from(
                BookRecommendation.create(3L, 1L, "cover", "데미안", "link", "추천합니다", null));

        assertThat(like).isEqualTo(new MyLikeResponse(9L, 1L, book.getItemId(), "데미안", book.getCover(),
                "헤세", book.getPublisher(), book.getLink()));
        // 신규 추천은 관리자 검토 전이므로 승인 대기(W) 상태로 응답해야 한다.
        assertThat(recommendation).isEqualTo(
                new MyRecommendationResponse(1L, "cover", "데미안", "link", "W", "추천합니다"));
    }

    private BookComment comment(Long id, String type) {
        BookComment comment = BookComment.create("내용" + id, type);
        comment.setBookCommentId(id);
        return comment;
    }
}
