package com.cinemaebooking.backend.report.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking_coupon.infrastructure.persistence.entity.BookingCouponJpaEntity;
import com.cinemaebooking.backend.report.application.dto.PromotionEffectivenessResponse;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPromotionEffectivenessUseCase {

    private final ReportQueryPort reportQueryPort;
    private final ReportDateRangeValidator reportDateRangeValidator;

    @Transactional(readOnly = true)
    public List<PromotionEffectivenessResponse> execute(
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
                .filter(booking -> booking.getCoupon() != null)
                .collect(Collectors.groupingBy(booking -> booking.getCoupon().getCouponCode()))
                .entrySet()
                .stream()
                .map(entry -> {
                    String couponCode = entry.getKey();
                    List<BookingJpaEntity> couponBookings = entry.getValue();

                    BigDecimal totalDiscount = couponBookings.stream()
                            .map(BookingJpaEntity::getCoupon)
                            .filter(Objects::nonNull)
                            .map(BookingCouponJpaEntity::getDiscountAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal revenueGenerated = couponBookings.stream()
                            .map(BookingJpaEntity::getFinalAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageDiscount = couponBookings.isEmpty()
                            ? BigDecimal.ZERO
                            : totalDiscount.divide(
                            BigDecimal.valueOf(couponBookings.size()),
                            2,
                            RoundingMode.HALF_UP
                    );

                    return PromotionEffectivenessResponse.builder()
                            .couponCode(couponCode)
                            .usedCount(couponBookings.size())
                            .totalDiscount(totalDiscount)
                            .revenueGenerated(revenueGenerated)
                            .averageDiscountPerBooking(averageDiscount)
                            .build();
                })
                .sorted(Comparator.comparing(PromotionEffectivenessResponse::getUsedCount).reversed())
                .toList();
    }
}