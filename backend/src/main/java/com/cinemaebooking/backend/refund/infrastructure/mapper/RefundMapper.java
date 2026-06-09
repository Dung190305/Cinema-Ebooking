package com.cinemaebooking.backend.refund.infrastructure.mapper;

import com.cinemaebooking.backend.refund.domain.model.Refund;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;

public interface RefundMapper {

    Refund toDomain(RefundJpaEntity entity);

    /**
     * Like toDomain but also populates transient booking detail fields
     * from the already-loaded booking relationship.
     */
    Refund toDomainWithBookingDetails(RefundJpaEntity entity);

    RefundJpaEntity toEntity(Refund domain);

    void updateEntity(RefundJpaEntity entity, Refund domain);
}
