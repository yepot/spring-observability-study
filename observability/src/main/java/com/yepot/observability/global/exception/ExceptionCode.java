package com.yepot.observability.global.exception;

import org.springframework.http.HttpStatus;

public enum ExceptionCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ClientExceptionCode.INTERNAL_SERVER_ERROR, "예상치 못한 서버에러가 발생했습니다."),
    REQUEST_BODY_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "요청 본문이 필요합니다."),
    USER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "사용자 이름은 필수입니다."),
    INITIAL_BALANCE_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "초기 잔액은 필수입니다."),
    INITIAL_BALANCE_NEGATIVE(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "초기 잔액은 0원 이상이어야 합니다."),
    DEPOSIT_AMOUNT_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "입금 금액은 필수입니다."),
    DEPOSIT_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "입금 금액은 0원보다 커야 합니다."),
    WITHDRAW_AMOUNT_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "출금 금액은 필수입니다."),
    WITHDRAW_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "출금 금액은 0원보다 커야 합니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "잔액이 부족합니다."),
    FROM_ACCOUNT_ID_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "출금 계좌 ID는 필수입니다."),
    TO_ACCOUNT_ID_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "입금 계좌 ID는 필수입니다."),
    TRANSFER_AMOUNT_REQUIRED(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "이체 금액은 필수입니다."),
    TRANSFER_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "이체 금액은 0원보다 커야 합니다."),
    TRANSFER_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, ClientExceptionCode.ILLEGAL_ARGUMENT, "출금 계좌의 잔액이 부족합니다."),
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
