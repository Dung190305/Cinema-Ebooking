package com.cinemaebooking.backend.ticket.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketJpaRepository extends SoftDeleteJpaRepository<TicketJpaEntity> {
    Optional<TicketJpaEntity> findByTicketCodeAndDeletedAtIsNull(String ticketCode);
    List<TicketJpaEntity> findAllByBookingIdAndDeletedAtIsNull(Long bookingId);
    List<TicketJpaEntity> findAllByBookingBookingCodeAndDeletedAtIsNull(String bookingCode);
    List<TicketJpaEntity> findAllByIdInAndDeletedAtIsNull(List<Long> ids);
}
