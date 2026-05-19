package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.dto.BookingDetailResponse;
import com.cinemaebooking.backend.booking.application.dto.CreateBookingRequest;
import com.cinemaebooking.backend.booking.application.mapper.BookingDetailResponseMapper;
import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.application.port.BookingLoyaltyPort;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking_combo.application.port.ComboInternalService;
import com.cinemaebooking.backend.booking_combo.domain.model.BookingCombo;
import com.cinemaebooking.backend.booking_coupon.application.port.CouponInternalService;
import com.cinemaebooking.backend.booking_coupon.domain.model.BookingCoupon;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeInternalService;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateBookingUseCase {

    private final BookingRepository bookingRepository;
    private final BookingDetailResponseMapper mapper;
    private final ShowtimeInternalService showtimeService;
    private final ComboInternalService comboService;
    private final CouponInternalService couponService;
    private final BookingLoyaltyPort loyaltyPort;

    private static final int MAX_SEATS_PER_BOOKING = 8;

    @Transactional
    public BookingDetailResponse execute(CreateBookingRequest request) {

        if (request.getShowTimeSeatIds().size() > MAX_SEATS_PER_BOOKING) {
            throw CommonExceptions.invalidInput(
                    "Chỉ được đặt tối đa 8 ghế cho mỗi booking."
            );
        }

        var showtimeSnapshot = showtimeService.getSnapshot(request.getShowtimeId());

        List<Ticket> tickets = showtimeService.getTicketsBySeatIds(
                request.getShowtimeId(),
                request.getShowTimeSeatIds()
        );

        List<BookingCombo> combos = comboService.getBookingCombos(request.getCombos());

        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .userId(request.getUserId())
                .showtimeId(request.getShowtimeId())
                .movieTitle(showtimeSnapshot.getMovieTitle())
                .cinemaName(showtimeSnapshot.getCinemaName())
                .roomName(showtimeSnapshot.getRoomName())
                .showtimeStartTime(showtimeSnapshot.getStartTime())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .tickets(tickets)
                .combos(combos)
                .build();

        var tierInfo = loyaltyPort.getMembershipTierInfo(request.getUserId());
        if (tierInfo != null) {
            booking.setMembershipTierName(tierInfo.tierName());
            BigDecimal discount = tierInfo.discountPercent() != null ? tierInfo.discountPercent() : BigDecimal.ZERO;
            booking.applyTierDiscount(discount);
        }

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            BigDecimal currentSubtotal = booking.calculateSubtotal();
            BookingCoupon couponData = couponService.validateAndGetCoupon(
                    request.getUserId(),
                    request.getCouponCode(),
                    currentSubtotal
            );
            booking.applyCoupon(couponData);
        }

        booking.calculateTotal();
        Booking savedBooking = bookingRepository.save(booking);

        return mapper.toDetailResponse(savedBooking);
    }

    private String generateBookingCode() {
        return "BOK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
