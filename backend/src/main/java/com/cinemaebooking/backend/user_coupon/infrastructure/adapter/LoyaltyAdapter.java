package com.cinemaebooking.backend.user_coupon.infrastructure.adapter;

import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.user_coupon.application.port.LoyaltyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LoyaltyAdapter implements LoyaltyPort {

    private final LoyaltyAccountRepository loyaltyAccountRepository;

    @Override
    public int getUserPoints(Long userId) {
        if (userId == null) return 0;
        return loyaltyAccountRepository.findByUserId(userId)
                .map(account -> {
                    BigDecimal points = account.getCurrentPoints();
                    return points != null ? points.intValue() : 0;
                })
                .orElse(0);
    }
}
