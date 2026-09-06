package com.bkrc.bkrcv3.like.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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

    @Schema(description = "좋아요 집계 이벤트 버전", example = "43")
    @Column(nullable = false)
    private Long eventVersion = 0L;

    public static LikeCount create(Integer itemId, Integer likeCount) {
        LikeCount bookLikeCount = new LikeCount();
        bookLikeCount.itemId = itemId;
        bookLikeCount.likeCount = likeCount;
        bookLikeCount.eventVersion = 0L;
        return bookLikeCount;
    }

    public void increase() {
        this.likeCount++;
        increaseEventVersion();
    }

    public void decrease() {
        this.likeCount--;
        increaseEventVersion();
    }

    private void increaseEventVersion() {
        this.eventVersion = this.eventVersion == null ? 1L : this.eventVersion + 1;
    }
}
