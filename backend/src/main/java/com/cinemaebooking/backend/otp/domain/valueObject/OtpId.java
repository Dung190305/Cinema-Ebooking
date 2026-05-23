package com.cinemaebooking.backend.otp.domain.valueObject;

import com.cinemaebooking.backend.common.domain.BaseId;

public final class OtpId extends BaseId {

    private OtpId(Long value) {
        super(value);
    }

    public static OtpId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OtpId must be positive");
        }
        return new OtpId(value);
    }

    public static OtpId ofNullable(Long value) {
        return value == null ? null : new OtpId(value);
    }
}
