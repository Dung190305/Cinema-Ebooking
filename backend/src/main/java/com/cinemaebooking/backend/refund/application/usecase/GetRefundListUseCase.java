package com.cinemaebooking.backend.refund.application.usecase;

import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.application.mapper.RefundResponseMapper;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
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
    public Page<RefundResponse> execute(Pageable pageable) {
        return refundRepository.findAll(pageable)
                .map(mapper::toResponse);
    }
}
