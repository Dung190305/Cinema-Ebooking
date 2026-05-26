package com.cinemaebooking.backend.notification.infrastructure.adapter;

import com.cinemaebooking.backend.notification.application.dto.BookingEmailData;
import com.cinemaebooking.backend.notification.application.port.BookingEmailService;
import com.cinemaebooking.backend.notification.application.port.NotificationService;
import com.cinemaebooking.backend.notification.application.port.QRCodeService;
import com.cinemaebooking.backend.notification.application.port.NotificationRepository;
import com.cinemaebooking.backend.notification.domain.enums.NotificationType;
import com.cinemaebooking.backend.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final BookingEmailService bookingEmailService;
    private final QRCodeService qrCodeService;

    @Override
    @Transactional
    public void sendPaymentSuccessNotification(
            Long userId,
            String userEmail,
            String userName,
            BookingEmailData booking
    ) {
        String title = "Thanh toán thành công - " + booking.getBookingCode();
        String message = buildPaymentSuccessMessage(booking);
        String qrBase64 = qrCodeService.generateQRCodeBase64(booking.getBookingCode(), 300, 300);

        saveNotification(userId, title, message, NotificationType.PAYMENT_SUCCESS,
                booking.getBookingId(), null);

        if (userEmail != null && !userEmail.isBlank()) {
            bookingEmailService.sendPaymentSuccessEmail(userEmail, userName, booking, qrBase64);
        }
    }

    @Override
    @Transactional
    public void sendReminderNotification(
            Long userId,
            String userEmail,
            String userName,
            BookingEmailData booking
    ) {
        String title = "Nhắc nhở: Phim \"" + booking.getMovieTitle() + "\" sắp chiếu";
        String message = buildReminderMessage(booking);

        saveNotification(userId, title, message, NotificationType.REMINDER,
                booking.getBookingId(), null);

        if (userEmail != null && !userEmail.isBlank()) {
            bookingEmailService.sendReminderEmail(userEmail, userName, booking);
        }
    }

    @Override
    @Transactional
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    private void saveNotification(
            Long userId,
            String title,
            String message,
            NotificationType type,
            Long bookingId,
            Long paymentId
    ) {
        Notification notification = Notification.create(userId, title, message, type, bookingId, paymentId);
        notificationRepository.save(notification);
    }

    private String buildPaymentSuccessMessage(BookingEmailData booking) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thanh toán thanh cong! Ma dat ve: ").append(booking.getBookingCode());
        sb.append(". Phim: ").append(booking.getMovieTitle());
        sb.append(". Tong tien: ").append(booking.getFinalAmount()).append(" VND");
        return sb.toString();
    }

    private String buildReminderMessage(BookingEmailData booking) {
        return "Phim \"" + booking.getMovieTitle() + "\" se chieu trong 2 gio nua tai "
                + booking.getCinemaName() + " - " + booking.getRoomName();
    }
}
