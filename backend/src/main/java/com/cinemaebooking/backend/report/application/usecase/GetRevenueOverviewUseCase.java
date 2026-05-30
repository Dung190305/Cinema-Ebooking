package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.dto.RevenueOverviewResponse;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.*;
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

        BigDecimal totalRevenue = sum(confirmedBookings, BookingJpaEntity::getFinalAmount);
        BigDecimal totalTicketRevenue = sum(confirmedBookings, BookingJpaEntity::getTotalTicketPrice);
        BigDecimal totalComboRevenue = sum(confirmedBookings, BookingJpaEntity::getTotalComboPrice);

        long confirmedCount = allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .count();

        long pendingCount = allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING)
                .count();

        long cancelledCount = allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CANCELLED)
                .count();

        long totalTicketsSold = confirmedBookings.stream()
                .mapToLong(booking -> booking.getTickets() == null ? 0 : booking.getTickets().size())
                .sum();

        BigDecimal averageRevenuePerBooking = confirmedBookings.isEmpty()
                ? BigDecimal.ZERO
                : totalRevenue.divide(
                BigDecimal.valueOf(confirmedBookings.size()),
                2,
                RoundingMode.HALF_UP
        );

        return RevenueOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTicketRevenue(totalTicketRevenue)
                .totalComboRevenue(totalComboRevenue)
                .totalBookings(allBookings.size())
                .confirmedBookings(confirmedCount)
                .pendingBookings(pendingCount)
                .cancelledBookings(cancelledCount)
                .totalTicketsSold(totalTicketsSold)
                .averageRevenuePerBooking(averageRevenuePerBooking)
                .build();
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