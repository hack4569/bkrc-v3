package bkrc.chaptersofu.domain.member;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.BusinessException;

public class PasswordNotEquals extends BusinessException {
    public PasswordNotEquals(ErrorCode errorCode) {
        super(errorCode);
    }
}
