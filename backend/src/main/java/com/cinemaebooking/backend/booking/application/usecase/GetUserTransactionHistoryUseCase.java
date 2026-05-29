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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetUserTransactionHistoryUseCase {

    private final BookingRepository bookingRepository;
    private final BookingListItemResponseMapper mapper;

    @Transactional(readOnly = true)
    public Page<BookingListItemResponse> execute(
            Long userId,
            Long movieId,
            BookingStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;

        return bookingRepository.findAllForUser(
                userId,
                movieId,
                status,
                fromDateTime,
                toDateTime,
                pageable
        ).map(mapper::toListItemResponse);
    }
}