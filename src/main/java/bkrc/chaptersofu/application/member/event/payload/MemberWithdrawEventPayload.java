package bkrc.chaptersofu.application.member.event.payload;

import bkrc.chaptersofu.application.shared.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberWithdrawEventPayload implements EventPayload {
    private String loginId;
    private LocalDateTime withdrawnAt;
}
