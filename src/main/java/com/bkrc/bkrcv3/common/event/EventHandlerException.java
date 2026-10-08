package com.bkrc.bkrcv3.common.event;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;

public class EventHandlerException extends BusinessException {
    public EventHandlerException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventHandlerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
