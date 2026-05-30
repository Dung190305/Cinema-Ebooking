package com.cinemaebooking.backend.refund.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundRequest {

    private Long bookingId;

    private String reason;
}
