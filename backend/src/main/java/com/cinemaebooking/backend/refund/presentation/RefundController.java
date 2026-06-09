package com.cinemaebooking.backend.refund.presentation;

import com.cinemaebooking.backend.common.security.CustomUserPrincipal;
import com.cinemaebooking.backend.refund.application.dto.CreateRefundRequest;
import com.cinemaebooking.backend.refund.application.dto.ProcessRefundRequest;
import com.cinemaebooking.backend.refund.application.dto.RefundCalculationResponse;
import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.application.usecase.*;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RefundController {

    private final CreateRefundUseCase createRefundUseCase;
    private final CalculateRefundAmountUseCase calculateRefundAmountUseCase;
    private final CancelRefundUseCase cancelRefundUseCase;
    private final ApproveRefundUseCase approveRefundUseCase;
    private final RejectRefundUseCase rejectRefundUseCase;
    private final CompleteRefundUseCase completeRefundUseCase;
    private final GetRefundDetailUseCase getRefundDetailUseCase;
    private final GetRefundListUseCase getRefundListUseCase;
    private final GetMyRefundsUseCase getMyRefundsUseCase;

    @PostMapping("/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse createRefund(@RequestBody CreateRefundRequest request) {
        return createRefundUseCase.execute(request);
    }

    @GetMapping("/refunds/calculate")
    public RefundCalculationResponse calculateRefundAmount(@RequestParam Long bookingId) {
        return calculateRefundAmountUseCase.execute(bookingId);
    }

    @PostMapping("/refunds/{id}/cancel")
    public RefundResponse cancelRefund(@PathVariable Long id) {
        return cancelRefundUseCase.execute(id);
    }

    @GetMapping("/admin/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<RefundResponse> getRefundList(
            @RequestParam(required = false) RefundStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return getRefundListUseCase.execute(status, pageable);
    }

    @GetMapping("/admin/refunds/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RefundResponse getRefundDetail(@PathVariable Long id) {
        return getRefundDetailUseCase.execute(id);
    }

    @PostMapping("/admin/refunds/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public RefundResponse approveRefund(@PathVariable Long id,
                                        @RequestBody(required = false) ProcessRefundRequest request) {
        return approveRefundUseCase.execute(id, request);
    }

    @PostMapping("/admin/refunds/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public RefundResponse rejectRefund(@PathVariable Long id,
                                       @RequestBody(required = false) ProcessRefundRequest request) {
        return rejectRefundUseCase.execute(id, request);
    }

    @PostMapping("/admin/refunds/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public RefundResponse completeRefund(@PathVariable Long id,
                                         @RequestBody(required = false) ProcessRefundRequest request) {
        return completeRefundUseCase.execute(id, request);
    }

    @GetMapping("/refunds/me")
    public List<RefundResponse> getMyRefunds(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return getMyRefundsUseCase.execute(principal.getUserId());
    }
}
