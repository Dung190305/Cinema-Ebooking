package com.cinemaebooking.backend.loyalty.application.usecase.loyalty_account;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.exception_loyalty.LoyaltyExceptions;
import com.cinemaebooking.backend.common.security.CustomUserPrincipal;
import com.cinemaebooking.backend.loyalty.application.dto.loyalty_account.LoyaltyAccountSummaryResponse;
import com.cinemaebooking.backend.loyalty.application.mapper.LoyaltyAccountResponseMapper;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyAccountRepository;
import com.cinemaebooking.backend.loyalty.application.port.MembershipTierRepository;
import com.cinemaebooking.backend.loyalty.domain.model.MembershipTier;
import com.cinemaebooking.backend.loyalty.domain.valueobject.MembershipTierId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMyLoyaltySummaryUseCase {

    private final LoyaltyAccountRepository accountRepository;
    private final MembershipTierRepository tierRepository;
    private final LoyaltyAccountResponseMapper mapper;

    public LoyaltyAccountSummaryResponse execute() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserPrincipal principal))
            throw CommonExceptions.unauthorized();

        Long userId = principal.getUserId();

        var account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> LoyaltyExceptions.notFoundByUserId(userId));

        String tierName = tierRepository.findById(MembershipTierId.of(account.getTierId()))
                .map(MembershipTier::getName)
                .orElse("Unknown");

        return mapper.toSummaryResponse(account, tierName);
    }
}