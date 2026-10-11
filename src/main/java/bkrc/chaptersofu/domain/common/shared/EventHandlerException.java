package bkrc.chaptersofu.domain.common.shared;

public class EventHandlerException extends BusinessException {
    public EventHandlerException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventHandlerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
