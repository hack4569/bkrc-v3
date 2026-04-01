package com.bkrc.bkrcv3.common.event.payload;

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
    private String loginId;
    private LocalDateTime createdAt;
    private Integer bookLikeCount;
}
