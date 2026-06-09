package com.cinemaebooking.backend.refund.application.usecase;

import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.application.mapper.RefundResponseMapper;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRefundListUseCase {

    private final RefundRepository refundRepository;
    private final RefundResponseMapper mapper;

    @Transactional(readOnly = true)
    public Page<RefundResponse> execute(RefundStatus status, Pageable pageable) {
        Page<RefundResponse> page = refundRepository.findAll(status, pageable)
                .map(mapper::toResponse);
        return page;
    }
}
