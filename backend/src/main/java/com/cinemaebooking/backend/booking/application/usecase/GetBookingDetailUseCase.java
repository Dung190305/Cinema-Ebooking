package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.dto.BookingDetailResponse;
import com.cinemaebooking.backend.booking.application.mapper.BookingDetailResponseMapper;
import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBookingDetailUseCase {

    private final BookingRepository bookingRepository;
    private final BookingDetailResponseMapper mapper;

    @Transactional(readOnly = true)
    public BookingDetailResponse execute(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(mapper::toDetailResponse)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));
    }

    @Transactional(readOnly = true)
    public BookingDetailResponse executeByBookingCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode)
                .map(mapper::toDetailResponse)
                .orElseThrow(() -> BookingExceptions.notFoundByCode(bookingCode));
    }
}