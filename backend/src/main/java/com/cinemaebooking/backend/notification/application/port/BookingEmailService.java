package com.cinemaebooking.backend.notification.application.port;

import com.cinemaebooking.backend.notification.application.dto.BookingEmailData;

public interface BookingEmailService {

    void sendPaymentSuccessEmail(String to, String userName, BookingEmailData booking, String qrCodeBase64);

    void sendReminderEmail(String to, String userName, BookingEmailData booking);
}
