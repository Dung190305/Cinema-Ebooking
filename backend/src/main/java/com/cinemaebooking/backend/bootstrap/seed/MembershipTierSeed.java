package com.cinemaebooking.backend.bootstrap.seed;

import com.cinemaebooking.backend.loyalty.application.port.EarningRuleRepository;
import com.cinemaebooking.backend.loyalty.application.port.MembershipTierRepository;
import com.cinemaebooking.backend.loyalty.domain.enums.EarningType;
import com.cinemaebooking.backend.loyalty.domain.model.EarningRule;
import com.cinemaebooking.backend.loyalty.domain.model.MembershipTier;
import com.cinemaebooking.backend.loyalty.domain.valueobject.EarningRuleId;
import com.cinemaebooking.backend.loyalty.domain.valueobject.MembershipTierId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipTierSeed {

    private final MembershipTierRepository membershipTierRepository;
    private final EarningRuleRepository loyaltyEarningRuleRepository;

    public void seed() {

        MembershipTier basic = createTierIfNotExists(
                "BASIC",
                BigDecimal.ZERO,
                1,
                "Tích điểm cho mọi giao dịch vé phim và combo"
        );

        MembershipTier silver = createTierIfNotExists(
                "SILVER",
                BigDecimal.valueOf(1_500_000),
                2,
                "Tích điểm với tỷ lệ cao hơn, gia tăng tốc độ đổi thưởng"
        );

        MembershipTier gold = createTierIfNotExists(
                "GOLD",
                BigDecimal.valueOf(3_000_000),
                3,
                "Tận hưởng mức tích điểm cao nhất cùng nhiều giá trị đổi thưởng hấp dẫn"
        );

        // Ticket earning rules
        createRuleIfNotExists(basic, EarningType.TICKET, BigDecimal.valueOf(0.003));
        createRuleIfNotExists(silver, EarningType.TICKET, BigDecimal.valueOf(0.005));
        createRuleIfNotExists(gold, EarningType.TICKET, BigDecimal.valueOf(0.007));

        // Concession earning rules
        createRuleIfNotExists(basic, EarningType.CONCESSION, BigDecimal.valueOf(0.003));
        createRuleIfNotExists(silver, EarningType.CONCESSION, BigDecimal.valueOf(0.004));
        createRuleIfNotExists(gold, EarningType.CONCESSION, BigDecimal.valueOf(0.005));
    }

    private MembershipTier createTierIfNotExists(
            String name,
            BigDecimal minSpending,
            Integer tierLevel,
            String benefitsDescription
    ) {

        if (membershipTierRepository.existsByName(name)) {

            log.info("Membership tier already exists: {}", name);

            return membershipTierRepository.findByName(name)
                    .orElseThrow(() ->
                            new IllegalStateException("Membership tier exists but not found"));
        }

        MembershipTier tier = MembershipTier.builder()
                .name(name)
                .minSpendingRequired(minSpending)
                .discountPercent(BigDecimal.ZERO)
                .tierLevel(tierLevel)
                .benefitsDescription(benefitsDescription)
                .build();

        MembershipTier saved = membershipTierRepository.create(tier);

        log.info("Membership tier seeded: {}", name);

        return saved;
    }

    private void createRuleIfNotExists(
            MembershipTier tier,
            EarningType earningType,
            BigDecimal multiplier
    ) {

        boolean exists = loyaltyEarningRuleRepository
            .existsByMembershipTierIdAndEarningType(
                    tier.getId().getValue(),
                    earningType
            );

        if (exists) {

            log.info(
                    "Loyalty earning rule already exists: {} - {}",
                    tier.getName(),
                    earningType
            );

            return;
        }

        EarningRule rule = EarningRule.builder()
                .tier(tier)
                .earningType(earningType)
                .multiplier(multiplier)
                .fixedPoints(null)
                .description(
                        String.format(
                                "%s earns %.0f%% points for %s",
                                tier.getName(),
                                multiplier.multiply(BigDecimal.valueOf(100)),
                                earningType
                        )
                )
                .active(true)
                .priority(tier.getTierLevel())
                .build();

        loyaltyEarningRuleRepository.create(rule);

        log.info(
                "Loyalty earning rule seeded: {} - {}",
                tier.getName(),
                earningType
        );
    }
}