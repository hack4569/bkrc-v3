package bkrc.chaptersofu.domain.like;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.BusinessException;

public class LikeException extends BusinessException {
    public LikeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LikeException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public LikeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
