package com.cinemaebooking.backend.refund.application.dto;

import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RefundResponse {

    private Long id;
    private Long bookingId;

    // Refund amount fields
    private BigDecimal originalAmount;
    private BigDecimal refundAmount;
    private Integer refundPercentage;

    // Status & timestamps
    private RefundStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    // Reason
    private String reason;
    private String adminNote;

    // Booking details (populated via JOIN FETCH)
    private String bookingCode;
    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private Long userId;
    private String showtimeStartTime;
}
