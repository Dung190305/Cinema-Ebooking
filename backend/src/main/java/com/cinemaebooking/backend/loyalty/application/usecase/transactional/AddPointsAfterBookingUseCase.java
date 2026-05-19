package com.cinemaebooking.backend.loyalty.application.usecase.transactional;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.exception_loyalty.LoyaltyExceptions;
import com.cinemaebooking.backend.common.exception.domain.exception_loyalty.MembershipTierExceptions;
import com.cinemaebooking.backend.loyalty.application.port.EarningRuleRepository;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyTransactionRepository;
import com.cinemaebooking.backend.loyalty.application.port.MembershipTierRepository;
import com.cinemaebooking.backend.loyalty.domain.enums.EarningType;
import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyTransactionType;
import com.cinemaebooking.backend.loyalty.domain.model.EarningRule;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyAccount;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyTransaction;
import com.cinemaebooking.backend.loyalty.domain.model.MembershipTier;
import com.cinemaebooking.backend.loyalty.domain.valueobject.MembershipTierId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class AddPointsAfterBookingUseCase {
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final EarningRuleRepository earningRuleRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Transactional
    public void execute(Long userId, BigDecimal totalTicketPrice, BigDecimal totalComboPrice) {
        log.info("=== AddPointsAfterBooking === userId={}, totalTicketPrice={}, totalComboPrice={}", userId, totalTicketPrice, totalComboPrice);

        if (userId == null) {
            throw CommonExceptions.invalidInput("userId must not be null");
        }

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> LoyaltyExceptions.notFoundByUserId(userId));

        MembershipTier currentTier = membershipTierRepository.findById(MembershipTierId.of(account.getTierId())).orElseThrow();

        log.info("Account found: id={}, currentPoints={}, tier={}", account.getId(), account.getCurrentPoints(), currentTier.getName());


        BigDecimal pointsEarned = BigDecimal.ZERO;

        // Ticket points
        if (totalTicketPrice != null && totalTicketPrice.compareTo(BigDecimal.ZERO) > 0) {
            EarningRule ticketRule = findRuleForType(currentTier, EarningType.TICKET);
            BigDecimal ratePercent = ticketRule != null ? ticketRule.getMultiplier() : BigDecimal.ZERO;
            log.info("Ticket rule: ratePercent={}%", ratePercent);
            BigDecimal ticketPoints = totalTicketPrice.multiply(ratePercent)
                    .divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP);
            pointsEarned = pointsEarned.add(ticketPoints);
            log.info("Ticket points earned: {}", ticketPoints);
        }

        // Combo points
        if (totalComboPrice != null && totalComboPrice.compareTo(BigDecimal.ZERO) > 0) {
            EarningRule comboRule = findRuleForType(currentTier, EarningType.CONCESSION);
            BigDecimal ratePercent = comboRule != null ? comboRule.getMultiplier() : BigDecimal.ZERO;
            log.info("Combo rule: ratePercent={}%", ratePercent);
            BigDecimal comboPoints = totalComboPrice.multiply(ratePercent)
                    .divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP);
            pointsEarned = pointsEarned.add(comboPoints);
            log.info("Combo points earned: {}", comboPoints);
        }

        log.info("Total pointsEarned={}", pointsEarned);

        // Cộng điểm
        account.setCurrentPoints(account.getCurrentPoints().add(pointsEarned));
        account.setLifetimePoints(account.getLifetimePoints().add(pointsEarned));

        // Cộng tổng chi tiêu (totalSpending)
        BigDecimal totalSpendingToAdd = (totalTicketPrice != null ? totalTicketPrice : BigDecimal.ZERO)
                .add(totalComboPrice != null ? totalComboPrice : BigDecimal.ZERO);
        account.addSpending(totalSpendingToAdd);

        // Re-evaluate tier
        MembershipTier newTier = evaluateTier(account.getTotalSpending());
        if (!newTier.getId().getValue().equals(account.getTierId())) {
            account.updateTier(newTier.getId());
        }

        LoyaltyAccount saved = loyaltyAccountRepository.save(account);

        if (pointsEarned.compareTo(BigDecimal.ZERO) > 0) {
            LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                    .loyaltyAccountId(saved.getId().getValue())
                    .type(LoyaltyTransactionType.EARN_FROM_BOOKING)
                    .changePoint(pointsEarned)
                    .balanceAfter(saved.getCurrentPoints())
                    .changeDate(LocalDateTime.now())
                    .build();
            transactionRepository.save(transaction);
        }
    }

    private EarningRule findRuleForType(MembershipTier tier, EarningType type) {
        List<EarningRule> rules = earningRuleRepository.findByTierAndType(
                MembershipTierId.of(tier.getId().getValue()), type);
        log.info("findRuleForType: tier={} (id={}), type={}, rules found={}", tier.getName(), tier.getId(), type, rules.size());
        rules.forEach(r -> log.info("  rule: id={}, multiplier={}, active={}", r.getId(), r.getMultiplier(), r.getActive()));
        return rules.stream().filter(EarningRule::getActive).findFirst().orElse(null);
    }

    private MembershipTier evaluateTier(BigDecimal totalSpending) {
        // Lấy tier cao nhất có minSpending <= totalSpending
        return membershipTierRepository.findAllByMinSpendingRequiredLessThanEqualOrderByTierLevelDesc(totalSpending)
                .stream().findFirst()
                .orElseThrow(() -> CommonExceptions.resourceNotFound(
                        "No membership tier found for spending: " + totalSpending));
    }
}