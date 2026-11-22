package bkrc.chaptersofu.domain.aladin;

import bkrc.chaptersofu.domain.book.Book;
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

    @Schema(description = "코멘트 유형 (aiRecommend / mdRecommend / description / toc / phrase / user)", example = "aiRecommend")
    private String type;

//    @Column(name = "item_id", insertable = false, updatable = false)
//    private Integer itemId;

    /**
     * 공통 도서가 코멘트 연관관계의 실제 소유자이며 book_id 외래키를 저장한다.
     * Book.bookCommentList의 mappedBy 값과 필드명을 일치시켜 Hibernate 매핑 오류를 방지한다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Book book;


    public static BookComment create(String comment, String type) {
        BookComment bookComment = new BookComment();
        bookComment.setComment(comment);
        bookComment.setType(type);
        return bookComment;
    }
}
