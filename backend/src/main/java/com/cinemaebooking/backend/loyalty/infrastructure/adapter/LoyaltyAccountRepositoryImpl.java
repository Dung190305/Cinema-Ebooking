package com.cinemaebooking.backend.loyalty.infrastructure.adapter;

import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyAccount;
import com.cinemaebooking.backend.loyalty.domain.valueobject.LoyaltyAccountId;
import com.cinemaebooking.backend.loyalty.infrastructure.mapper.loyalty_account.LoyaltyAccountMapper;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyAccountJpaEntity;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.MembershipTierJpaEntity;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.repository.LoyaltyAccountJpaRepository;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.repository.MembershipTierJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoyaltyAccountRepositoryImpl implements LoyaltyAccountRepository {
    private final LoyaltyAccountJpaRepository jpaRepository;
    private final MembershipTierJpaRepository tierJpaRepository;
    private final LoyaltyAccountMapper mapper;

    @Override
    public Optional<LoyaltyAccount> findById(LoyaltyAccountId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<LoyaltyAccount> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public LoyaltyAccount save(LoyaltyAccount account) {
        LoyaltyAccountJpaEntity entity;

        if (account.getId() == null) {
            entity = mapper.toEntity(account);
        } else {
            entity = jpaRepository.findById(account.getId().getValue())
                    .orElseThrow(() -> new RuntimeException(
                            "LoyaltyAccount not found: " + account.getId().getValue()));
            mapper.updateEntity(entity, account);
        }

        if (account.getTierId() != null) {
            MembershipTierJpaEntity tierRef = tierJpaRepository.getReferenceById(account.getTierId());
            entity.setMembershipTier(tierRef);
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepository.findByUserId(userId).ifPresent(jpaRepository::delete);
    }
}