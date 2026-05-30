package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.report.application.dto.MoviePerformanceResponse;
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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetMoviePerformanceUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public List<MoviePerformanceResponse> execute(
            LocalDate fromDate,
            LocalDate toDate,
            Long cinemaId,
            Long movieId
    ) {
        ReportDateRange dateRange = reportDateRangeValidator.validateAndBuild(fromDate, toDate);

        List<BookingJpaEntity> confirmedBookings = reportQueryPort.findConfirmedBookingsByPaidAt(
                dateRange.getFromDateTime(),
                dateRange.getToDateTime(),
                cinemaId,
                movieId
        );

        BigDecimal totalRevenue = sum(confirmedBookings, BookingJpaEntity::getFinalAmount);

        return confirmedBookings.stream()
                .collect(Collectors.groupingBy(BookingJpaEntity::getMovieTitle))
                .entrySet()
                .stream()
                .map(entry -> {
                    String movieTitle = entry.getKey();
                    List<BookingJpaEntity> bookings = entry.getValue();

                    BigDecimal revenue = sum(bookings, BookingJpaEntity::getFinalAmount);

                    long ticketSold = bookings.stream()
                            .mapToLong(booking -> booking.getTickets() == null ? 0 : booking.getTickets().size())
                            .sum();

                    BigDecimal revenueShare = totalRevenue.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : revenue.multiply(BigDecimal.valueOf(100))
                              .divide(totalRevenue, 2, RoundingMode.HALF_UP);

                    Long resolvedMovieId = resolveMovieId(bookings);

                    return MoviePerformanceResponse.builder()
                            .movieId(resolvedMovieId)
                            .movieTitle(movieTitle)
                            .bookingCount(bookings.size())
                            .ticketSold(ticketSold)
                            .revenue(revenue)
                            .revenueShare(revenueShare)
                            .build();
                })
                .sorted(Comparator.comparing(MoviePerformanceResponse::getRevenue).reversed())
                .toList();
    }

    private Long resolveMovieId(List<BookingJpaEntity> bookings) {
        return bookings.stream()
                .filter(booking -> booking.getTickets() != null)
                .flatMap(booking -> booking.getTickets().stream())
                .map(ticket -> ticket.getShowtimeSeat().getShowtime().getMovie().getId())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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