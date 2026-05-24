package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.booking_coupon.application.usecase.ReleaseBookingCouponUseCase;
import com.cinemaebooking.backend.ticket.application.usecase.ReleaseSeatsUseCase;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ScheduledJob tự động hủy booking PENDING hết hạn và giải phóng ghế về AVAILABLE.
 *
 * <p>Chạy mỗi 1 phút, tìm tất cả booking có status = PENDING và expiredAt <= now,
 * đánh dấu CANCELLED và trả ghế về AVAILABLE.
 *
 * <p>Trường hợp phủ:
 * <ul>
 *   <li>User tạo booking nhưng không tạo payment → booking tự hủy sau 15 phút</li>
 *   <li>Payment expired cleanup đã xử lý phần lớn, nhưng job này là lớp phòng thủ cuối</li>
 * </ul>
 *
 * @author Cursor Agent
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpiredCleanupJob {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingRepository bookingRepository;
    private final ReleaseSeatsUseCase releaseSeatsUseCase;
    private final ReleaseBookingCouponUseCase releaseCouponUseCase;

    /**
     * Chạy mỗi 1 phút để cleanup booking hết hạn.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> expiredBookings = bookingJpaRepository
                .findAllByExpiredAtBeforeAndStatusAndDeletedFalse(now, BookingStatus.PENDING)
                .stream()
                .map(entity -> {
                    var mapper = bookingRepository.getClass().getSimpleName();
                    return bookingRepository.findById(entity.getId()).orElse(null);
                })
                .filter(b -> b != null)
                .toList();

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("[BookingExpiredCleanupJob] Found {} expired bookings to cleanup", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                processExpiredBooking(booking);
            } catch (Exception e) {
                log.error("[BookingExpiredCleanupJob] Failed to process expired booking: {}",
                        booking.getId(), e);
            }
        }
    }

    private void processExpiredBooking(Booking booking) {
        log.info("[BookingExpiredCleanupJob] Canceling expired booking {}", booking.getId());

        // 1. Booking domain: PENDING → CANCELLED
        booking.cancel();

        // 2. Tickets: CANCELLED
        booking.getTickets().forEach(Ticket::cancel);

        // 3. Giải phóng ghế về AVAILABLE
        releaseSeatsUseCase.execute(booking.getShowtimeId(), booking.getTickets());

        // 4. Giải phóng coupon nếu có
        releaseCouponUseCase.execute(booking.getId().getValue());

        // 5. Persist
        bookingRepository.save(booking);

        log.info("[BookingExpiredCleanupJob] Successfully canceled expired booking {}",
                booking.getId());
    }
}
