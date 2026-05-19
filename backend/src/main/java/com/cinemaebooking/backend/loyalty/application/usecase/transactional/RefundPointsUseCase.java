package com.cinemaebooking.backend.loyalty.application.usecase.transactional;

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
public class RefundPointsUseCase {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    @Transactional
    public void execute(Long userId, Long bookingId) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElse(null);
        if (account == null) return;

        LoyaltyTransaction earnedTransaction = transactionRepository.findByBookingId(bookingId)
                .orElse(null);

        // Chỉ xử lý nếu tìm thấy transaction EARN_FROM_BOOKING (chưa từng refund)
        if (earnedTransaction == null
                || earnedTransaction.getType() != LoyaltyTransactionType.EARN_FROM_BOOKING) {
            return;
        }

        BigDecimal pointsToRefund = earnedTransaction.getChangePoint();
        if (pointsToRefund == null || pointsToRefund.compareTo(BigDecimal.ZERO) <= 0) return;

        account.setCurrentPoints(account.getCurrentPoints().subtract(pointsToRefund));

        LoyaltyAccount saved = loyaltyAccountRepository.save(account);

        LoyaltyTransaction refundTransaction = LoyaltyTransaction.builder()
                .loyaltyAccountId(saved.getId().getValue())
                .type(LoyaltyTransactionType.REFUND_POINT)
                .changePoint(pointsToRefund)
                .balanceAfter(saved.getCurrentPoints())
                .changeDate(LocalDateTime.now())
                .bookingId(bookingId)
                .build();
        transactionRepository.save(refundTransaction);
    }
}
