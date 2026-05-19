package com.cinemaebooking.backend.common.exception.domain;

import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import com.cinemaebooking.backend.seat_lock.domain.valueobject.SeatLockId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SeatLockExceptions - Exception factory cho domain SeatLock.
 * Tất cả exception liên quan đến seat lock phải được tạo qua factory này.
 *
 * <p>Error codes: 4015–4018
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SeatLockExceptions {

    public static BaseException notFound(SeatLockId id) {
        return new BaseException(ErrorCode.SEAT_LOCK_NOT_FOUND,
                "Không tìm thấy khóa ghế: " + id.getValue());
    }

    public static BaseException expired(SeatLockId id) {
        return new BaseException(ErrorCode.SEAT_LOCK_EXPIRED,
                "Khóa ghế đã hết hạn: " + id.getValue());
    }

    public static BaseException conflict(Long showtimeSeatId) {
        return new BaseException(ErrorCode.SEAT_LOCK_CONFLICT,
                "Ghế đã được người khác khóa, không thể chiếm giữ");
    }

    public static BaseException conflict(String seatNumber) {
        return new BaseException(ErrorCode.SEAT_LOCK_CONFLICT,
                "Ghế " + seatNumber + " đã được người khác khóa, không thể chiếm giữ");
    }

    public static BaseException failed(String detail) {
        return new BaseException(ErrorCode.SEAT_LOCK_FAILED,
                "Khóa ghế thất bại: " + detail);
    }

    public static BaseException alreadyLocked(Long showtimeSeatId, Long userId) {
        return new BaseException(ErrorCode.SEAT_LOCK_CONFLICT,
                "Ghế đã được bạn khóa trước đó");
    }
}
