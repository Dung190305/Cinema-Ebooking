package com.cinemaebooking.backend.showtime.application.usecase.showtime;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.ShowtimeExceptions;
import com.cinemaebooking.backend.room.application.port.RoomRepository;
import com.cinemaebooking.backend.room.domain.valueObject.RoomId;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeCancelResult;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeCancelResult.RefundError;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeResponse;
import com.cinemaebooking.backend.showtime.application.mapper.ShowtimeResponseMapper;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import com.cinemaebooking.backend.showtime.application.service.AutoRefundForShowtimeCancellationService;
import com.cinemaebooking.backend.showtime.domain.model.Showtime;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelShowtimeUseCase {

    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeResponseMapper mapper;
    private final AutoRefundForShowtimeCancellationService autoRefundService;

    /**
     * Cancels the showtime and creates 100% auto-refunds for all CONFIRMED bookings.
     * <p>
     * The showtime cancellation itself is transactional.  Each per-booking refund
     * runs in its own nested {@code REQUIRES_NEW} transaction (inside
     * {@link AutoRefundForShowtimeCancellationService}) so a failure on one booking
     * does not roll back the others.
     * <p>
     * Partial failures are collected and returned in {@link ShowtimeCancelResult#refundErrors()}.
     */
    @Transactional
    public ShowtimeCancelResult execute(ShowtimeId id) {
        if (id == null) {
            throw CommonExceptions.invalidInput("Showtime id must not be null");
        }

        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> ShowtimeExceptions.notFound(id));

        // Mark showtime as cancelled first — this is the primary operation
        showtime.cancel();
        Showtime saved = showtimeRepository.update(showtime);

        // Process auto-refunds for all CONFIRMED bookings of this showtime
        List<Booking> confirmedBookings = bookingRepository
                .findAllByShowtimeIdAndStatus(id.getValue(), BookingStatus.CONFIRMED);

        List<RefundError> errors = new ArrayList<>();

        for (Booking booking : confirmedBookings) {
            RefundError error = autoRefundService.processRefundForBooking(booking);
            if (error != null) {
                errors.add(error);
            }
        }

        if (!errors.isEmpty()) {
            log.warn("CancelShowtimeUseCase: showtime {} cancelled but {}/{} refunds failed",
                    id.getValue(), errors.size(), confirmedBookings.size());
        }

        Long cinemaId = roomRepository.getCinemaIdByRoomId(RoomId.of(saved.getRoomId()));
        ShowtimeResponse response = mapper.toResponse(saved, cinemaId);

        return new ShowtimeCancelResult(response, errors);
    }
}