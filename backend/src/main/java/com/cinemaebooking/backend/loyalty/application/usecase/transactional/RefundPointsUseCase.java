package com.cinemaebooking.backend.loyalty.application.usecase.transactional;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyTransactionRepository;
import com.cinemaebooking.backend.loyalty.application.port.MembershipTierRepository;
import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyTransactionType;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyAccount;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyTransaction;
import com.cinemaebooking.backend.loyalty.domain.model.MembershipTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundPointsUseCase {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final BookingRepository bookingRepository;

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

        // 1. Trừ điểm đã tích
        BigDecimal pointsToRefund = earnedTransaction.getChangePoint();
        if (pointsToRefund != null && pointsToRefund.compareTo(BigDecimal.ZERO) > 0) {
            account.setCurrentPoints(
                    account.getCurrentPoints().subtract(pointsToRefund).max(BigDecimal.ZERO)
            );
        }

        // 2. Hoàn lại totalSpending từ finalAmount của booking
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (booking.getFinalAmount() != null
                    && booking.getFinalAmount().compareTo(BigDecimal.ZERO) > 0) {
                account.subtractSpending(booking.getFinalAmount());
                log.info("[RefundPoints] Subtract spending {} for userId={}, bookingId={}",
                        booking.getFinalAmount(), userId, bookingId);
            }
        });

        // 3. Re-evaluate tier dựa trên totalSpending mới
        MembershipTier newTier = membershipTierRepository
                .findAllByMinSpendingRequiredLessThanEqualOrderByTierLevelDesc(account.getTotalSpending())
                .stream()
                .findFirst()
                .orElse(null);

        if (newTier != null && !newTier.getId().getValue().equals(account.getTierId())) {
            log.info("[RefundPoints] Downgrade tier for userId={}: tierId {} -> {}",
                    userId, account.getTierId(), newTier.getId().getValue());
            account.updateTier(newTier.getId());
        }

        LoyaltyAccount saved = loyaltyAccountRepository.save(account);

        // 4. Ghi transaction REFUND_POINT
        if (pointsToRefund != null && pointsToRefund.compareTo(BigDecimal.ZERO) > 0) {
            LoyaltyTransaction refundTransaction = LoyaltyTransaction.builder()
                    .loyaltyAccountId(saved.getId().getValue())
                    .type(LoyaltyTransactionType.REFUND_POINT)
                    .changePoint(pointsToRefund.negate())
                    .balanceAfter(saved.getCurrentPoints())
                    .changeDate(LocalDateTime.now())
                    .bookingId(bookingId)
                    .build();
            transactionRepository.save(refundTransaction);
        }
    }
}