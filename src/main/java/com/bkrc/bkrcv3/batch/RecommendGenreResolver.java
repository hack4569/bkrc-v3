package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.entity.Category;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RecommendGenreResolver {

    private static final Map<String, RecommendGenre> GENRE_BY_DEPTH1 = Map.ofEntries(
            Map.entry("가족/관계", RecommendGenre.FAMILY_RELATIONSHIPS),
            Map.entry("건강/스포츠", RecommendGenre.HEALTH_LEISURE),
            Map.entry("건강/취미/레저", RecommendGenre.HEALTH_LEISURE),
            Map.entry("건축/디자인", RecommendGenre.ART_DESIGN),
            Map.entry("경제경영", RecommendGenre.BUSINESS),
            Map.entry("고전", RecommendGenre.LITERATURE),
            Map.entry("고전/명작", RecommendGenre.LITERATURE),
            Map.entry("공예/취미/수집", RecommendGenre.HEALTH_LEISURE),
            Map.entry("기술공학", RecommendGenre.SCIENCE_ENGINEERING),
            Map.entry("기타", RecommendGenre.ETC),
            Map.entry("대만도서", RecommendGenre.FOREIGN_BOOKS),
            Map.entry("독일 도서", RecommendGenre.FOREIGN_BOOKS),
            Map.entry("드라마/코미디", RecommendGenre.ART_DESIGN),
            Map.entry("라이트 노벨", RecommendGenre.LITERATURE),
            Map.entry("로맨스", RecommendGenre.LITERATURE),
            Map.entry("사회과학", RecommendGenre.SOCIAL_HUMANITIES),
            Map.entry("소설/시/희곡", RecommendGenre.LITERATURE),
            Map.entry("스페인 도서", RecommendGenre.FOREIGN_BOOKS),
            Map.entry("언어학", RecommendGenre.SOCIAL_HUMANITIES),
            Map.entry("에세이", RecommendGenre.ESSAY),
            Map.entry("여행", RecommendGenre.TRAVEL),
            Map.entry("역사", RecommendGenre.HISTORY_BIOGRAPHY),
            Map.entry("예술/대중문화", RecommendGenre.ART_DESIGN),
            Map.entry("의학", RecommendGenre.SCIENCE_ENGINEERING),
            Map.entry("인문/사회", RecommendGenre.SOCIAL_HUMANITIES),
            Map.entry("인문학", RecommendGenre.SOCIAL_HUMANITIES),
            Map.entry("인물/평전", RecommendGenre.HISTORY_BIOGRAPHY),
            Map.entry("일본 도서", RecommendGenre.FOREIGN_BOOKS),
            Map.entry("자기계발", RecommendGenre.SELF_DEVELOPMENT),
            Map.entry("자연과학", RecommendGenre.SCIENCE_ENGINEERING),
            Map.entry("장르소설", RecommendGenre.LITERATURE),
            Map.entry("전기/자서전", RecommendGenre.HISTORY_BIOGRAPHY),
            Map.entry("전집/중고전집", RecommendGenre.COLLECTION),
            Map.entry("좋은부모", RecommendGenre.FAMILY_RELATIONSHIPS),
            Map.entry("중국 도서", RecommendGenre.FOREIGN_BOOKS),
            Map.entry("취미/스포츠", RecommendGenre.HEALTH_LEISURE),
            Map.entry("컴퓨터", RecommendGenre.COMPUTER),
            Map.entry("컴퓨터/모바일", RecommendGenre.COMPUTER),
            Map.entry("해외도서", RecommendGenre.FOREIGN_BOOKS)
    );

    public RecommendGenre resolve(Category category) {
        if (category == null) {
            return RecommendGenre.ETC;
        }
        return GENRE_BY_DEPTH1.getOrDefault(category.getDepth1(), RecommendGenre.ETC);
    }
}
