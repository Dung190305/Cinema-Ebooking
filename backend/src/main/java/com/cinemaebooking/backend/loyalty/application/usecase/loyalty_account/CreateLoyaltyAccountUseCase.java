package com.cinemaebooking.backend.loyalty.application.usecase.loyalty_account;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.loyalty.application.port.MembershipTierRepository;
import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyAccountStatus;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyAccount;
import com.cinemaebooking.backend.loyalty.domain.model.MembershipTier;
import com.cinemaebooking.backend.loyalty.domain.valueobject.MembershipTierId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateLoyaltyAccountUseCase {
    private final LoyaltyAccountRepository repository;
    private final MembershipTierRepository tierRepository;

    @Transactional
    public LoyaltyAccount execute(Long userId) {
        if (userId == null) throw CommonExceptions.invalidInput("User ID must not be null");
        if (repository.existsByUserId(userId))
            throw CommonExceptions.resourceAlreadyExists("Loyalty account already exists for user " + userId);

        // Dùng findAllOrderByTierLevelAsc để lấy danh sách tier đã sắp xếp
        MembershipTier basicTier = tierRepository.findAllOrderByTierLevelAsc().stream()
                .filter(t -> t.getMinSpendingRequired().compareTo(BigDecimal.ZERO) == 0)
                .findFirst()
                .orElseThrow(() -> CommonExceptions.resourceNotFound("No basic membership tier configured"));

        Long basicTierId = basicTier.getId().getValue();
        LoyaltyAccount account = LoyaltyAccount.builder()
                .userId(userId)
                .loyaltyNumber("LY" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .totalSpending(BigDecimal.ZERO)
                .lifetimePoints(BigDecimal.ZERO)
                .currentPoints(BigDecimal.ZERO)
                .tierId(basicTierId)
                .joinedDate(LocalDateTime.now())
                .status(LoyaltyAccountStatus.ACTIVE)
                .build();
        return repository.save(account);
    }
}