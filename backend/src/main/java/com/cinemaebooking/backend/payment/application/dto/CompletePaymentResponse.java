package com.cinemaebooking.backend.payment.application.dto;


import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletePaymentResponse {
    private PaymentStatus status;
    private Long bookingId;
    private Long showtimeId;
    private String message;
    private String transactionId;
}
