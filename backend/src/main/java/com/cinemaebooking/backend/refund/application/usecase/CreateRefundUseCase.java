package com.cinemaebooking.backend.refund.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.RefundExceptions;
import com.cinemaebooking.backend.refund.application.dto.CreateRefundRequest;
import com.cinemaebooking.backend.refund.application.dto.RefundCalculationResponse;
import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.application.mapper.RefundResponseMapper;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import com.cinemaebooking.backend.refund.application.validator.RefundCommandValidator;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateRefundUseCase {

    private final RefundRepository refundRepository;
    private final RefundResponseMapper mapper;
    private final RefundCommandValidator validator;
    private final CalculateRefundAmountUseCase calculateRefundAmountUseCase;

    @Transactional
    public RefundResponse execute(CreateRefundRequest request) {
        validator.validateCreateRequest(request);

        RefundCalculationResponse calculation = calculateRefundAmountUseCase.execute(request.getBookingId());

        if (calculation.getRefundPercentage() == null || calculation.getRefundPercentage() <= 0) {
            throw RefundExceptions.notEligible(calculation.getMessage());
        }

        Refund refund = Refund.builder()
                .bookingId(request.getBookingId())
                .originalAmount(calculation.getOriginalAmount())
                .refundAmount(calculation.getRefundAmount())
                .refundPercentage(calculation.getRefundPercentage())
                .status(RefundStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .reason(request.getReason())
                .build();

        return mapper.toResponse(refundRepository.create(refund));
    }
}
