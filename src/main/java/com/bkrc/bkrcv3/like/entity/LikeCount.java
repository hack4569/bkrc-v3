package com.bkrc.bkrcv3.like.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.ToString;

@Schema(description = "도서별 좋아요 수 집계 엔티티")
@Table(name = "like_count_book")
@Getter
@Entity
@ToString
public class LikeCount {
    @Schema(description = "도서 ID (itemId)", example = "123456789")
    @Id
    private Integer itemId;

    @Schema(description = "좋아요 수", example = "42")
    private Integer likeCount;

    public static LikeCount create(Integer itemId, Integer likeCount) {
        LikeCount bookLikeCount = new LikeCount();
        bookLikeCount.itemId = itemId;
        bookLikeCount.likeCount = likeCount;
        return bookLikeCount;
    }

    public void increase() {
        this.likeCount++;
    }

    public void decrease() {
        this.likeCount--;
    }
}


