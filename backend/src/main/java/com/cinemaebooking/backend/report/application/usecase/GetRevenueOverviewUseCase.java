package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.dto.RevenueOverviewResponse;
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
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class GetRevenueOverviewUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public RevenueOverviewResponse execute(
            LocalDate fromDate,
            LocalDate toDate,
            Long cinemaId,
            Long movieId
    ) {
        ReportDateRange dateRange = reportDateRangeValidator.validateAndBuild(fromDate, toDate);

        List<BookingJpaEntity> allBookings = reportQueryPort.findBookingsByCreatedAt(
                dateRange.getFromDateTime(),
                dateRange.getToDateTime(),
                cinemaId,
                movieId
        );

        List<BookingJpaEntity> confirmedBookings = reportQueryPort.findConfirmedBookingsByPaidAt(
                dateRange.getFromDateTime(),
                dateRange.getToDateTime(),
                cinemaId,
                movieId
        );

        List<RefundJpaEntity> processedRefunds = reportQueryPort.findRefundsByProcessedAt(
                dateRange.getFromDateTime(),
                dateRange.getToDateTime(),
                cinemaId,
                movieId
        );

        BigDecimal confirmedRevenue = sum(confirmedBookings, BookingJpaEntity::getFinalAmount);
        BigDecimal totalTicketRevenue = sum(confirmedBookings, BookingJpaEntity::getTotalTicketPrice);
        BigDecimal totalComboRevenue = sum(confirmedBookings, BookingJpaEntity::getTotalComboPrice);

        List<RefundJpaEntity> completedRefunds = processedRefunds.stream()
                .filter(refund -> refund.getStatus() == RefundStatus.COMPLETED)
                .toList();

        BigDecimal refundedOriginalAmount = completedRefunds.stream()
                .map(RefundJpaEntity::getOriginalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefundAmount = completedRefunds.stream()
                .map(RefundJpaEntity::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossRevenue = confirmedRevenue.add(refundedOriginalAmount);
        BigDecimal netRevenue = grossRevenue.subtract(totalRefundAmount);

        long confirmedBookingsCount = allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .count();

        long pendingBookings = allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING)
                .count();

        long cancelledBookings = allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CANCELLED)
                .count();

        long requestedRefunds = countRefunds(processedRefunds, RefundStatus.REQUESTED);
        long approvedRefunds = countRefunds(processedRefunds, RefundStatus.APPROVED);
        long completedRefundsCount = countRefunds(processedRefunds, RefundStatus.COMPLETED);
        long rejectedRefunds = countRefunds(processedRefunds, RefundStatus.REJECTED);
        long cancelledRefunds = countRefunds(processedRefunds, RefundStatus.CANCELLED);

        long totalTicketsSold = confirmedBookings.stream()
                .mapToLong(booking -> booking.getTickets() == null ? 0 : booking.getTickets().size())
                .sum();

        BigDecimal averageRevenuePerBooking = confirmedBookings.isEmpty()
                ? BigDecimal.ZERO
                : netRevenue.divide(
                BigDecimal.valueOf(confirmedBookings.size()),
                2,
                RoundingMode.HALF_UP
        );

        return RevenueOverviewResponse.builder()
                .totalRevenue(netRevenue)
                .grossRevenue(grossRevenue)
                .totalTicketRevenue(totalTicketRevenue)
                .totalComboRevenue(totalComboRevenue)
                .totalRefundAmount(totalRefundAmount)
                .netRevenue(netRevenue)
                .totalBookings(allBookings.size())
                .confirmedBookings(confirmedBookingsCount)
                .pendingBookings(pendingBookings)
                .cancelledBookings(cancelledBookings)
                .totalRefunds(processedRefunds.size())
                .requestedRefunds(requestedRefunds)
                .approvedRefunds(approvedRefunds)
                .completedRefunds(completedRefundsCount)
                .rejectedRefunds(rejectedRefunds)
                .cancelledRefunds(cancelledRefunds)
                .totalTicketsSold(totalTicketsSold)
                .averageRevenuePerBooking(averageRevenuePerBooking)
                .build();
    }

    private long countRefunds(List<RefundJpaEntity> refunds, RefundStatus status) {
        return refunds.stream()
                .filter(refund -> refund.getStatus() == status)
                .count();
    }

    private BigDecimal sum(
            List<BookingJpaEntity> bookings,
            Function<BookingJpaEntity, BigDecimal> extractor
    ) {
        return bookings.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
