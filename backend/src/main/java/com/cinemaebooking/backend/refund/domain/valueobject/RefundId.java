package com.cinemaebooking.backend.refund.domain.valueobject;

import com.cinemaebooking.backend.common.domain.BaseId;

public final class RefundId extends BaseId {

    private RefundId(Long value) {
        super(value);
    }

    public static RefundId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("RefundId must be positive");
        }
        return new RefundId(value);
    }

    public static RefundId ofNullable(Long value) {
        return value == null ? null : new RefundId(value);
    }
}
