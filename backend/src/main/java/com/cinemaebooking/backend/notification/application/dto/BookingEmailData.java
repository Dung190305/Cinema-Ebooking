package com.cinemaebooking.backend.notification.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class BookingEmailData {

    private Long bookingId;
    private String bookingCode;
    private Long userId;

    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private Instant showtimeStartTime;

    private List<SeatData> seats;
    private List<ComboData> combos;

    private BigDecimal finalAmount;

    private CouponData coupon;

    @Getter
    @Builder
    public static class CouponData {
        private String code;
        private BigDecimal discountAmount;
    }

    @Getter
    @Builder
    public static class SeatData {
        private String seatName;
        private String seatType;
        private BigDecimal price;
    }

    @Getter
    @Builder
    public static class ComboData {
        private String comboName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
