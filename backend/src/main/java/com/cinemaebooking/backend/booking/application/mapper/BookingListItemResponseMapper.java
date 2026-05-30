package com.cinemaebooking.backend.booking.application.mapper;

import com.cinemaebooking.backend.booking.application.dto.BookingListItemResponse;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingListItemResponseMapper {

    public BookingListItemResponse toListItemResponse(Booking booking) {
        if (booking == null) return null;

        return new BookingListItemResponse(
                booking.getId() != null ? booking.getId().getValue() : null,
                booking.getBookingCode(),
                booking.getMovieId(),
                booking.getMovieTitle(),
                booking.getShowtimeStartTime(),
                booking.getFinalAmount(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getPaidAt()
        );
    }
}