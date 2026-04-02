package com.bkrc.bkrcv3.like.entity;

import com.bkrc.bkrcv3.common.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;
    @Column(nullable = false)
    private Integer itemId;
    @Column(nullable = false)
    private String loginId;

    public static Like create(int itemId, String loginId) {
        Like like = new Like();
        like.loginId = loginId;
        like.itemId = itemId;
        return like;
    }
}
