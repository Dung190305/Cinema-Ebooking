package com.cinemaebooking.backend.loyalty.application.usecase.transactional;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.exception_loyalty.LoyaltyExceptions;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyTransactionRepository;
import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyTransactionType;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyAccount;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeductPointsUseCase {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    @Transactional
    public void execute(Long userId, BigDecimal pointsToDeduct, LoyaltyTransactionType type, Long userCouponId) {
        if (pointsToDeduct == null || pointsToDeduct.compareTo(BigDecimal.ZERO) <= 0) return;

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> LoyaltyExceptions.notFoundByUserId(userId));

        if (!account.hasEnoughPoints(pointsToDeduct)) {
            throw CommonExceptions.invalidInput("Insufficient loyalty points. Required: " + pointsToDeduct
                    + ", Available: " + account.getCurrentPoints());
        }

        account.deductPoints(pointsToDeduct);
        LoyaltyAccount saved = loyaltyAccountRepository.save(account);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .loyaltyAccountId(saved.getId().getValue())
                .type(type)
                .changePoint(pointsToDeduct.negate())
                .balanceAfter(saved.getCurrentPoints())
                .changeDate(LocalDateTime.now())
                .userCouponId(userCouponId)
                .build();
        transactionRepository.save(transaction);
    }
}
