package com.cinemaebooking.backend.refund.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.RefundExceptions;
import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.application.mapper.RefundResponseMapper;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import com.cinemaebooking.backend.refund.domain.valueobject.RefundId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelRefundUseCase {

    private final RefundRepository refundRepository;
    private final RefundResponseMapper mapper;

    @Transactional
    public RefundResponse execute(Long id) {
        RefundId refundId = RefundId.of(id);
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> RefundExceptions.notFound(refundId));

        refund.cancel();

        return mapper.toResponse(refundRepository.update(refund));
    }
}
