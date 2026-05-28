package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.dto.RoomPerformanceResponse;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.ReportDateRangeValidator;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRoomPerformanceUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public List<RoomPerformanceResponse> execute(
            LocalDate fromDate,
            LocalDate toDate,
            Long cinemaId,
            Long movieId
    ) {
        ReportDateRange dateRange = reportDateRangeValidator.validateAndBuild(fromDate, toDate);

        Instant showtimeFrom = dateRange.getFromDateTime()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant showtimeTo = dateRange.getToDateTime()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        List<BookingJpaEntity> bookings = reportQueryPort.findConfirmedBookingsByShowtimeStartTime(
                showtimeFrom,
                showtimeTo,
                cinemaId,
                movieId
        );

        List<ShowtimeJpaEntity> showtimes = reportQueryPort.findShowtimesByStartTime(
                showtimeFrom,
                showtimeTo,
                cinemaId,
                movieId
        );

        return bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getCinemaName() + " - " + booking.getRoomName()))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<BookingJpaEntity> roomBookings = entry.getValue();
                    BookingJpaEntity firstBooking = roomBookings.get(0);

                    BigDecimal revenue = sum(roomBookings, BookingJpaEntity::getFinalAmount);

                    List<TicketJpaEntity> tickets = roomBookings.stream()
                            .filter(booking -> booking.getTickets() != null)
                            .flatMap(booking -> booking.getTickets().stream())
                            .toList();

                    long ticketSold = tickets.size();

                    Set<Long> showtimeIds = tickets.stream()
                            .map(ticket -> ticket.getShowtimeSeat())
                            .filter(Objects::nonNull)
                            .map(showtimeSeat -> showtimeSeat.getShowtime())
                            .filter(Objects::nonNull)
                            .map(showtime -> showtime.getId())
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    Long roomId = resolveRoomId(tickets);

                    BigDecimal occupancyRate = calculateRoomOccupancyRate(
                            firstBooking.getCinemaName(),
                            firstBooking.getRoomName(),
                            ticketSold,
                            tickets,
                            showtimes
                    );

                    return RoomPerformanceResponse.builder()
                            .roomId(roomId)
                            .roomName(firstBooking.getRoomName())
                            .cinemaName(firstBooking.getCinemaName())
                            .revenue(revenue)
                            .bookingCount(roomBookings.size())
                            .ticketSold(ticketSold)
                            .showtimeCount(showtimeIds.size())
                            .occupancyRate(occupancyRate)
                            .build();
                })
                .sorted(Comparator.comparing(RoomPerformanceResponse::getRevenue).reversed())
                .toList();
    }

    private Long resolveRoomId(List<TicketJpaEntity> tickets) {
        return tickets.stream()
                .map(ticket -> ticket.getShowtimeSeat())
                .filter(Objects::nonNull)
                .map(showtimeSeat -> showtimeSeat.getShowtime())
                .filter(Objects::nonNull)
                .map(showtime -> showtime.getRoom())
                .filter(Objects::nonNull)
                .map(room -> room.getId())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal calculateRoomOccupancyRate(
            String cinemaName,
            String roomName,
            long ticketSold,
            List<TicketJpaEntity> tickets,
            List<ShowtimeJpaEntity> showtimes
    ) {
        int totalCapacity = showtimes.stream()
                .filter(showtime -> showtime.getRoom() != null)
                .filter(showtime -> showtime.getRoom().getCinema() != null)
                .filter(showtime -> cinemaName.equals(showtime.getRoom().getCinema().getName()))
                .filter(showtime -> roomName.equals(showtime.getRoom().getName()))
                .map(showtime -> showtime.getRoom().getTotalSeats())
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        if (totalCapacity == 0) {
            totalCapacity = calculateCapacityFromSoldTicketShowtimes(tickets);
        }

        if (totalCapacity == 0) {
            return ticketSold > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(ticketSold)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCapacity), 2, RoundingMode.HALF_UP);
    }

    private int calculateCapacityFromSoldTicketShowtimes(List<TicketJpaEntity> tickets) {
        Map<Long, Integer> capacityByShowtime = new LinkedHashMap<>();

        for (TicketJpaEntity ticket : tickets) {
            if (ticket.getShowtimeSeat() == null
                    || ticket.getShowtimeSeat().getShowtime() == null
                    || ticket.getShowtimeSeat().getShowtime().getRoom() == null) {
                continue;
            }

            Long showtimeId = ticket.getShowtimeSeat().getShowtime().getId();
            Integer totalSeats = ticket.getShowtimeSeat().getShowtime().getRoom().getTotalSeats();

            if (showtimeId != null && totalSeats != null && totalSeats > 0) {
                capacityByShowtime.putIfAbsent(showtimeId, totalSeats);
            }
        }

        return capacityByShowtime.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
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