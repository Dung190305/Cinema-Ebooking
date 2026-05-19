package com.cinemaebooking.backend.loyalty.application.dto.loyalty_account;

import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyAccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LoyaltyAccountResponse {
    private Long loyaltyAccountId;
    private String loyaltyNumber;
    private BigDecimal totalSpending;
    private BigDecimal lifetimePoints;
    private BigDecimal currentPoints;
    private Long tierId;
    private LocalDateTime lastActivityDate;
    private LocalDateTime joinedDate;
    private LoyaltyAccountStatus status;
}