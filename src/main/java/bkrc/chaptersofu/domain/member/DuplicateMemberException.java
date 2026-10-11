package bkrc.chaptersofu.domain.member;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.exception.BusinessException;

public class DuplicateMemberException extends BusinessException {
    public DuplicateMemberException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateMemberException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
