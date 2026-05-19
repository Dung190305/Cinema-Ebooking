package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.dto.BookingListItemResponse;
import com.cinemaebooking.backend.booking.application.mapper.BookingListItemResponseMapper;
import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserBookingsUseCase {

    private final BookingRepository bookingRepository;
    private final BookingListItemResponseMapper mapper;

    @Transactional(readOnly = true)
    public Page<BookingListItemResponse> execute(Long userId, BookingStatus status, Pageable pageable) {
        return bookingRepository.findByUserId(userId, status, pageable)
                .map(mapper::toListItemResponse);
    }
}
