package com.cinemaebooking.backend.review.application.usecase;

import com.cinemaebooking.backend.review.application.dto.TicketCheckResponse;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Use case kiểm tra user có vé đã check-in cho 1 phim hay chưa.
 * Dùng cho GET /reviews/movies/{movieId}/check-ticket
 */
@Service
@RequiredArgsConstructor
public class CheckTicketEligibilityUseCase {

    private final BookingJpaRepository bookingJpaRepository;

    /**
     * Kiểm tra user có vé đã check-in (ticket status = USED) cho phim movieId.
     * Chỉ tính booking có status = CONFIRMED.
     *
     * @param userId  ID của user
     * @param movieId ID của phim
     * @return TicketCheckResponse chứa kết quả kiểm tra
     */
    @Transactional(readOnly = true)
    public TicketCheckResponse execute(Long userId, Long movieId) {
        List<BookingJpaEntity> checkedInBookings =
                bookingJpaRepository.findCheckedInBookingsByUserAndMovie(userId, movieId);

        int count = checkedInBookings.size();
        boolean hasCheckedIn = count > 0;

        String latestBookingCode = null;
        Long latestBookingId = null;
        if (hasCheckedIn) {
            BookingJpaEntity latest = checkedInBookings.stream()
                    .max(Comparator.comparing(BookingJpaEntity::getCreatedAt))
                    .orElse(null);
            if (latest != null) {
                latestBookingCode = latest.getBookingCode();
                latestBookingId = latest.getId();
            }
        }

        return TicketCheckResponse.builder()
                .hasCheckedInTicket(hasCheckedIn)
                .checkedInCount(count)
                .latestBookingCode(latestBookingCode)
                .latestBookingId(latestBookingId)
                .build();
    }
}
