package com.cinemaebooking.backend.seat_lock.domain.valueobject;

import com.cinemaebooking.backend.common.domain.BaseId;

/**
 * SeatLockId - Type-safe identifier for SeatLock domain entity.
 *
 * @author Hieu Nguyen
 * @since 2026
 */
public final class SeatLockId extends BaseId {

    private SeatLockId(Long value) {
        super(value);
    }

    public static SeatLockId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("SeatLockId must be positive");
        }
        return new SeatLockId(value);
    }

    public static SeatLockId ofNullable(Long value) {
        return value == null ? null : new SeatLockId(value);
    }
}
