package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.dto.BookingDetailResponse;
import com.cinemaebooking.backend.booking.application.mapper.BookingDetailResponseMapper;
import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetPendingBookingUseCase {

    private final BookingRepository bookingRepository;
    private final BookingDetailResponseMapper mapper;

    @Transactional(readOnly = true)
    public BookingDetailResponse execute(Long userId, Long showtimeId) {
        var bookingOpt = bookingRepository.findByUserIdAndShowtimeIdAndStatus(userId, showtimeId, BookingStatus.PENDING);

        if (bookingOpt.isEmpty()) {
            throw BookingExceptions.notFoundForUserAndShowtime(userId, showtimeId);
        }

        var booking = bookingOpt.get();
        // Kiểm tra nếu booking đã quá hạn thanh toán (ví dụ: expiredAt < hiện tại)
        if (booking.getExpiredAt() != null && booking.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw BookingExceptions.expired(booking.getId());
        }

        return mapper.toDetailResponse(booking);
    }
}