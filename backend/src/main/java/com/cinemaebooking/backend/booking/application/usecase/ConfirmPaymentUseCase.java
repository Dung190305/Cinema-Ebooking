package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.loyalty.application.usecase.transactional.AddPointsAfterBookingUseCase;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.room_layout.application.port.roomLayout.RoomLayoutInternalService;
import com.cinemaebooking.backend.room_layout.domain.model.roomLayoutSeat.RoomLayoutSeat;
import com.cinemaebooking.backend.seat_lock.application.port.SeatLockService;
import com.cinemaebooking.backend.showtime_seat.application.port.ShowtimeSeatRepository;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * ConfirmPaymentUseCase — Single Source of Truth để finalize một Booking.
 *
 * <p>Đây là nơi DUY NHẤT tạo {@link Ticket}. Ticket chỉ tồn tại khi thanh toán
 * đã thành công — không có Ticket nào ở trạng thái PENDING lưu trước đó.
 *
 * <p>Trách nhiệm:
 * <ol>
 *   <li>Fetch Booking → validate chưa expired</li>
 *   <li>Tạo {@link Ticket} từ {@code booking.showtimeSeatIds} (snapshot giá, tên ghế tại đây)</li>
 *   <li>Booking.confirm() → PENDING → CONFIRMED</li>
 *   <li>Set paidAt</li>
 *   <li>ShowtimeSeats → BOOKED</li>
 *   <li>Giải phóng seat locks</li>
 *   <li>Tích điểm loyalty</li>
 * </ol>
 *
 * <p>Dùng cho cả thanh toán online (qua {@code CompletePaymentUseCase})
 * lẫn thanh toán tại quầy.
 */
@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ConfirmPaymentUseCase.class);

    private final BookingRepository bookingRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final RoomLayoutInternalService layoutService;
    private final SeatLockService seatLockService;
    private final AddPointsAfterBookingUseCase addPointsAfterBookingUseCase;

    /** Thanh toán tại quầy — paidAt = now(). */
    @Transactional
    public void execute(Long bookingId) {
        doExecute(bookingId, LocalDateTime.now(), null);
    }

    /** Thanh toán online qua CompletePaymentUseCase. */
    @Transactional
    public void execute(Long bookingId, Payment payment) {
        LocalDateTime paidAt = (payment != null && payment.getPaidAt() != null)
                ? payment.getPaidAt()
                : LocalDateTime.now();
        doExecute(bookingId, paidAt, null);
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private void doExecute(Long bookingId, LocalDateTime paidAt, Long overrideUserId) {

        // 1. Fetch booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        // 2. Không confirm booking đã hết hạn
        if (booking.isExpired()) {
            throw BookingExceptions.expired(BookingId.of(bookingId));
        }

        // 3. Load ShowtimeSeats từ seatIds đã lưu trong booking
        List<Long> seatIds = booking.getShowtimeSeatIds();
        List<ShowtimeSeat> seats = showtimeSeatRepository.findAllByIds(seatIds);

        // 4. Tạo Tickets — đây là lần đầu tiên và duy nhất Ticket được tạo
        List<Ticket> tickets = buildTickets(seats);

        // 5. Gắn tickets vào booking
        booking.setTickets(tickets);

        // 6. Booking domain: PENDING → CONFIRMED
        booking.confirm();

        // 7. Set paidAt
        booking.setPaidAt(paidAt);

        // 8. ShowtimeSeats: LOCKED/AVAILABLE → BOOKED
        seats.forEach(ShowtimeSeat::book);

        // 9. Persist tất cả trong cùng một transaction
        bookingRepository.save(booking);
        seats.forEach(showtimeSeatRepository::save);

        // 10. Giải phóng seat locks
        Long effectiveUserId = (overrideUserId != null) ? overrideUserId : booking.getUserId();
        try {
            seatLockService.releaseUserLocks(effectiveUserId, booking.getShowtimeId());
        } catch (Exception e) {
            log.warn("Failed to release seat locks for booking {}: {}", bookingId, e.getMessage());
        }

        try {
            addPointsAfterBookingUseCase.execute(
                    booking.getUserId(),
                    booking.getTotalTicketPrice(),
                    booking.getTotalComboPrice());
        } catch (Exception e) {
            log.warn("Failed to add loyalty points for booking {}: {}", bookingId, e.getMessage());
        }

        // 11. Tích điểm loyalty
        addPointsAfterBookingUseCase.execute(
                booking.getUserId(),
                booking.getTotalTicketPrice(),
                booking.getTotalComboPrice()
        );
    }

    /**
     * Tạo danh sách Ticket từ ShowtimeSeat — snapshot giá và tên ghế tại thời điểm confirm.
     * Không gọi validate vì ghế đã được validate + lock từ CreateBookingUseCase.
     */
    private List<Ticket> buildTickets(List<ShowtimeSeat> seats) {
        List<Long> layoutSeatIds = seats.stream()
                .map(ShowtimeSeat::getRoomLayoutSeatId)
                .distinct()
                .toList();

        Map<Long, RoomLayoutSeat> layoutSeatMap = layoutService.getMapByIds(layoutSeatIds);

        List<Long> seatTypeIds = layoutSeatMap.values().stream()
                .map(RoomLayoutSeat::getSeatTypeId)
                .distinct()
                .toList();
        Map<Long, String> seatTypeNameMap = layoutService.getSeatTypeNameMap(seatTypeIds);

        return seats.stream().map(seat -> {
            String typeName = Optional.ofNullable(layoutSeatMap.get(seat.getRoomLayoutSeatId()))
                    .map(rls -> seatTypeNameMap.getOrDefault(rls.getSeatTypeId(), "STANDARD"))
                    .orElse("STANDARD");

            return Ticket.builder()
                    .showtimeSeatId(seat.getId().getValue())
                    .seatType(typeName)
                    .seatName(seat.getSeatNumber())
                    .price(seat.getPrice())           // snapshot giá tại thời điểm confirm
                    .status(TicketStatus.ACTIVE)      // tạo ra là ACTIVE luôn, không qua PENDING
                    .createdAt(LocalDateTime.now())
                    .ticketCode(generateTicketCode())
                    .build();
        }).collect(Collectors.toList());
    }

    private String generateTicketCode() {
        return "TIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}