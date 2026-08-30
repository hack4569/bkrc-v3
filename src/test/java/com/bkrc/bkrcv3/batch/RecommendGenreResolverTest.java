package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.entity.Category;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendGenreResolverTest {

    private final RecommendGenreResolver resolver = new RecommendGenreResolver();

    @Test
    void depth1PathToRepresentativeGenre() {
        Map<String, RecommendGenre> cases = Map.of(
                "소설/시/희곡", RecommendGenre.LITERATURE,
                "경제경영", RecommendGenre.BUSINESS,
                "역사", RecommendGenre.HISTORY_BIOGRAPHY,
                "에세이", RecommendGenre.ESSAY,
                "일본 도서", RecommendGenre.FOREIGN_BOOKS,
                "컴퓨터/모바일", RecommendGenre.COMPUTER
        );

        cases.forEach((depth1, expected) ->
                assertThat(resolver.resolve(category(depth1))).isEqualTo(expected));
    }

    @Test
    void unknownOrEmptyCategoryBecomesEtc() {
        assertThat(resolver.resolve(category("새로운 분류"))).isEqualTo(RecommendGenre.ETC);
        assertThat(resolver.resolve(null)).isEqualTo(RecommendGenre.ETC);
    }

    @Test
    void allDepth1ValuesInCategoryTableAreMapped() {
        Set<String> depth1Values = Set.of(
                "가족/관계", "건강/스포츠", "건강/취미/레저", "건축/디자인", "경제경영",
                "고전", "고전/명작", "공예/취미/수집", "기술공학", "대만도서", "독일 도서",
                "드라마/코미디", "라이트 노벨", "로맨스", "사회과학", "소설/시/희곡",
                "스페인 도서", "언어학", "에세이", "여행", "역사", "예술/대중문화", "의학",
                "인문/사회", "인문학", "인물/평전", "일본 도서", "자기계발", "자연과학",
                "장르소설", "전기/자서전", "전집/중고전집", "좋은부모", "중국 도서",
                "취미/스포츠", "컴퓨터", "컴퓨터/모바일", "해외도서"
        );

        assertThat(depth1Values)
                .allSatisfy(depth1 -> assertThat(resolver.resolve(category(depth1))).isNotEqualTo(RecommendGenre.ETC));
        assertThat(resolver.resolve(category("기타"))).isEqualTo(RecommendGenre.ETC);
    }

    private Category category(String depth1) {
        return Category.builder().depth1(depth1).build();
    }
}
