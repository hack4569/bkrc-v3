package com.bkrc.bkrcv3.history.entity;

import com.bkrc.bkrcv3.common.shared.BaseEntity;
import com.bkrc.bkrcv3.history.application.HistorySaveRequest;
import com.bkrc.bkrcv3.history.application.HistoryResponse;
import com.bkrc.bkrcv3.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Schema(description = "도서 열람 이력 엔티티")
@Entity
@Table(
        name="histories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_history_item_login",
                        columnNames = {"login_id", "item_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class History extends BaseEntity {
    @Schema(description = "이력 ID (Snowflake)")
    @Id
    @Column(name = "history_id")
    private Long id;

    @Schema(description = "로그인 ID", example = "user123")
    @Column(nullable = false)
    private String loginId;

    @Schema(description = "열람한 도서 ID (itemId)", example = "123456789")
    @Column(nullable = false)
    private Integer itemId;


    public HistoryResponse of() {
        HistoryResponse historyResponse = new HistoryResponse();
        historyResponse.setItemId(getItemId());
        historyResponse.setLoginId(getLoginId());
        historyResponse.setCreatedAt(getCreated());
        return historyResponse;
    }

    public static History create(Integer itemId, String loginId, Long historyId) {
        History history = new History();
        history.setId(historyId);
        history.setLoginId(loginId);
        history.setItemId(itemId);
        return history;
    }
}