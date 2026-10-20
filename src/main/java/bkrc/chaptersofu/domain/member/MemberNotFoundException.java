package bkrc.chaptersofu.domain.member;

import bkrc.chaptersofu.domain.common.shared.BusinessException;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import lombok.Getter;

@Getter
public class MemberNotFoundException extends BusinessException {

    private final Long memberId;
    private final String loginId;

    public MemberNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
        this.memberId = null;
        this.loginId = null;
    }

    public MemberNotFoundException(Long memberId) {
        super(ErrorCode.USER_NOT_FOUND, "memberId=" + memberId);
        this.memberId = memberId;
        this.loginId = null;
    }

    public MemberNotFoundException(String loginId) {
        super(ErrorCode.USER_NOT_FOUND, "loginId=" + loginId);
        this.memberId = null;
        this.loginId = loginId;
    }
}
