package bkrc.chaptersofu.domain.like;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Schema(description = "도서별 좋아요 수 집계 엔티티")
@Table(name = "like_count_book")
@Getter
@Entity
@ToString
public class LikeCount {
    @Schema(description = "내부 도서 ID", example = "1")
    @Id
    @Column(name = "book_id")
    private Long bookId;

    @Schema(description = "좋아요 수", example = "42")
    private Integer likeCount;

    @Schema(description = "좋아요 집계 이벤트 버전", example = "43")
    @Column(nullable = false)
    private Long eventVersion = 0L;

    protected LikeCount() {
    }

    public static LikeCount create(Long bookId, Integer likeCount) {
        LikeCount count = new LikeCount();
        count.bookId = bookId;
        count.likeCount = likeCount;
        return count;
    }

    public void increase() {
        likeCount++;
        increaseEventVersion();
    }

    public void decrease() {
        likeCount--;
        increaseEventVersion();
    }

    private void increaseEventVersion() {
        eventVersion = eventVersion == null ? 1L : eventVersion + 1;
    }
}
