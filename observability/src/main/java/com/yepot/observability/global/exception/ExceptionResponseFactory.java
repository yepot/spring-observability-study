package com.yepot.observability.global.exception;

import com.yepot.observability.global.exception.dto.ExceptionResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;

public final class ExceptionResponseFactory {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private ExceptionResponseFactory() {
    }

    public static ExceptionResponse from(ExceptionCode exceptionCode, String path) {
        return new ExceptionResponse(
                OffsetDateTime.now(ZoneOffset.UTC).format(TIMESTAMP_FORMATTER),
                exceptionCode.getHttpStatus().value(),
                exceptionCode.getClientExceptionCode().getError(),
                exceptionCode.getMessage(),
                path
        );
    }

    public static ExceptionResponse from(ApiException exception, String path) {
        return new ExceptionResponse(
                OffsetDateTime.now(ZoneOffset.UTC).format(TIMESTAMP_FORMATTER),
                exception.getHttpStatusCode().value(),
                exception.getExceptionCodeName(),
                exception.getMessage(),
                path
        );
    }

    public static ExceptionResponse badRequest(String message, String path) {
        return new ExceptionResponse(
                OffsetDateTime.now(ZoneOffset.UTC).format(TIMESTAMP_FORMATTER),
                HttpStatus.BAD_REQUEST.value(),
                ClientExceptionCode.ILLEGAL_ARGUMENT.getError(),
                message,
                path
        );
    }

    public static ExceptionResponse internalServerError(String path) {
        return from(ExceptionCode.INTERNAL_SERVER_ERROR, path);
    }
}
