package com.cinemaebooking.backend.refund.application.mapper;

import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import org.springframework.stereotype.Component;

@Component
public class RefundResponseMapper {

    public RefundResponse toResponse(Refund refund) {
        if (refund == null) {
            return null;
        }

        return new RefundResponse(
                refund.getId() != null ? refund.getId().getValue() : null,
                refund.getBookingId(),
                refund.getOriginalAmount(),
                refund.getRefundAmount(),
                refund.getRefundPercentage(),
                refund.getStatus(),
                refund.getRequestedAt(),
                refund.getProcessedAt(),
                refund.getReason(),
                refund.getAdminNote()
        );
    }
}
