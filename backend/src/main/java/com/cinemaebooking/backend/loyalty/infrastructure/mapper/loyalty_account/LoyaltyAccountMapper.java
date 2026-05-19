package com.cinemaebooking.backend.loyalty.infrastructure.mapper.loyalty_account;

import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyAccount;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyAccountJpaEntity;

public interface LoyaltyAccountMapper {
    LoyaltyAccountJpaEntity toEntity(LoyaltyAccount domain);
    LoyaltyAccount toDomain(LoyaltyAccountJpaEntity entity);
    void updateEntity(LoyaltyAccountJpaEntity entity, LoyaltyAccount domain);
}