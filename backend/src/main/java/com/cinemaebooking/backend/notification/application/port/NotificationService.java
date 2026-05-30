package com.cinemaebooking.backend.notification.application.port;

import com.cinemaebooking.backend.notification.application.dto.BookingEmailData;
import com.cinemaebooking.backend.notification.domain.model.Notification;

public interface NotificationService {

    void sendPaymentSuccessNotification(
            Long userId,
            String userEmail,
            String userName,
            BookingEmailData booking
    );

    void sendReminderNotification(
            Long userId,
            String userEmail,
            String userName,
            BookingEmailData booking
    );

    Notification saveNotification(Notification notification);
}
