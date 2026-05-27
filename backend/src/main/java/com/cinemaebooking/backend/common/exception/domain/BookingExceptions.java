package com.cinemaebooking.backend.common.exception.domain;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingExceptions {

    // ================== NOT FOUND ==================

    public static BaseException notFound(BookingId id) {
        return new BaseException(ErrorCode.BOOKING_NOT_FOUND,
                "Không tìm thấy đặt vé: " + id.getValue());
    }

    public static BaseException notFoundForUserAndShowtime(Long userId, Long showtimeId) {
        return new BaseException(ErrorCode.BOOKING_NOT_FOUND,
                String.format("Không tìm thấy đơn hàng đang chờ cho người dùng %s và suất chiếu %s", userId, showtimeId));
    }

    // ================== BUSINESS RULE ==================

    public static BaseException invalidStatus(BookingStatus currentStatus) {
        return new BaseException(ErrorCode.BOOKING_INVALID_STATUS,
                "Trạng thái đặt vé không hợp lệ: " + currentStatus);
    }

    public static BaseException alreadyPaid(BookingId id) {
        return new BaseException(ErrorCode.BOOKING_ALREADY_PAID,
                "Đặt vé đã được thanh toán trước đó: " + id.getValue());
    }

    public static BaseException expired(BookingId id) {
        return new BaseException(ErrorCode.BOOKING_EXPIRED,
                "Phiên đặt vé đã hết hạn: " + id.getValue());
    }

    public static BaseException notOwnedByUser(BookingId bookingId, Long userId) {
        return new BaseException(ErrorCode.BOOKING_NOT_OWNED_BY_USER,
                String.format("Đặt vé %s không thuộc về người dùng %s", bookingId.getValue(), userId));
    }

    public static BaseException cancelled(BookingId id) {
        return new BaseException(ErrorCode.BOOKING_CANCELLED,
                "Đặt vé đã bị hủy: " + id.getValue());
    }

    // ================== TECHNICAL ==================

    public static BaseException processFailed(String detail) {
        return new BaseException(ErrorCode.BOOKING_PROCESS_FAILED,
                "Xử lý đặt vé thất bại: " + detail);
    }
}
