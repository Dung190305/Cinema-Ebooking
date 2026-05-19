package com.cinemaebooking.backend.booking.infrastructure.adapter;

import com.cinemaebooking.backend.booking.application.port.BookingLoyaltyPort;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.loyalty.application.port.MembershipTierRepository;
import com.cinemaebooking.backend.loyalty.domain.valueobject.MembershipTierId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BookingLoyaltyAdapter implements BookingLoyaltyPort {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final MembershipTierRepository membershipTierRepository;

    @Override
    public MembershipTierInfo getMembershipTierInfo(Long userId) {
        if (userId == null) return new MembershipTierInfo(null, "Basic", BigDecimal.ZERO);

        return loyaltyAccountRepository.findByUserId(userId)
                .map(account -> {
                    var tier = membershipTierRepository.findById(MembershipTierId.of(account.getTierId()));
                    if (tier.isEmpty()) return new MembershipTierInfo(null, "Basic", BigDecimal.ZERO);
                    return new MembershipTierInfo(
                            tier.get().getId().getValue(),
                            tier.get().getName(),
                            tier.get().getDiscountPercent() != null ? tier.get().getDiscountPercent() : BigDecimal.ZERO
                    );
                })
                .orElse(new MembershipTierInfo(null, "Basic", BigDecimal.ZERO));
    }
}
