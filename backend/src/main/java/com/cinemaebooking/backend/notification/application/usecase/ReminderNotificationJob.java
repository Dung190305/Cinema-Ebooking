package com.cinemaebooking.backend.notification.application.usecase;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.notification.application.dto.BookingEmailData;
import com.cinemaebooking.backend.notification.application.port.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * ScheduledJob gửi thông báo nhắc nhở giờ chiếu.
 *
 * <p>Chạy mỗi 30 phút, tìm booking CONFIRMED có showtime trong khoảng
 * [now + hoursBefore - 10min, now + hoursBefore + 10min].
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderNotificationJob {

    private final BookingJpaRepository bookingJpaRepository;
    private final NotificationService notificationService;

    @Value("${notification.reminder.hours-before:2}")
    private int hoursBefore;

    @Value("${notification.reminder.check-window-minutes:20}")
    private int windowMinutes;

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void sendReminders() {
        Instant now = Instant.now();
        int halfWindow = windowMinutes / 2;
        Instant fromTime = now.plusSeconds((hoursBefore * 60L - halfWindow) * 60);
        Instant toTime = now.plusSeconds((hoursBefore * 60L + halfWindow) * 60);

        List<BookingJpaEntity> bookings = bookingJpaRepository.findBookingsNeedingReminder(
                BookingStatus.CONFIRMED, fromTime, toTime);

        if (bookings.isEmpty()) {
            return;
        }

        log.info("[ReminderNotificationJob] Found {} bookings needing reminder", bookings.size());

        for (BookingJpaEntity booking : bookings) {
            try {
                sendReminder(booking);
            } catch (Exception e) {
                log.error("[ReminderNotificationJob] Failed to send reminder for booking {}: {}",
                        booking.getBookingCode(), e.getMessage());
            }
        }
    }

    private void sendReminder(BookingJpaEntity booking) {
        var user = booking.getUser();

        BookingEmailData bookingData = BookingEmailData.builder()
                .bookingId(booking.getId())
                .bookingCode(booking.getBookingCode())
                .movieTitle(booking.getMovieTitle())
                .cinemaName(booking.getCinemaName())
                .roomName(booking.getRoomName())
                .showtimeStartTime(booking.getShowtimeStartTime())
                .build();

        notificationService.sendReminderNotification(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                bookingData
        );

        log.info("[ReminderNotificationJob] Reminder sent for booking {}", booking.getBookingCode());
    }
}
