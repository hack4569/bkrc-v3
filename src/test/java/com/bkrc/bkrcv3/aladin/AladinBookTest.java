package com.bkrc.bkrcv3.aladin;

import com.bkrc.bkrcv3.adapter.gpt.Gpt;
import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.CategoryService;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.aladin.entity.QueryType;
import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.member.MemberTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
class AladinBookTest {

    private static final Snowflake SNOWFLAKE = new Snowflake();

    private String loginId;
    private Integer targetItemId;
    private List<AladinBook> aladinBookList = new ArrayList<>();

    @BeforeEach
    void 알라딘api_책정보_목록_조회(
            @Autowired MemberTestFixture fixture,
            @Autowired AladinService aladinService
    ) {
        int page = 1;
        this.aladinBookList = aladinService.getAladinItemList(
                AladinRequest.builder()
                        .querytype(QueryType.BEST_SELLER.getQueryType())
                        .maxResults(AladinConstants.ITEM_LIST_PAGE)
                        .start(page).build()
        );
    }

    @Test
    void 알라딘_책목록이_허용된_카테고리_리스트에_포함되어야_함(
            @Autowired Gpt gpt,
            @Autowired CategoryService categoryService
    ) {
        // Arrange
        Set<Integer> allowedCategoryIds = Set.of(3, 17, 116, 50922);
        // Act
        var allowedAladinBookList = aladinBookList.stream()
                .filter(aladinBook -> aladinBook.isInAllowedCategories(allowedCategoryIds))
                .toList();
        // Assert
        allowedAladinBookList.forEach(book -> assertThat(allowedCategoryIds).contains(book.getCategoryId()));
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private AladinBook bookWithPubDate(String pubDate) {
        AladinBookResponse response = new AladinBookResponse();
        response.setPubDate(pubDate);
        return AladinBook.toEntity(response);
    }

    static Stream<Arguments> isPublishedAfterCases() {
        return Stream.of(
                Arguments.of("null이면 false",          null,                                                                  false),
                Arguments.of("빈 문자열이면 false",      "",                                                                   false),
                Arguments.of("정확히 1년 전이면 false",  LocalDate.now().minusYears(1).format(DATE_FORMATTER),                 false),
                Arguments.of("1년 1일 이전이면 true",    LocalDate.now().minusYears(1).minusDays(1).format(DATE_FORMATTER),    true),
                Arguments.of("2년 전이면 true",          LocalDate.now().minusYears(2).format(DATE_FORMATTER),                 true),
                Arguments.of("6개월 전이면 false",       LocalDate.now().minusMonths(6).format(DATE_FORMATTER),                false),
                Arguments.of("잘못된 형식이면 false",    "invalid-date",                                                       false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("isPublishedAfterCases")
    void publishDateFilter_테스트(String description, String pubDate, boolean expected) {
        assertThat(bookWithPubDate(pubDate).publishDateFilter()).satisfies(result ->
                assertThat(result).isEqualTo(expected)
        );
    }

//    @Test
//    void historyFilter_히스토리에_있는_책은_결과에서_제외된다(
//            @Autowired HistoryRepository historyRepository,
//            @Autowired HistoryService historyService,
//            @Autowired MemberTestFixture fixture
//    ) {
//        // Arrange
//        targetItemId = aladinBookList.get(0).getItemId();
//        loginId = GeneratorForTest.generateLoginId();
//
//        // 1. loginId, targetItemId로 히스토리 저장 후 created를 어제로 수정
//        // (historyFilter는 created가 오늘이 아닌 날짜일 때만 해당 책을 필터링)
//        History history = History.builder()
//                .id(SNOWFLAKE.nextId())
//                .loginId(loginId)
//                .itemId(targetItemId)
//                .build();
//        var saved = historyRepository.saveAndFlush(history);
//        saved.setCreated(LocalDateTime.now().minusDays(1));
//        historyRepository.saveAndFlush(saved);
//
//        // 2. loginId로 히스토리 조회
//        List<History> historyList = historyService.getHistoryByLoginId(loginId);
//        assertThat(historyList).isNotEmpty();
//
//        //Act
//        // 3. historyFilter로 필터링 → targetItemId를 가진 책이 제외되는지 검증
//        List<AladinBook> filtered = aladinBookList.stream()
//                .filter(AladinBook.historyFilter(historyList))
//                .toList();
//
//        // Assert
//        assertThat(filtered)
//                .extracting(AladinBook::getItemId)
//                .doesNotContain(targetItemId);
//    }
}
