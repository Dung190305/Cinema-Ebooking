package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;
import com.cinemaebooking.backend.report.application.dto.RefundReportResponse;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.ReportDateRangeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GetRefundReportUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public RefundReportResponse execute(
            LocalDate fromDate,
            LocalDate toDate,
            Long cinemaId,
            Long movieId
    ) {
        ReportDateRange dateRange = reportDateRangeValidator.validateAndBuild(fromDate, toDate);

        List<RefundJpaEntity> refunds = reportQueryPort.findRefundsByRequestedAt(
                dateRange.getFromDateTime(),
                dateRange.getToDateTime(),
                cinemaId,
                movieId
        );

        long totalRefunds = refunds.size();
        long requestedRefunds = countByStatus(refunds, RefundStatus.REQUESTED);
        long approvedRefunds = countByStatus(refunds, RefundStatus.APPROVED);
        long completedRefunds = countByStatus(refunds, RefundStatus.COMPLETED);
        long rejectedRefunds = countByStatus(refunds, RefundStatus.REJECTED);
        long cancelledRefunds = countByStatus(refunds, RefundStatus.CANCELLED);

        BigDecimal totalOriginalAmount = refunds.stream()
                .map(RefundJpaEntity::getOriginalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefundAmount = refunds.stream()
                .filter(refund -> refund.getStatus() == RefundStatus.COMPLETED)
                .map(RefundJpaEntity::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageRefundAmount = completedRefunds == 0
                ? BigDecimal.ZERO
                : totalRefundAmount.divide(
                BigDecimal.valueOf(completedRefunds),
                2,
                RoundingMode.HALF_UP
        );

        return RefundReportResponse.builder()
                .totalRefunds(totalRefunds)
                .requestedRefunds(requestedRefunds)
                .approvedRefunds(approvedRefunds)
                .completedRefunds(completedRefunds)
                .rejectedRefunds(rejectedRefunds)
                .cancelledRefunds(cancelledRefunds)
                .totalOriginalAmount(totalOriginalAmount)
                .totalRefundAmount(totalRefundAmount)
                .averageRefundAmount(averageRefundAmount)
                .build();
    }

    private long countByStatus(List<RefundJpaEntity> refunds, RefundStatus status) {
        return refunds.stream()
                .filter(refund -> refund.getStatus() == status)
                .count();
    }
}
