package com.bkrc.bkrcv3.adapter.payload;

import com.bkrc.bkrcv3.required.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberJoinEventPayload implements EventPayload {
    private Long memberId;
    private String loginId;
    private LocalDateTime created;
}
