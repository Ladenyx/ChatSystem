package com.chatsystem.server.api;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.api.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalApiExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    private final Clock clock;

    public GlobalApiExceptionHandler(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(
                ex.getErrorCode(),
                ex.getMessage(),
                clock.instant(),
                request.getRequestURI(),
                RequestIdFilter.resolve(request),
                ex.getDetails()
        );
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(body.status());
        if (ex.getRetryAfter() != null) {
            builder.header(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1L, ex.getRetryAfter().toSeconds())));
        }
        return builder.body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new FieldError(
                        err.getField(),
                        "VALIDATION_FAILED",
                        err.getDefaultMessage() == null ? "Giá trị không hợp lệ." : err.getDefaultMessage()))
                .toList();
        return errorResponse(ErrorCode.VALIDATION_ERROR, "Dữ liệu đầu vào không hợp lệ.", details, request);
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
        return errorResponse(ErrorCode.VALIDATION_ERROR, "Yêu cầu không đúng định dạng.", List.of(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        return errorResponse(ErrorCode.NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu.", List.of(), request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabaseFailure(DataAccessException ex, HttpServletRequest request) {
        LOG.error("Database access error at {}: {}", request.getRequestURI(), ex.getMessage());
        return errorResponse(ErrorCode.SERVICE_UNAVAILABLE, "Cơ sở dữ liệu tạm thời không sẵn sàng.", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        LOG.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return errorResponse(ErrorCode.INTERNAL_ERROR, "Lỗi nội bộ Server ngoài dự kiến.", List.of(), request);
    }

    private ResponseEntity<ApiError> errorResponse(
            ErrorCode code,
            String message,
            List<FieldError> details,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.of(
                code,
                message,
                clock.instant(),
                request.getRequestURI(),
                RequestIdFilter.resolve(request),
                details
        );
        return ResponseEntity.status(error.status()).body(error);
    }
}
