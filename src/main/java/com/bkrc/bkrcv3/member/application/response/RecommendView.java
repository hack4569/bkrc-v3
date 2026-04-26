package com.bkrc.bkrcv3.member.application.response;

import com.bkrc.bkrcv3.aladin.entity.BookComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;

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
    @Schema(description = "AI/MD 추천 코멘트 목록")
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
}
