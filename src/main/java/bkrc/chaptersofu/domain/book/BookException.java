package bkrc.chaptersofu.domain.book;

import bkrc.chaptersofu.domain.common.shared.BusinessException;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;

public class BookException extends BusinessException {
    public BookException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BookException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BookException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
