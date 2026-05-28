package com.cinemaebooking.backend.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class CreatePaymentResponse {
    private String paymentCode;
    private String paymentUrl;
    private LocalDateTime expiredAt;
}