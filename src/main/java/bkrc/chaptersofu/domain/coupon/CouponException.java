package bkrc.chaptersofu.domain.coupon;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.BusinessException;

public class CouponException extends BusinessException {
    public CouponException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CouponException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public CouponException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
