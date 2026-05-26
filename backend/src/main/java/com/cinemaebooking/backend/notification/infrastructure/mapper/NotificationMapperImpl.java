package com.cinemaebooking.backend.notification.infrastructure.mapper;

import com.cinemaebooking.backend.notification.domain.model.Notification;
import com.cinemaebooking.backend.notification.domain.valueObject.NotificationId;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) return null;

        return Notification.builder()
                .id(NotificationId.ofNullable(entity.getId()))
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .status(entity.getStatus())
                .bookingId(entity.getBooking() != null ? entity.getBooking().getId() : null)
                .paymentId(entity.getPayment() != null ? entity.getPayment().getId() : null)
                .build();
    }

    @Override
    public NotificationJpaEntity toEntity(Notification domain) {
        if (domain == null) return null;

        return NotificationJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .title(domain.getTitle())
                .message(domain.getMessage())
                .type(domain.getType())
                .status(domain.getStatus())
                .build();
    }
}
