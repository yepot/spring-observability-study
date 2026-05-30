package com.yepot.observability.global.exception;

import org.springframework.http.HttpStatus;

public enum ExceptionCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ClientExceptionCode.INTERNAL_SERVER_ERROR, "예상치 못한 서버에러가 발생했습니다."),
    REQUEST_BODY_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "요청 본문이 필요합니다."),
    USER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "사용자 이름은 필수입니다."),
    INITIAL_BALANCE_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "초기 잔액은 필수입니다."),
    INITIAL_BALANCE_NEGATIVE(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "초기 잔액은 0원 이상이어야 합니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, ClientExceptionCode.NOT_FOUND, "존재하지 않는 계좌입니다.");

    private final HttpStatus httpStatus;
    private final ClientExceptionCode clientExceptionCode;
    private final String message;

    ExceptionCode(HttpStatus httpStatus, ClientExceptionCode clientExceptionCode, String message) {
        this.httpStatus = httpStatus;
        this.clientExceptionCode = clientExceptionCode;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ClientExceptionCode getClientExceptionCode() {
        return clientExceptionCode;
    }

    public String getMessage() {
        return message;
    }
}
