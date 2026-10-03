package com.bkrc.bkrcv3.common.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ALADIN_NOT_READY(HttpStatus.SERVICE_UNAVAILABLE, "EAL1", "준비중입니다."),
    ALADIN_CLIENT_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "EAL2", "알라딘 API 에러"),
    ALADIN_NOT_FOUND(HttpStatus.NOT_FOUND, "EAL3","책 정보를 찾을 수 없습니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "EAL4", "조회되지 않는 상품번호입니다."),

    USER_NOT_EQUALS_PW(HttpStatus.BAD_REQUEST, "EU1", "비밀번호가 일치하지 않습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "EU2", "이미 등록된 사용자 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "EU3","해당 아이디를 찾을 수 없습니다."),

    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "EL1","이미 처리 되었습니다."),

    HISTORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "EH1","이미 등록되었습니다."),

    RECOMMENDATION_NOT_EDITABLE(HttpStatus.CONFLICT, "ER1", "미승인 상태의 추천만 수정할 수 있습니다."),

    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "EC1", "쿠폰을 찾을 수 없습니다."),
    COUPON_NOT_DOWNLOADABLE(HttpStatus.CONFLICT, "EC2", "다운로드할 수 없는 쿠폰입니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "EC3", "이미 다운로드한 쿠폰입니다."),
    MEMBER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "EC4", "보유 쿠폰을 찾을 수 없습니다."),
    COUPON_ALREADY_USED(HttpStatus.CONFLICT, "EC5", "이미 사용한 쿠폰입니다."),
    COUPON_NOT_VALID(HttpStatus.CONFLICT, "EC6", "쿠폰 유효기간이 아닙니다."),

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "EAU1","인증에 실패했습니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ECM0", "에러가 발생하였습니다."),
    INVALID_TIME(HttpStatus.INTERNAL_SERVER_ERROR, "ECM1", "Invalid Time"),
    EVENT_HANDLER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"ECM2", "이벤트 핸들러를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
