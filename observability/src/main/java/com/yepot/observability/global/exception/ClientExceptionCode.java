package com.yepot.observability.global.exception;

public enum ClientExceptionCode {
    INTERNAL_SERVER_ERROR("Internal Server Error"),
    ILLEGAL_ARGUMENT("Illegal Argument"),
    NOT_FOUND("Not Found");

    private final String error;

    ClientExceptionCode(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
