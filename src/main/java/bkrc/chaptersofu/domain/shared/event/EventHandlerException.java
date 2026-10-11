package bkrc.chaptersofu.domain.shared.event;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.exception.BusinessException;

public class EventHandlerException extends BusinessException {
    public EventHandlerException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventHandlerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
