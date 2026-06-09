package com.cinemaebooking.backend.refund.application.usecase;

import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.application.mapper.RefundResponseMapper;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMyRefundsUseCase {

    private final RefundRepository refundRepository;
    private final RefundResponseMapper mapper;

    @Transactional(readOnly = true)
    public List<RefundResponse> execute(Long userId) {
        return refundRepository.findAllByUserId(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}