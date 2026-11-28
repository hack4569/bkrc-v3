package bkrc.chaptersofu.domain.book;

import bkrc.chaptersofu.domain.history.History;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 추천 대상 필터링에 사용하는 도서 도메인 규칙을 검증한다.
 */
class BookTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 28);

    @Test
    void 허용_카테고리가_비어있으면_모든_도서를_허용한다() {
        Book book = BookFixture.book(1L, "제목", "저자", "소설", "2020-01-01");

        assertThat(book.isInAllowedCategories(null)).isTrue();
        assertThat(book.isInAllowedCategories(Set.of())).isTrue();
        assertThat(book.isInAllowedCategories(Set.of(2105))).isTrue();
        assertThat(book.isInAllowedCategories(Set.of(1))).isFalse();
    }

    @Test
    void 출간일이_1년보다_이전인지_판단하고_형식이_잘못되면_false를_반환한다() {
        assertThat(book("2024-01-01").wasPublishedBeforeOneYearAgo(TODAY)).isTrue();
        assertThat(book("2026-11-01").wasPublishedBeforeOneYearAgo(TODAY)).isFalse();
        // 외부 API가 비정상 날짜를 내려줘도 배치가 중단되지 않아야 한다.
        assertThat(book("2024").wasPublishedBeforeOneYearAgo(TODAY)).isFalse();
        assertThat(book(null).wasPublishedBeforeOneYearAgo(TODAY)).isFalse();
    }

    @Test
    void 오늘이_아닌_날_조회한_도서는_조회_이력이_있는_것으로_본다() {
        Book book = BookFixture.book(1L, "제목", "저자", "소설", "2020-01-01");
        History viewedYesterday = history(1L, TODAY.minusDays(1));
        History viewedToday = history(1L, TODAY);
        History otherBook = history(2L, TODAY.minusDays(1));

        assertThat(book.hasNotBeenViewedBefore(null, TODAY)).isTrue();
        assertThat(book.hasNotBeenViewedBefore(List.of(viewedToday, otherBook), TODAY)).isTrue();
        assertThat(book.hasNotBeenViewedBefore(List.of(viewedYesterday), TODAY)).isFalse();
    }

    @Test
    void 추천용_소개글은_두번째_전체소개를_우선한다() {
        assertThat(BookFixture.book(1L, "t", "a", "c", "p", null, "소개1", "소개2", null, null)
                .getDescForRecommend()).isEqualTo("소개2");
        assertThat(BookFixture.book(1L, "t", "a", "c", "p", null, "소개1", null, null, null)
                .getDescForRecommend()).isEqualTo("소개1");
        assertThat(BookFixture.book(1L, "t", "a", "c", "p", null, null, null, null, null)
                .getDescForRecommend()).isNull();
    }

    @Test
    void 기존_도서_ID를_복원한다() {
        Book book = BookFixture.book(null, "제목", "저자", "소설", "2020-01-01");

        book.restoreBookId(7L);

        assertThat(book.getBookId()).isEqualTo(7L);
    }

    private Book book(String pubDate) {
        return BookFixture.book(1L, "제목", "저자", "소설", pubDate);
    }

    private History history(Long bookId, LocalDate createdDate) {
        History history = History.create(bookId, 10L, bookId * 100);
        // 생성일은 JPA Auditing이 채우는 값이므로 단위 테스트에서는 직접 설정한다.
        ReflectionTestUtils.setField(history, "created", createdDate.atStartOfDay());
        return history;
    }
}
