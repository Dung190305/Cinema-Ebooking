package com.cinemaebooking.backend.loyalty.application.usecase.transactional;

import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyTransactionRepository;
import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyTransactionType;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyAccount;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyTransaction;
import com.cinemaebooking.backend.loyalty.domain.valueobject.LoyaltyAccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EarnPointsUseCase {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    @Transactional
    public void execute(Long userId, BigDecimal pointsEarned, LoyaltyTransactionType type,
                        Long bookingId, Long userCouponId) {
        if (pointsEarned == null || pointsEarned.compareTo(BigDecimal.ZERO) <= 0) return;

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElse(null);
        if (account == null) return;

        account.setCurrentPoints(account.getCurrentPoints().add(pointsEarned));
        account.setLifetimePoints(account.getLifetimePoints().add(pointsEarned));

        LoyaltyAccount saved = loyaltyAccountRepository.save(account);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .loyaltyAccountId(saved.getId().getValue())
                .type(type)
                .changePoint(pointsEarned)
                .balanceAfter(saved.getCurrentPoints())
                .changeDate(LocalDateTime.now())
                .bookingId(bookingId)
                .userCouponId(userCouponId)
                .build();
        transactionRepository.save(transaction);
    }
}
