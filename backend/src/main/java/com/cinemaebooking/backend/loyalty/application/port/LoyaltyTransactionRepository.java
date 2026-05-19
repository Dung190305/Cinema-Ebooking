package com.cinemaebooking.backend.loyalty.application.port;

import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyTransaction;
import com.cinemaebooking.backend.loyalty.domain.valueobject.LoyaltyTransactionId;

import java.util.List;
import java.util.Optional;

public interface LoyaltyTransactionRepository {
    LoyaltyTransaction save(LoyaltyTransaction transaction);
    List<LoyaltyTransaction> findByLoyaltyAccountId(Long loyaltyAccountId);
    Optional<LoyaltyTransaction> findByBookingId(Long bookingId);
}
