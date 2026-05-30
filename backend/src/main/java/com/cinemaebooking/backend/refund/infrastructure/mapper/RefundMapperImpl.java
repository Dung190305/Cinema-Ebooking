package com.cinemaebooking.backend.refund.infrastructure.mapper;

import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import com.cinemaebooking.backend.refund.domain.valueobject.RefundId;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundMapperImpl implements RefundMapper {

    private final BookingJpaRepository bookingJpaRepository;

    @Override
    public Refund toDomain(RefundJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Refund.builder()
                .id(RefundId.ofNullable(entity.getId()))
                .bookingId(entity.getBooking() != null ? entity.getBooking().getId() : null)
                .originalAmount(entity.getOriginalAmount())
                .refundAmount(entity.getRefundAmount())
                .refundPercentage(entity.getRefundPercentage())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .processedAt(entity.getProcessedAt())
                .reason(entity.getReason())
                .adminNote(entity.getAdminNote())
                .build();
    }

    @Override
    public RefundJpaEntity toEntity(Refund domain) {
        if (domain == null) {
            return null;
        }

        return RefundJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .booking(bookingJpaRepository.getReferenceById(domain.getBookingId()))
                .originalAmount(domain.getOriginalAmount())
                .refundAmount(domain.getRefundAmount())
                .refundPercentage(domain.getRefundPercentage())
                .status(domain.getStatus())
                .requestedAt(domain.getRequestedAt())
                .processedAt(domain.getProcessedAt())
                .reason(domain.getReason())
                .adminNote(domain.getAdminNote())
                .build();
    }

    @Override
    public void updateEntity(RefundJpaEntity entity, Refund domain) {
        entity.setOriginalAmount(domain.getOriginalAmount());
        entity.setRefundAmount(domain.getRefundAmount());
        entity.setRefundPercentage(domain.getRefundPercentage());
        entity.setStatus(domain.getStatus());
        entity.setRequestedAt(domain.getRequestedAt());
        entity.setProcessedAt(domain.getProcessedAt());
        entity.setReason(domain.getReason());
        entity.setAdminNote(domain.getAdminNote());
    }
}
