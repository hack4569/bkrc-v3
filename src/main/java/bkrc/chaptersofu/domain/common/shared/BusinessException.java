package bkrc.chaptersofu.domain.common.shared;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import lombok.Getter;

/**
 * 5** 에러
 */
@Getter
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCode errorCode, String message) {
        super(errorCode.getMessage() + ": " + message);
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getMessage() + ": " + message, cause);
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
