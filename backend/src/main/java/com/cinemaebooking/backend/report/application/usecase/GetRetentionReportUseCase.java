package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.dto.RetentionReportResponse;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRetentionReportUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public RetentionReportResponse execute(
            LocalDate fromDate,
            LocalDate toDate,
            Long cinemaId,
            Long movieId
    ) {
        ReportDateRange dateRange = reportDateRangeValidator.validateAndBuild(fromDate, toDate);

        List<BookingJpaEntity> bookings = reportQueryPort.findConfirmedBookingsByPaidAt(
                dateRange.getFromDateTime(),
                dateRange.getToDateTime(),
                cinemaId,
                movieId
        );

        Map<Long, List<BookingJpaEntity>> groupedByUser = bookings.stream()
                .filter(booking -> booking.getUser() != null)
                .collect(Collectors.groupingBy(booking -> booking.getUser().getId()));

        long totalCustomers = groupedByUser.size();

        long returningCustomers = groupedByUser.values()
                .stream()
                .filter(userBookings -> userBookings.size() >= 2)
                .count();

        long oneTimeCustomers = groupedByUser.values()
                .stream()
                .filter(userBookings -> userBookings.size() == 1)
                .count();

        BigDecimal retentionRate = totalCustomers == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(returningCustomers)
                  .multiply(BigDecimal.valueOf(100))
                  .divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP);

        BigDecimal averageBookingsPerCustomer = totalCustomers == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(bookings.size())
                  .divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP);

        return RetentionReportResponse.builder()
                .totalCustomers(totalCustomers)
                .returningCustomers(returningCustomers)
                .oneTimeCustomers(oneTimeCustomers)
                .retentionRate(retentionRate)
                .averageBookingsPerCustomer(averageBookingsPerCustomer)
                .build();
    }
}