package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.RefundExceptions;
import com.cinemaebooking.backend.refund.application.dto.RefundResponse;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import com.cinemaebooking.backend.refund.application.usecase.CalculateRefundAmountUseCase;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * RequestRefundAndCancelUseCase — Cho CONFIRMED booking: tạo RefundRequest + hủy booking.
 *
 * <p>Chỉ áp dụng cho booking đã thanh toán (CONFIRMED).
 * Booking PENDING → dùng {@link CancelBookingUseCase} thay thế.
 *
 * <p>Luồng:
 * <ol>
 *   <li>Validate booking tồn tại, status = CONFIRMED</li>
 *   <li>Kiểm tra chưa có refund request nào</li>
 *   <li>Tính số tiền hoàn theo {@link CalculateRefundAmountUseCase}</li>
 *   <li>Tạo Refund record với status = REQUESTED</li>
 *   <li>Booking → CANCELLED</li>
 *   <li>Persist</li>
 * </ol>
 *
 * <p>Ghế KHÔNG được giải phóng ngay — Admin sẽ xử lý khi approve/complete refund.
 */
@Service
@RequiredArgsConstructor
public class RequestRefundAndCancelUseCase {

    private final BookingRepository bookingRepository;
    private final RefundRepository refundRepository;
    private final CalculateRefundAmountUseCase calculateRefundAmountUseCase;

    @Transactional
    public RefundResponse execute(Long bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        // Chỉ CONFIRMED booking mới được request refund
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw BookingExceptions.invalidStatus(booking.getStatus());
        }

        // Tránh duplicate refund request
        if (refundRepository.existsByBookingId(bookingId)) {
            throw RefundExceptions.alreadyProcessed(null);
        }

        // Tính số tiền hoàn
        var calculation = calculateRefundAmountUseCase.execute(bookingId);

        // Tạo Refund record
        Refund refund = Refund.builder()
                .bookingId(bookingId)
                .originalAmount(calculation.getOriginalAmount())
                .refundAmount(calculation.getRefundAmount())
                .refundPercentage(calculation.getRefundPercentage())
                .status(RefundStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .reason(reason != null && !reason.isBlank() ? reason : "Người dùng yêu cầu hủy vé")
                .build();

        Refund savedRefund = refundRepository.create(refund);

        // Hủy booking — giữ tickets để admin xem khi duyệt refund
        booking.cancel();
        bookingRepository.save(booking);

        return toResponse(savedRefund);
    }

    private RefundResponse toResponse(Refund refund) {
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