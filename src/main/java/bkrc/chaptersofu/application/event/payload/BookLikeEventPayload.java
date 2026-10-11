package bkrc.chaptersofu.application.event.payload;

import bkrc.chaptersofu.application.required.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookLikeEventPayload implements EventPayload {
    private Integer bookId;
    private Long memberId;
    private LocalDateTime createdAt;
    private Integer delta;
}
