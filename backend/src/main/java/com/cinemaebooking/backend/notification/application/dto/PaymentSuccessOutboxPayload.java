package com.cinemaebooking.backend.notification.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessOutboxPayload implements Serializable {

    private Long bookingId;
    private String bookingCode;
    private Long userId;
    private String userEmail;
    private String userName;
    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private Instant showtimeStartTime;
    private BigDecimal finalAmount;
    private List<SeatData> seats;
    private List<ComboData> combos;
    private CouponData coupon;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatData implements Serializable {
        private String seatName;
        private String seatType;
        private BigDecimal price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComboData implements Serializable {
        private String comboName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponData implements Serializable {
        private String code;
        private BigDecimal discountAmount;
    }
}
