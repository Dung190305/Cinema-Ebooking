package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.report.application.dto.CinemaPerformanceResponse;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.*;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCinemaPerformanceUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public List<CinemaPerformanceResponse> execute(
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

        Instant showtimeFrom = dateRange.getFromDateTime()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant showtimeTo = dateRange.getToDateTime()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        List<ShowtimeJpaEntity> showtimes = reportQueryPort.findShowtimesByStartTime(
                showtimeFrom,
                showtimeTo,
                cinemaId,
                movieId
        );

        return bookings.stream()
                .collect(Collectors.groupingBy(BookingJpaEntity::getCinemaName))
                .entrySet()
                .stream()
                .map(entry -> {
                    String cinemaName = entry.getKey();
                    List<BookingJpaEntity> cinemaBookings = entry.getValue();

                    BigDecimal revenue = sum(cinemaBookings, BookingJpaEntity::getFinalAmount);

                    List<TicketJpaEntity> tickets = cinemaBookings.stream()
                            .filter(booking -> booking.getTickets() != null)
                            .flatMap(booking -> booking.getTickets().stream())
                            .toList();

                    long ticketSold = tickets.size();
                    Long resolvedCinemaId = resolveCinemaId(tickets);

                    BigDecimal occupancyRate = calculateCinemaOccupancyRate(
                            cinemaName,
                            ticketSold,
                            showtimes
                    );

                    return CinemaPerformanceResponse.builder()
                            .cinemaId(resolvedCinemaId)
                            .cinemaName(cinemaName)
                            .revenue(revenue)
                            .bookingCount(cinemaBookings.size())
                            .ticketSold(ticketSold)
                            .occupancyRate(occupancyRate)
                            .build();
                })
                .sorted(Comparator.comparing(CinemaPerformanceResponse::getRevenue).reversed())
                .toList();
    }

    private Long resolveCinemaId(List<TicketJpaEntity> tickets) {
        return tickets.stream()
                .map(ticket -> ticket.getShowtimeSeat().getShowtime().getRoom().getCinema().getId())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal calculateCinemaOccupancyRate(
            String cinemaName,
            long ticketSold,
            List<ShowtimeJpaEntity> showtimes
    ) {
        int totalCapacity = showtimes.stream()
                .filter(showtime -> showtime.getRoom() != null)
                .filter(showtime -> showtime.getRoom().getCinema() != null)
                .filter(showtime -> cinemaName.equals(showtime.getRoom().getCinema().getName()))
                .map(showtime -> showtime.getRoom().getTotalSeats())
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        if (totalCapacity == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(ticketSold)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCapacity), 2, RoundingMode.HALF_UP);
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