package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.dto.ReportGroupBy;
import com.cinemaebooking.backend.report.application.dto.RevenueTrendPointResponse;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRevenueTrendUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public List<RevenueTrendPointResponse> execute(
            LocalDate fromDate,
            LocalDate toDate,
            Long cinemaId,
            Long movieId,
            ReportGroupBy groupBy
    ) {
        ReportDateRange dateRange = reportDateRangeValidator.validateAndBuild(fromDate, toDate);

        ReportGroupBy safeGroupBy = groupBy == null ? ReportGroupBy.DAY : groupBy;

        List<BookingJpaEntity> confirmedBookings = reportQueryPort.findConfirmedBookingsByPaidAt(
                dateRange.getFromDateTime(),
                dateRange.getToDateTime(),
                cinemaId,
                movieId
        );

        return confirmedBookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> buildGroupLabel(booking.getPaidAt(), safeGroupBy),
                        TreeMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<BookingJpaEntity> bookings = entry.getValue();

                    BigDecimal revenue = sum(bookings, BookingJpaEntity::getFinalAmount);
                    BigDecimal ticketRevenue = sum(bookings, BookingJpaEntity::getTotalTicketPrice);
                    BigDecimal comboRevenue = sum(bookings, BookingJpaEntity::getTotalComboPrice);

                    long ticketCount = bookings.stream()
                            .mapToLong(booking -> booking.getTickets() == null ? 0 : booking.getTickets().size())
                            .sum();

                    return RevenueTrendPointResponse.builder()
                            .label(entry.getKey())
                            .revenue(revenue)
                            .ticketRevenue(ticketRevenue)
                            .comboRevenue(comboRevenue)
                            .bookingCount(bookings.size())
                            .ticketCount(ticketCount)
                            .build();
                })
                .toList();
    }

    private String buildGroupLabel(LocalDateTime paidAt, ReportGroupBy groupBy) {
        if (paidAt == null) {
            return "UNKNOWN";
        }

        return switch (groupBy) {
            case DAY -> paidAt.toLocalDate().format(DateTimeFormatter.ISO_DATE);
            case MONTH -> paidAt.getYear() + "-" + String.format("%02d", paidAt.getMonthValue());
            case YEAR -> String.valueOf(paidAt.getYear());
        };
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