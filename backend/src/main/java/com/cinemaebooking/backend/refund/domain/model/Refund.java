package com.cinemaebooking.backend.refund.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.domain.valueobject.RefundId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class Refund extends BaseEntity<RefundId> {

    private Long bookingId;

    private BigDecimal originalAmount;

    private BigDecimal refundAmount;

    private Integer refundPercentage;

    private RefundStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime processedAt;

    private String reason;

    private String adminNote;

    public void approve(String adminNote) {
        if (this.status != RefundStatus.REQUESTED) {
            throw CommonExceptions.invalidInput("Chỉ yêu cầu hoàn tiền đang chờ xử lý mới được duyệt.");
        }

        this.status = RefundStatus.APPROVED;
        this.adminNote = adminNote;
    }

    public void reject(String adminNote) {
        if (this.status != RefundStatus.REQUESTED) {
            throw CommonExceptions.invalidInput("Chỉ yêu cầu hoàn tiền đang chờ xử lý mới được từ chối.");
        }

        this.status = RefundStatus.REJECTED;
        this.adminNote = adminNote;
        this.processedAt = LocalDateTime.now();
    }

    public void complete(String adminNote) {
        if (this.status != RefundStatus.APPROVED) {
            throw CommonExceptions.invalidInput("Chỉ yêu cầu hoàn tiền đã được duyệt mới có thể hoàn tất.");
        }

        this.status = RefundStatus.COMPLETED;
        this.adminNote = adminNote;
        this.processedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != RefundStatus.REQUESTED) {
            throw CommonExceptions.invalidInput("Chỉ có thể hủy yêu cầu hoàn tiền trước khi admin xử lý.");
        }

        this.status = RefundStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isProcessed() {
        return this.status == RefundStatus.REJECTED
                || this.status == RefundStatus.COMPLETED
                || this.status == RefundStatus.CANCELLED;
    }
}
