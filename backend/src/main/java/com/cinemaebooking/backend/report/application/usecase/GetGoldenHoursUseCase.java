package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.report.application.dto.GoldenHourResponse;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.ReportDateRangeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetGoldenHoursUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public List<GoldenHourResponse> execute(
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

        return bookings.stream()
                .collect(Collectors.groupingBy(this::buildGoldenHourKey))
                .entrySet()
                .stream()
                .map(entry -> buildResponse(entry.getKey(), entry.getValue()))
                .sorted(
                        Comparator.comparing(GoldenHourResponse::getDayOfWeek)
                                .thenComparing(GoldenHourResponse::getHour)
                )
                .toList();
    }

    private GoldenHourResponse buildResponse(String key, List<BookingJpaEntity> groupBookings) {
        String[] parts = key.split("-");
        String dayOfWeek = parts[0];
        int hour = Integer.parseInt(parts[1]);

        BigDecimal revenue = sum(groupBookings, BookingJpaEntity::getFinalAmount);

        long ticketSold = groupBookings.stream()
                .mapToLong(booking -> booking.getTickets() == null ? 0 : booking.getTickets().size())
                .sum();

        return GoldenHourResponse.builder()
                .dayOfWeek(dayOfWeek)
                .hour(hour)
                .bookingCount(groupBookings.size())
                .ticketSold(ticketSold)
                .revenue(revenue)
                .build();
    }

    private String buildGoldenHourKey(BookingJpaEntity booking) {
        Instant startTime = booking.getShowtimeStartTime();

        if (startTime == null) {
            return "UNKNOWN-0";
        }

        LocalDateTime localDateTime = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
        return localDateTime.getDayOfWeek().name() + "-" + localDateTime.getHour();
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
