package com.cinemaebooking.backend.loyalty.application.dto.loyalty_account;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class LoyaltyAccountSummaryResponse {
    private Long loyaltyAccountId;
    private BigDecimal currentPoints;
    private String tierName;
}
