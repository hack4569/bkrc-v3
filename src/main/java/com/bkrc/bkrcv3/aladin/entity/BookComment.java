package com.bkrc.bkrcv3.aladin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_comment")
public class BookComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookCommentId;

    @Lob
    private String comment;

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
