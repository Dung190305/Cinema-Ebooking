package com.cinemaebooking.backend.booking.application.dto;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BookingDetailResponse - DTO chi tiết toàn bộ đơn hàng.
 * Phục vụ cho màn hình "Chi tiết vé đã đặt".
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {

    private Long bookingId;
    private String bookingCode;

    private Long userId;
    private Long showtimeId;

    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private LocalDateTime showtimeStartTime;

    private BigDecimal totalTicketPrice;
    private BigDecimal totalComboPrice;
    private BigDecimal tierDiscountAmount;
    private BigDecimal couponDiscountAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    private String membershipTierName;
    private BigDecimal membershipDiscountPercent;

    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime paidAt;

    private List<SeatInfo> seats;
    private List<ComboInfo> combos;
    private CouponInfo coupon;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private Long showtimeSeatId; // Dùng ID của ghế trong suất chiếu
        private String seatName;     // Ví dụ: "A12"
        private String seatType;     // Ví dụ: "VIP", "NORMAL"
        private BigDecimal price;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComboInfo {
        private Long comboId;
        private String comboName;
        private Integer quantity;
        private BigDecimal unitPrice; // Giá đơn lẻ để UI dễ hiển thị
        private BigDecimal totalPrice; // Tổng tiền của combo này (quantity * unitPrice)
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponInfo {
        private Long couponId;
        private String code;
        private BigDecimal discountValue;
    }
}