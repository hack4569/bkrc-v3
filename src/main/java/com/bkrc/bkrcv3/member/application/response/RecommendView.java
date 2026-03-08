package com.bkrc.bkrcv3.member.application.response;

import com.bkrc.bkrcv3.aladin.entity.BookComment;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class RecommendView {
    private long itemId;
    private List<BookComment> recommendCommentList;
    private String title;
    private String link;
    private String cover;
    private String author;
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
