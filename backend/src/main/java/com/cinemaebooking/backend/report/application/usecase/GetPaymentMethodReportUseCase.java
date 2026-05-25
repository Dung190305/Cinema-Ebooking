package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.payment.domain.enums.PaymentMethod;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.report.application.dto.PaymentMethodReportResponse;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPaymentMethodReportUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public List<PaymentMethodReportResponse> execute(
            LocalDate fromDate,
            LocalDate toDate,
            Long cinemaId,
            Long movieId
    ) {
        ReportDateRange dateRange = reportDateRangeValidator.validateAndBuild(fromDate, toDate);

        List<PaymentJpaEntity> payments = reportQueryPort.findSuccessfulPaymentsByPaidAt(
                dateRange.getFromDateTime(),
                dateRange.getToDateTime(),
                cinemaId,
                movieId
        );

        BigDecimal totalAmount = payments.stream()
                .map(PaymentJpaEntity::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<PaymentMethod, List<PaymentJpaEntity>> grouped = payments.stream()
                .collect(Collectors.groupingBy(PaymentJpaEntity::getMethod));

        return grouped.entrySet()
                .stream()
                .map(entry -> {
                    PaymentMethod method = entry.getKey();
                    List<PaymentJpaEntity> methodPayments = entry.getValue();

                    BigDecimal amount = methodPayments.stream()
                            .map(PaymentJpaEntity::getAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : amount.multiply(BigDecimal.valueOf(100))
                              .divide(totalAmount, 2, RoundingMode.HALF_UP);

                    return PaymentMethodReportResponse.builder()
                            .method(method)
                            .transactionCount(methodPayments.size())
                            .totalAmount(amount)
                            .percentage(percentage)
                            .build();
                })
                .sorted(Comparator.comparing(PaymentMethodReportResponse::getTotalAmount).reversed())
                .toList();
    }
}