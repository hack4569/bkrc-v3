package com.bkrc.bkrcv3.member.domain;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;

public class DuplicateMemberException extends BusinessException {
    public DuplicateMemberException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateMemberException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
