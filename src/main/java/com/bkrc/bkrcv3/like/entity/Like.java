package com.bkrc.bkrcv3.like.entity;

import com.bkrc.bkrcv3.common.shared.BaseEntity;
import com.bkrc.bkrcv3.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;

@Schema(description = "도서 좋아요 엔티티")
@Entity
@Table(name = "like_book",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_like_book_item_login",
                columnNames = {"item_id", "login_id"}
        )
    }
)
@Getter
public class Like extends BaseEntity {
    @Schema(description = "좋아요 ID (Snowflake)")
    @Id
    private Long likeId;

    @Schema(description = "도서 ID (itemId)", example = "123456789")
    @Column(nullable = false)
    private Integer itemId;

//    @Schema(description = "로그인 ID", example = "user123")
//    @Column(nullable = false)
//    private String loginId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id")
    @JsonIgnore
    private Member member;

    public static Like create(Long id, int itemId, Member member) {
        Like like = new Like();
        like.likeId = id;
        like.itemId = itemId;
        like.member = member;
        return like;
    }
}
