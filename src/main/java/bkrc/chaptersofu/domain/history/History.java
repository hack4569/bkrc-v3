package bkrc.chaptersofu.domain.history;

import bkrc.chaptersofu.domain.common.shared.BaseEntity;
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
                        name = "uk_history_item_member",
                        columnNames = {"member_id", "book_id"}
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

    @Schema(description = "회원 ID")
    @Column(nullable = false)
    private Long memberId;

    @Schema(description = "열람한 내부 도서 ID", example = "1")
    @Column(name = "book_id", nullable = false)
    private Long bookId;


    public static History create(Long bookId, Long memberId, Long historyId) {
        History history = new History();
        history.setId(historyId);
        history.setMemberId(memberId);
        history.setBookId(bookId);
        return history;
    }
}
