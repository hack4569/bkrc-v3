package com.bkrc.bkrcv3.like.domain;

import com.bkrc.bkrcv3.aladin.domain.AladinBook;
import com.bkrc.bkrcv3.common.shared.BaseEntity;
import com.bkrc.bkrcv3.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Schema(description = "도서 좋아요 엔티티")
@Entity
@Table(name = "like_book", uniqueConstraints =
        @UniqueConstraint(name = "uk_like_book_item_member", columnNames = {"item_id", "member_id"}))
@Getter
public class Like extends BaseEntity {
    @Schema(description = "좋아요 ID (Snowflake)")
    @Id
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private AladinBook book;

    protected Like() {
    }

    public static Like create(Long id, AladinBook book, Member member) {
        Like like = new Like();
        like.likeId = id;
        like.book = book;
        like.member = member;
        return like;
    }
}
