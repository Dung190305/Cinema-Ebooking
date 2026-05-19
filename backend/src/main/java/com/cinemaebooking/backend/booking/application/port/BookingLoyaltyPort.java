package com.cinemaebooking.backend.booking.application.port;

import java.math.BigDecimal;

public interface BookingLoyaltyPort {
    MembershipTierInfo getMembershipTierInfo(Long userId);

    record MembershipTierInfo(Long tierId, String tierName, BigDecimal discountPercent) {}
}
