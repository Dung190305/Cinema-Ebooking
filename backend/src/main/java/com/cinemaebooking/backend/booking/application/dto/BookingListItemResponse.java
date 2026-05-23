package com.cinemaebooking.backend.booking.application.dto;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * BookingListItemResponse - DTO cho màn hình danh sách đơn hàng.
 * Thu gọn thông tin để tối ưu hiệu năng truy vấn.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingListItemResponse {

    private Long bookingId;
    private String bookingCode;

    private String movieTitle;
    Instant showtime;

    private BigDecimal finalAmount;
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}