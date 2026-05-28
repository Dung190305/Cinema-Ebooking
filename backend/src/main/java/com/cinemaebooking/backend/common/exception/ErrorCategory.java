package com.cinemaebooking.backend.common.exception;

public enum ErrorCategory {
    REQUIRED,
    INVALID_FORMAT,
    INVALID_LENGTH_MAX,
    INVALID_LENGTH_MIN,
    INVALID_VALUE,
    DUPLICATE,
    NOT_FOUND,
    CONFLICT,
    EXPIRED,
    FORBIDDEN,
    TIME_CONSTRAINT
}
