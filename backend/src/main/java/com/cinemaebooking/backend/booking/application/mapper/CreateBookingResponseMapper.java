package com.cinemaebooking.backend.booking.application.mapper;

import com.cinemaebooking.backend.booking.application.dto.CreateBookingResponse;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class CreateBookingResponseMapper {

    public CreateBookingResponse toCreateResponse(Booking booking) {
        if (booking == null) return null;

        BigDecimal tierDiscount = booking.getTierDiscountAmount() != null
                ? booking.getTierDiscountAmount() : java.math.BigDecimal.ZERO;
        BigDecimal couponDiscount = booking.getCouponDiscountAmount() != null
                ? booking.getCouponDiscountAmount() : java.math.BigDecimal.ZERO;

        return CreateBookingResponse.builder()
                .bookingId(booking.getId() != null ? booking.getId().getValue() : null)
                .bookingCode(booking.getBookingCode())
                .totalTicketPrice(booking.getTotalTicketPrice())
                .totalComboPrice(booking.getTotalComboPrice())
                .tierDiscountAmount(booking.getTierDiscountAmount())
                .couponDiscountAmount(booking.getCouponDiscountAmount())
                .discountAmount(tierDiscount.add(couponDiscount))
                .finalAmount(booking.getFinalAmount())
                .membershipTierName(booking.getMembershipTierName())
                .membershipDiscountPercent(booking.getMembershipDiscountPercent())
                .status(booking.getStatus())
                .expiredAt(booking.getExpiredAt())
                .showTimeSeatIds(booking.getTickets() != null
                        ? booking.getTickets().stream().map(Ticket::getShowtimeSeatId).collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }
}
