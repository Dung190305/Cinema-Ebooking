package com.cinemaebooking.backend.review.infrastructure.adapter;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.review.application.port.ReviewEligibilityPort;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewEligibilityAdapter implements ReviewEligibilityPort {

    private final BookingJpaRepository bookingJpaRepository;

    @Override
    public boolean isEligibleToReview(Long userId, Long bookingId) {
        BookingJpaEntity booking = bookingJpaRepository.findById(bookingId).orElse(null);

        if (booking == null) return false;
        if (!booking.getUser().getId().equals(userId)) return false;
        if (booking.isDeleted()) return false;
        if (booking.getStatus() != BookingStatus.CONFIRMED) return false;

        List<TicketJpaEntity> tickets = booking.getTickets();
        if (tickets == null || tickets.isEmpty()) return false;

        return tickets.stream()
                .filter(t -> !t.isDeleted())
                .anyMatch(t -> t.getStatus() == TicketStatus.USED);
    }
}
