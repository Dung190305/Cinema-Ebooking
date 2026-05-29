package com.cinemaebooking.backend.notification.application.port;

public interface QRCodeService {

    byte[] generateQRCode(String bookingCode, int width, int height);

    String generateQRCodeBase64(String bookingCode, int width, int height);
}
