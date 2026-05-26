package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.loyalty.application.usecase.transactional.AddPointsAfterBookingUseCase;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.seat_lock.application.port.SeatLockService;
import com.cinemaebooking.backend.showtime_seat.application.port.ShowtimeSeatRepository;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ConfirmPaymentUseCase - Single Source of Truth để finalizing một Booking.
 *
 * <p>Dùng cho cả thanh toán online (qua webhook) lẫn thanh toán tại quầy.
 * Tất cả domain operations được gói trong MỘT transaction để đảm bảo
 * tính atomic: booking + tickets + seats + loyalty cùng thành công hoặc cùng rollback.
 *
 * <p>Trách nhiệm duy nhất của UseCase này: chuyển trạng thái từ PENDING → CONFIRMED.
 * KHÔNG chứa logic payment (xử lý gateway, transactionId) — đó là việc của CompletePaymentUseCase.
 *
 * <p>Luồng:
 * <ol>
 *   <li>Fetch Booking → throw NotFound / Expired</li>
 *   <li>Booking.confirm() → status = CONFIRMED</li>
 *   <li>Set paidAt (từ Payment hoặc direct)</li>
 *   <li>Tickets.activate() → PENDING → ACTIVE</li>
 *   <li>ShowtimeSeats.book() → AVAILABLE → BOOKED</li>
 *   <li>seatLockService.releaseUserLocks() → xóa temp locks (theo userId + showtimeId)</li>
 *   <li>addPointsAfterBookingUseCase.execute() → tích điểm loyalty</li>
 * </ol>
 *
 * @author ducthinhn
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final BookingRepository bookingRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final SeatLockService seatLockService;
    private final AddPointsAfterBookingUseCase addPointsAfterBookingUseCase;

    /**
     * Overload cho thanh toán tại quầy (không qua payment gateway).
     * paidAt = thời điểm cashier xác nhận.
     */
    @Transactional
    public void execute(Long bookingId) {
        execute(bookingId, LocalDateTime.now(), null);
    }

    /**
     * Full version — cho thanh toán online qua CompletePaymentUseCase.
     *
     * @param bookingId ID của booking cần xác nhận
     * @param payment  Payment domain đã mark success (chứa paidAt). Có thể null.
     */
    @Transactional
    public void execute(Long bookingId, Payment payment) {
        LocalDateTime paidAt = (payment != null && payment.getPaidAt() != null)
                ? payment.getPaidAt()
                : LocalDateTime.now();
        execute(bookingId, paidAt, null);
    }

    /**
     * Internal execution — tất cả các overload gọi vào đây.
     */
    private void execute(Long bookingId, LocalDateTime paidAt, Long userId) {
        // 1. Fetch booking với pessimistic lock — ngăn race condition khi nhiều thread cùng confirm
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        // 2. Kiểm tra booking chưa expired
        if (booking.isExpired()) {
            throw BookingExceptions.expired(BookingId.of(bookingId));
        }

        // 3. Booking domain: PENDING → CONFIRMED
        booking.confirm();

        // 4. Set paidAt
        booking.setPaidAt(paidAt);

        // 5. Tickets domain: PENDING → ACTIVE
        booking.getTickets().forEach(Ticket::activate);

        // 6. ShowtimeSeats domain: AVAILABLE → BOOKED
        List<Long> seatIds = booking.getTickets().stream()
                .map(Ticket::getShowtimeSeatId)
                .toList();

        List<ShowtimeSeat> seats = showtimeSeatRepository.findAllByIds(seatIds);
        seats.forEach(ShowtimeSeat::book);

        // 7. Persist all domain changes qua repository (trong cùng transaction)
        bookingRepository.save(booking);
        seats.forEach(showtimeSeatRepository::save);

        // 8. Giải phóng seat locks (theo userId + showtimeId, KHÔNG dùng bookingId)
        // bookingId trong seat_locks có thể null (lock tạo trước khi booking tồn tại)
        Long effectiveUserId = (userId != null) ? userId : booking.getUserId();
        seatLockService.releaseUserLocks(effectiveUserId, booking.getShowtimeId());

        // 9. Loyalty: tích điểm cho user
        addPointsAfterBookingUseCase.execute(
                booking.getUserId(),
                booking.getTotalTicketPrice(),
                booking.getTotalComboPrice()
        );
    }
}
