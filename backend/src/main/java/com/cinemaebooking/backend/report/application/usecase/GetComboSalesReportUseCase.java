package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking_combo.infrastructure.persistence.entity.BookingComboJpaEntity;
import com.cinemaebooking.backend.report.application.dto.ComboSalesReportResponse;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.report.application.validator.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetComboSalesReportUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public List<ComboSalesReportResponse> execute(
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

        return bookings.stream()
                .filter(booking -> booking.getCombos() != null)
                .flatMap(booking -> booking.getCombos().stream())
                .collect(Collectors.groupingBy(BookingComboJpaEntity::getComboId))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<BookingComboJpaEntity> combos = entry.getValue();
                    BookingComboJpaEntity first = combos.get(0);

                    long quantitySold = combos.stream()
                            .mapToLong(combo -> combo.getQuantity() == null ? 0 : combo.getQuantity())
                            .sum();

                    BigDecimal totalRevenue = combos.stream()
                            .map(BookingComboJpaEntity::getTotalPrice)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ComboSalesReportResponse.builder()
                            .comboId(first.getComboId())
                            .comboName(first.getComboName())
                            .quantitySold(quantitySold)
                            .totalRevenue(totalRevenue)
                            .build();
                })
                .sorted(Comparator.comparing(ComboSalesReportResponse::getTotalRevenue).reversed())
                .toList();
    }
}