package com.cinemaebooking.backend.loyalty.infrastructure.mapper.loyalty_account;

import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyAccount;
import com.cinemaebooking.backend.loyalty.domain.valueobject.LoyaltyAccountId;
import com.cinemaebooking.backend.loyalty.infrastructure.mapper.membership_tier.MembershipTierMapperImpl;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyAccountJpaEntity;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.MembershipTierJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyAccountMapperImpl implements LoyaltyAccountMapper {

    @Override
    public LoyaltyAccountJpaEntity toEntity(LoyaltyAccount domain) {
        if (domain == null) return null;
        return LoyaltyAccountJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .userId(domain.getUserId())
                .loyaltyNumber(domain.getLoyaltyNumber())
                .totalSpending(domain.getTotalSpending())
                .lifetimePoints(domain.getLifetimePoints())
                .currentPoints(domain.getCurrentPoints())
                .lastActivityDate(domain.getLastActivityDate())
                .joinedDate(domain.getJoinedDate())
                .status(domain.getStatus())
                .build();
    }

    @Override
    public LoyaltyAccount toDomain(LoyaltyAccountJpaEntity entity) {
        if (entity == null) return null;
        return LoyaltyAccount.builder()
                .id(LoyaltyAccountId.ofNullable(entity.getId()))
                .userId(entity.getUserId())
                .loyaltyNumber(entity.getLoyaltyNumber())
                .totalSpending(entity.getTotalSpending())
                .lifetimePoints(entity.getLifetimePoints())
                .currentPoints(entity.getCurrentPoints())
                .tierId(entity.getMembershipTier() != null ?
                        entity.getMembershipTier().getId() : null)
                .lastActivityDate(entity.getLastActivityDate())
                .joinedDate(entity.getJoinedDate())
                .status(entity.getStatus())
                .build();
    }

    @Override
    public void updateEntity(LoyaltyAccountJpaEntity target, LoyaltyAccount source) {
        target.setUserId(source.getUserId());
        target.setLoyaltyNumber(source.getLoyaltyNumber());
        target.setTotalSpending(source.getTotalSpending());
        target.setLifetimePoints(source.getLifetimePoints());
        target.setCurrentPoints(source.getCurrentPoints());
        target.setLastActivityDate(source.getLastActivityDate());
        target.setJoinedDate(source.getJoinedDate());
        target.setStatus(source.getStatus());
    }
}