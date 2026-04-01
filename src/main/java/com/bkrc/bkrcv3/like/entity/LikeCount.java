package com.bkrc.bkrcv3.like.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.ToString;

@Table(name = "like_count_book")
@Getter
@Entity
@ToString
public class LikeCount {
    @Id
    private Integer itemId;
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


