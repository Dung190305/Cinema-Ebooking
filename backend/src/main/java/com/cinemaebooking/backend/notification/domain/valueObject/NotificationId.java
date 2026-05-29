package com.cinemaebooking.backend.notification.domain.valueObject;

import com.cinemaebooking.backend.common.domain.BaseId;

public final class NotificationId extends BaseId {

    private NotificationId(Long value) {
        super(value);
    }

    public static NotificationId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("NotificationId must be positive");
        }
        return new NotificationId(value);
    }

    public static NotificationId ofNullable(Long value) {
        return value == null ? null : new NotificationId(value);
    }
}
