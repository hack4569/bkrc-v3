package bkrc.chaptersofu.domain.history;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.BusinessException;

public class HistoryException extends BusinessException {
    public HistoryException(ErrorCode errorCode) {
        super(errorCode);
    }

    public HistoryException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
