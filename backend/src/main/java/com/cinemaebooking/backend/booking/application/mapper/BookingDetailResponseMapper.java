package com.cinemaebooking.backend.booking.application.mapper;

import com.cinemaebooking.backend.booking.application.dto.BookingDetailResponse;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingDetailResponseMapper {

    public BookingDetailResponse toDetailResponse(Booking booking) {
        if (booking == null) return null;

        return BookingDetailResponse.builder()
                .bookingId(booking.getId() != null ? booking.getId().getValue() : null)
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .showtimeId(booking.getShowtimeId())
                .movieTitle(booking.getMovieTitle())
                .cinemaName(booking.getCinemaName())
                .roomName(booking.getRoomName())
                .showtimeStartTime(booking.getShowtimeStartTime())
                .totalTicketPrice(booking.getTotalTicketPrice())
                .totalComboPrice(booking.getTotalComboPrice())
                .tierDiscountAmount(booking.getTierDiscountAmount())
                .couponDiscountAmount(booking.getCouponDiscountAmount())
                .discountAmount(
                        (booking.getTierDiscountAmount() != null ? booking.getTierDiscountAmount() : java.math.BigDecimal.ZERO)
                        .add(booking.getCouponDiscountAmount() != null ? booking.getCouponDiscountAmount() : java.math.BigDecimal.ZERO)
                )
                .finalAmount(booking.getFinalAmount())
                .membershipTierName(booking.getMembershipTierName())
                .membershipDiscountPercent(booking.getMembershipDiscountPercent())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .expiredAt(booking.getExpiredAt())
                .paidAt(booking.getPaidAt())
                .seats(mapSeats(booking))
                .combos(mapCombos(booking))
                .coupon(mapCoupon(booking))
                .build();
    }

    private List<BookingDetailResponse.SeatInfo> mapSeats(Booking booking) {
        if (booking.getTickets() == null) return Collections.emptyList();
        return booking.getTickets().stream()
                .map(ticket -> BookingDetailResponse.SeatInfo.builder()
                        .showtimeSeatId(ticket.getShowtimeSeatId())
                        .seatType(ticket.getSeatType())
                        .seatName(ticket.getSeatName())
                        .price(ticket.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private List<BookingDetailResponse.ComboInfo> mapCombos(Booking booking) {
        if (booking.getCombos() == null) return Collections.emptyList();
        return booking.getCombos().stream()
                .map(combo -> BookingDetailResponse.ComboInfo.builder()
                        .comboId(combo.getComboId())
                        .comboName(combo.getComboName())
                        .quantity(combo.getQuantity())
                        .unitPrice(combo.getUnitPrice())
                        .totalPrice(combo.getTotalPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private BookingDetailResponse.CouponInfo mapCoupon(Booking booking) {
        if (booking.getCoupon() == null) return null;
        return BookingDetailResponse.CouponInfo.builder()
                .couponId(booking.getCoupon().getCouponId())
                .code(booking.getCoupon().getCode())
                .discountValue(booking.getCoupon().getDiscountValue())
                .build();
    }
}