package com.cinemaebooking.backend.loyalty.presentation;

import com.cinemaebooking.backend.loyalty.application.dto.loyalty_account.LoyaltyAccountResponse;
import com.cinemaebooking.backend.loyalty.application.dto.loyalty_account.LoyaltyAccountSummaryResponse;
import com.cinemaebooking.backend.loyalty.application.usecase.loyalty_account.GetLoyaltyAccountByUserUseCase;
import com.cinemaebooking.backend.loyalty.application.usecase.loyalty_account.GetMyLoyaltyAccountUseCase;
import com.cinemaebooking.backend.loyalty.application.usecase.loyalty_account.GetMyLoyaltySummaryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
public class LoyaltyAccountController {
    private final GetMyLoyaltyAccountUseCase getMyAccount;
    private final GetMyLoyaltySummaryUseCase getMyLoyaltySummary;
    private final GetLoyaltyAccountByUserUseCase getAccountByUser;

    @GetMapping("/my-account")
    public LoyaltyAccountResponse getMyAccount() {
        return getMyAccount.execute();
    }

    @GetMapping("/my-account/summary")
    public LoyaltyAccountSummaryResponse getMySummary() {
        return getMyLoyaltySummary.execute();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/accounts")
    public LoyaltyAccountResponse getByUserId(@RequestParam Long userId) {
        return getAccountByUser.execute(userId);
    }
}