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

    private BigDecimal originalAmount;

    private BigDecimal refundAmount;

    private Integer refundPercentage;

    private RefundStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime processedAt;

    private String reason;

    private String adminNote;
}
