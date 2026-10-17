package bkrc.chaptersofu.domain.aladin;

import bkrc.chaptersofu.domain.history.History;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AladinBookTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 10, 17);

    static Stream<Arguments> publicationDateCases() {
        return Stream.of(
                Arguments.of("날짜가 null이면 대상이 아니다", null, false),
                Arguments.of("날짜가 비어 있으면 대상이 아니다", "", false),
                Arguments.of("정확히 1년 전이면 대상이 아니다", "2025-10-17", false),
                Arguments.of("1년 1일 전이면 대상이다", "2025-10-16", true),
                Arguments.of("2년 전이면 대상이다", "2024-10-17", true),
                Arguments.of("6개월 전이면 대상이 아니다", "2026-04-17", false),
                Arguments.of("잘못된 형식이면 대상이 아니다", "invalid-date", false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("publicationDateCases")
    void 기준일로부터_1년보다_이전에_출간되었는지_판단한다(
            String description,
            String publicationDate,
            boolean expected
    ) {
        AladinBook book = book(1, publicationDate);

        assertThat(book.wasPublishedBeforeOneYearAgo(REFERENCE_DATE)).isEqualTo(expected);
    }

    @Test
    void 기준일_이전에_조회한_도서는_추천하지_않는다() {
        AladinBook book = book(1, "2024-01-01");
        History history = history(1, REFERENCE_DATE.minusDays(1).atStartOfDay());

        assertThat(book.hasNotBeenViewedBefore(List.of(history), REFERENCE_DATE)).isFalse();
    }

    @Test
    void 기준일에_조회한_도서는_추천할_수_있다() {
        AladinBook book = book(1, "2024-01-01");
        History history = history(1, REFERENCE_DATE.atStartOfDay());

        assertThat(book.hasNotBeenViewedBefore(List.of(history), REFERENCE_DATE)).isTrue();
    }

    @Test
    void 다른_도서의_조회_이력은_추천에_영향을_주지_않는다() {
        AladinBook book = book(1, "2024-01-01");
        History history = history(2, REFERENCE_DATE.minusDays(1).atStartOfDay());

        assertThat(book.hasNotBeenViewedBefore(List.of(history), REFERENCE_DATE)).isTrue();
    }

    @Test
    void 조회_이력이_없으면_추천할_수_있다() {
        AladinBook book = book(1, "2024-01-01");

        assertThat(book.hasNotBeenViewedBefore(List.of(), REFERENCE_DATE)).isTrue();
    }

    private AladinBook book(Integer itemId, String publicationDate) {
        AladinBookResponse response = new AladinBookResponse();
        response.setItemId(itemId);
        response.setPubDate(publicationDate);
        return AladinBook.toEntity(response);
    }

    private History history(Integer itemId, LocalDateTime createdAt) {
        History history = History.builder()
                .itemId(itemId)
                .memberId(1L)
                .build();
        history.setCreated(createdAt);
        return history;
    }
}
