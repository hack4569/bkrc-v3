package com.bkrc.bkrcv3.member.domain;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;

public class PasswordNotEquals extends BusinessException {
    public PasswordNotEquals(ErrorCode errorCode) {
        super(errorCode);
    }
}
