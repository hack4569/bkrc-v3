package com.bkrc.bkrcv3.aladin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Schema(description = "도서 코멘트 (AI 추천 / MD 추천 / 책 소개 등)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_comment")
public class BookComment {
    @Schema(description = "코멘트 ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookCommentId;

    @Schema(description = "코멘트 내용")
    @Lob
    private String comment;

    @Schema(description = "코멘트 유형 (aiRecommend / mdRecommend / description / toc / phrase)", example = "aiRecommend")
    private String type;

//    @Column(name = "item_id", insertable = false, updatable = false)
//    private Integer itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private AladinBook aladinBook;

    public static BookComment create(String comment, String type) {
        BookComment bookComment = new BookComment();
        bookComment.setComment(comment);
        bookComment.setType(type);
        return bookComment;
    }
}
