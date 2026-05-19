package com.cinemaebooking.backend.loyalty.infrastructure.mapper.loyalty_transaction;

import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyTransaction;
import com.cinemaebooking.backend.loyalty.domain.valueobject.LoyaltyTransactionId;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyAccountJpaEntity;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyTransactionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyTransactionMapperImpl implements LoyaltyTransactionMapper {

    @Override
    public LoyaltyTransactionJpaEntity toEntity(LoyaltyTransaction domain) {
        if (domain == null) return null;
        return LoyaltyTransactionJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .loyaltyAccount(LoyaltyAccountJpaEntity.builder().id(domain.getLoyaltyAccountId()).build())
                .type(domain.getType())
                .changePoint(domain.getChangePoint())
                .balanceAfter(domain.getBalanceAfter())
                .changeDate(domain.getChangeDate())
                .build();
    }

    @Override
    public LoyaltyTransaction toDomain(LoyaltyTransactionJpaEntity entity) {
        if (entity == null) return null;
        return LoyaltyTransaction.builder()
                .id(LoyaltyTransactionId.ofNullable(entity.getId()))
                .loyaltyAccountId(entity.getLoyaltyAccount().getId())
                .type(entity.getType())
                .changePoint(entity.getChangePoint())
                .balanceAfter(entity.getBalanceAfter())
                .changeDate(entity.getChangeDate())
                .bookingId(entity.getBooking() != null ? entity.getBooking().getId() : null)
                .paymentId(entity.getPayment() != null ? entity.getPayment().getId() : null)
                .couponId(entity.getCoupon() != null ? entity.getCoupon().getId() : null)
                .userCouponId(entity.getUserCoupon() != null ? entity.getUserCoupon().getId() : null)
                .build();
    }
}
