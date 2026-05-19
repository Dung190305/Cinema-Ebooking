package com.cinemaebooking.backend.loyalty.infrastructure.mapper.loyalty_transaction;

import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyTransaction;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyTransactionJpaEntity;

public interface LoyaltyTransactionMapper {
    LoyaltyTransactionJpaEntity toEntity(LoyaltyTransaction domain);
    LoyaltyTransaction toDomain(LoyaltyTransactionJpaEntity entity);
}
