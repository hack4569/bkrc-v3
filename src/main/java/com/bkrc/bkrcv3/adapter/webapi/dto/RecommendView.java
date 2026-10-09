package com.bkrc.bkrcv3.adapter.webapi.dto;

import com.bkrc.bkrcv3.aladin.domain.AladinBook;
import com.bkrc.bkrcv3.aladin.domain.BookComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Schema(description = "사용자 맞춤 도서 추천 뷰")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class RecommendView {
    @Schema(description = "도서 ID (itemId)", example = "123456789")
    private long itemId;
    @Schema(description = "슬라이드에 표시할 코멘트 목록. type: description(책소개) / aiRecommend(AI추천) / mdRecommend(편집자추천) / phrase(책속문장) / user(사용자추천) / toc(목차)")
    private List<BookComment> recommendCommentList;
    @Schema(description = "도서 제목", example = "Clean Code")
    private String title;
    @Schema(description = "도서 상세 페이지 링크")
    private String link;
    @Schema(description = "도서 커버 이미지 URL")
    private String cover;
    @Schema(description = "저자 (다수일 경우 '외 N명' 형식)", example = "로버트 C. 마틴 외 2명")
    private String author;
    @Schema(description = "카테고리명 (> 구분자 기준 2번째 뎁스)", example = "컴퓨터/인터넷")
    private String categoryName;

    private static final Map<String, Integer> COMMENT_TYPE_ORDER = Map.of(
            "phrase", 6,
            "description", 3,
            "descriptionInsight", 2,
            "aiRecommend", 4,
            "mdRecommend", 5,
            "user", 1,
            "toc", 7
    );

    public String getAuthor() {
        String seperator = ",";
        if (StringUtils.hasText(author) && author.contains(seperator)) {
            String[] authorName = this.author.split(seperator);
            if (authorName.length == 1) {
                return author;
            } else {
                return authorName[0] + " 외 " + (authorName.length - 1) + "명";
            }
        }
        return author;
    }

    public String getCategoryName() {
        String seperator = ">";
        if (StringUtils.hasText(categoryName) && categoryName.contains(seperator)) {
            String[] categoryName = this.categoryName.split(seperator);
            return categoryName[1];
        }
        return categoryName;
    }

    public static RecommendView from(AladinBook book) {
        return RecommendView.builder()
                .itemId(book.getItemId())
                .title(book.getTitle())
                .link(book.getLink())
                .cover(book.getCover())
                .recommendCommentList(sortBookComments(book.getBookCommentList()))
                .author(book.getAuthor())
                .categoryName(book.getCategoryName())
                .build();
    }

    private static List<BookComment> sortBookComments(List<BookComment> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return List.of();
        }

        return comments.stream()
                .sorted(Comparator
                        .comparingInt((BookComment comment) ->
                                COMMENT_TYPE_ORDER.getOrDefault(comment.getType(), Integer.MAX_VALUE))
                        .thenComparing(
                                BookComment::getBookCommentId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                .toList();
    }
}
