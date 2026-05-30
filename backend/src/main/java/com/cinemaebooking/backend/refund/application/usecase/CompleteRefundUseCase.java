package com.cinemaebooking.backend.refund.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking_coupon.application.usecase.ReleaseBookingCouponUseCase;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.RefundExceptions;
import com.cinemaebooking.backend.loyalty.application.usecase.transactional.RefundPointsUseCase;
import com.cinemaebooking.backend.refund.application.dto.ProcessRefundRequest;
import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.application.mapper.RefundResponseMapper;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import com.cinemaebooking.backend.refund.application.validator.RefundCommandValidator;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import com.cinemaebooking.backend.refund.domain.valueobject.RefundId;
import com.cinemaebooking.backend.ticket.application.usecase.ReleaseSeatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteRefundUseCase {

    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final RefundResponseMapper mapper;
    private final RefundCommandValidator validator;
    private final RefundPointsUseCase refundPointsUseCase;
    private final ReleaseSeatsUseCase releaseSeatsUseCase;
    private final ReleaseBookingCouponUseCase releaseBookingCouponUseCase;

    @Transactional
    public RefundResponse execute(Long id, ProcessRefundRequest request) {
        validator.validateProcessRequest(request);

        RefundId refundId = RefundId.of(id);
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> RefundExceptions.notFound(refundId));

        Booking booking = bookingRepository.findById(refund.getBookingId())
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(refund.getBookingId())));

        refund.complete(request != null ? request.getAdminNote() : null);

        refundPointsUseCase.execute(booking.getUserId(), booking.getId().getValue());
        releaseSeatsUseCase.execute(booking.getShowtimeId(), booking.getTickets());
        releaseBookingCouponUseCase.execute(booking.getId().getValue());

        booking.cancelAfterRefund();
        bookingRepository.save(booking);

        return mapper.toResponse(refundRepository.update(refund));
    }
}
