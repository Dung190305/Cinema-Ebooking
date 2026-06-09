package com.cinemaebooking.backend.refund.application.port;

import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import com.cinemaebooking.backend.refund.domain.valueobject.RefundId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RefundRepository {

    Refund create(Refund refund);

    Refund update(Refund refund);

    Optional<Refund> findById(RefundId id);

    Optional<Refund> findByBookingId(Long bookingId);

    Page<Refund> findAll(Pageable pageable);

    Page<Refund> findAll(RefundStatus status, Pageable pageable);

    boolean existsByBookingId(Long bookingId);
}
