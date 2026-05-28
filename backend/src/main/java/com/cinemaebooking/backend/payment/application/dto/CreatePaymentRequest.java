package com.cinemaebooking.backend.payment.application.dto;

import com.cinemaebooking.backend.payment.domain.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private Long bookingId;
    private PaymentMethod method;
    private String callbackUrl;
}
