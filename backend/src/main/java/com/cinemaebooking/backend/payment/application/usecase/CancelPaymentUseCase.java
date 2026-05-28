package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking_coupon.application.usecase.ReleaseBookingCouponUseCase;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.seat_lock.application.port.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CancelPaymentUseCase — Hủy payment và booking PENDING.
 *
 * <p>Vì Ticket chỉ được tạo tại {@code ConfirmPaymentUseCase} (sau thanh toán thành công),
 * tại thời điểm cancel booking vẫn đang PENDING và chưa có Ticket nào.
 * Do đó không cần loop cancel/xóa từng Ticket — chỉ cần:
 * <ol>
 *   <li>Mark payment CANCELED</li>
 *   <li>Cancel booking (PENDING → CANCELLED)</li>
 *   <li>Giải phóng seat locks theo userId + showtimeId</li>
 *   <li>Giải phóng coupon nếu có</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class CancelPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final ReleaseBookingCouponUseCase releaseCouponUseCase;

    @Transactional
    public void execute(String paymentCode) {

        // 1. Hủy payment
        Payment payment = paymentRepository.findByPaymentCode(paymentCode);
        payment.markCanceled();

        // 2. Lấy booking
        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> BookingExceptions.notFound(
                        BookingId.of(payment.getBookingId())));

        // 3. Cancel booking — booking.PENDING → CANCELLED
        //    Không cần forEach(Ticket::cancel) vì chưa có Ticket ở giai đoạn này
        booking.cancel();

        // 4. Giải phóng seat locks (theo userId + showtimeId)
        seatLockService.releaseUserLocks(booking.getUserId(), booking.getShowtimeId());

        // 5. Giải phóng coupon nếu có
        releaseCouponUseCase.execute(booking.getId().getValue());

        // 6. Persist
        bookingRepository.save(booking);
        paymentRepository.update(payment);
    }
}