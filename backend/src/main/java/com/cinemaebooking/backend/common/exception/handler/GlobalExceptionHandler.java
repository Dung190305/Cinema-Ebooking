package com.cinemaebooking.backend.common.exception.handler;

import com.cinemaebooking.backend.common.api.response.ApiError;
import com.cinemaebooking.backend.common.api.response.ApiResponse;
import com.cinemaebooking.backend.common.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId";

    // =========================================================
    // ================== BUSINESS EXCEPTION ====================
    // =========================================================

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseException(
            BaseException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ex.getErrorCode();
        String traceId = getTraceId();

        logByType(traceId, errorCode, request, ex);

        return buildErrorResponse(
                errorCode,
                traceId,
                request,
                ex.getParams(),
                ex.getDetails().isEmpty() ? null : ex.getDetails()
        );
    }

    // =========================================================
    // ================== VALIDATION ============================
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String traceId = getTraceId();
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        List<ErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ErrorDetail(
                        e.getField(),
                        null,
                        e.getDefaultMessage()
                ))
                .toList();

        log.warn("[{}] Validation failed | path={} | details={}",
                traceId,
                request.getRequestURI(),
                details
        );

        return buildErrorResponse(
                errorCode,
                traceId,
                request,
                null,
                details
        );
    }

    // =========================================================
    // ================== SECURITY ==============================
    // =========================================================

    /**
     * @PreAuthorize fail
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request
    ) {
        return handleSecurityError(ErrorCode.FORBIDDEN, request, "Authorization denied");
    }

    /**
     * requestMatchers / filter security
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return handleSecurityError(ErrorCode.FORBIDDEN, request, "Access denied");
    }

    /**
     * chưa login / token invalid
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return handleSecurityError(ErrorCode.UNAUTHORIZED, request, "Unauthorized");
    }

    private ResponseEntity<ApiResponse<?>> handleSecurityError(
            ErrorCode errorCode,
            HttpServletRequest request,
            String logMessage
    ) {
        String traceId = getTraceId();

        log.warn("[{}] {} | path={}",
                traceId,
                logMessage,
                request.getRequestURI()
        );

        return buildErrorResponse(
                errorCode,
                traceId,
                request,
                null,
                null
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        // Just return 404, don't log as error
        return ResponseEntity.status(404).body(null);
    }

    // =========================================================
    // ================== FALLBACK ==============================
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        String traceId = getTraceId();
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        log.error("[{}] Unexpected error | path={}",
                traceId,
                request.getRequestURI(),
                ex
        );

        return buildErrorResponse(
                errorCode,
                traceId,
                request,
                null,
                null
        );
    }

    // =========================================================
    // ================== CORE BUILDER ==========================
    // =========================================================

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(
            ErrorCode errorCode,
            String traceId,
            HttpServletRequest request,
            Map<String, Object> params,
            List<ErrorDetail> details
    ) {
        ApiError error = new ApiError(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getType(),
                params,
                details
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(error, traceId, request.getRequestURI()));
    }

    private String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return (traceId != null && !traceId.isBlank()) ? traceId : "UNKNOWN";
    }

    private void logByType(
            String traceId,
            ErrorCode errorCode,
            HttpServletRequest request,
            BaseException ex
    ) {
        String debugInfo = ex.getDebugMessage() != null
                ? " | debug: " + ex.getDebugMessage()
                : "";

        if (errorCode.getType() == ErrorType.TECHNICAL) {
            log.error("[{}] Technical error | code={} | path={}{}",
                    traceId,
                    errorCode.getCode(),
                    request.getRequestURI(),
                    debugInfo,
                    ex
            );
        } else {
            log.warn("[{}] Business error | code={} | path={}{}",
                    traceId,
                    errorCode.getCode(),
                    request.getRequestURI(),
                    debugInfo
            );
        }
    }
}