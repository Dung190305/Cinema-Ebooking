package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.dto.BookingDetailResponse;
import com.cinemaebooking.backend.booking.application.dto.CreateBookingRequest;
import com.cinemaebooking.backend.booking.application.mapper.BookingDetailResponseMapper;
import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.application.port.BookingLoyaltyPort;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking_combo.application.port.ComboInternalService;
import com.cinemaebooking.backend.booking_combo.domain.model.BookingCombo;
import com.cinemaebooking.backend.booking_coupon.application.port.CouponInternalService;
import com.cinemaebooking.backend.booking_coupon.domain.model.BookingCoupon;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeInternalService;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * CreateBookingUseCase — Tạo booking PENDING, chưa tạo Ticket.
 *
 * <p>Ticket chỉ được tạo tại {@link ConfirmPaymentUseCase} sau khi thanh toán thành công.
 * UseCase này chỉ chịu trách nhiệm:
 * <ol>
 *   <li>Validate + lock ghế (qua {@code validateAndLockSeats})</li>
 *   <li>Snapshot thông tin suất chiếu, combo, coupon, loyalty</li>
 *   <li>Tính tổng tiền</li>
 *   <li>Lưu Booking.PENDING kèm danh sách {@code showtimeSeatIds} — KHÔNG có tickets</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class CreateBookingUseCase {

    private final BookingRepository bookingRepository;
    private final BookingDetailResponseMapper mapper;
    private final ShowtimeInternalService showtimeService;
    private final ComboInternalService comboService;
    private final CouponInternalService couponService;
    private final BookingLoyaltyPort loyaltyPort;

    private static final int MAX_SEATS_PER_BOOKING = 8;

    @Transactional
    public BookingDetailResponse execute(CreateBookingRequest request) {

        // 1. Kiểm tra giới hạn số ghế
        if (request.getShowTimeSeatIds().size() > MAX_SEATS_PER_BOOKING) {
            throw CommonExceptions.invalidInput(
                    "Chỉ được đặt tối đa " + MAX_SEATS_PER_BOOKING + " ghế cho mỗi booking."
            );
        }

        // 2. Snapshot suất chiếu (movieTitle, cinemaName, roomName, startTime)
        var showtimeSnapshot = showtimeService.getSnapshot(request.getShowtimeId());

        // 3. Validate ghế + lock — KHÔNG tạo Ticket ở bước này
        //    Trả về List<ShowtimeSeat> đã qua validate để lấy seatIds
        List<ShowtimeSeat> validatedSeats = showtimeService.validateAndLockSeats(
                request.getShowtimeId(),
                request.getShowTimeSeatIds(),
                request.getUserId()
        );

        List<Long> seatIds = validatedSeats.stream()
                .map(s -> s.getId().getValue())
                .toList();

        // 4. Giá vé: tính từ ShowtimeSeat.price (snapshot tại thời điểm này)
        //    Lưu tổng vào booking, chi tiết sẽ được tạo trong Ticket lúc confirm
        BigDecimal totalTicketPrice = validatedSeats.stream()
                .map(ShowtimeSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Combo
        List<BookingCombo> combos = comboService.getBookingCombos(request.getCombos());

        // 6. Dựng Booking — tickets để rỗng, chỉ giữ seatIds
        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .userId(request.getUserId())
                .showtimeId(request.getShowtimeId())
                .movieId(showtimeSnapshot.getMovieId())
                .movieTitle(showtimeSnapshot.getMovieTitle())
                .cinemaName(showtimeSnapshot.getCinemaName())
                .roomName(showtimeSnapshot.getRoomName())
                .showtimeStartTime(showtimeSnapshot.getStartTime())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .showtimeSeatIds(seatIds)   // lưu để ConfirmPaymentUseCase dùng tạo Ticket
                .totalTicketPrice(totalTicketPrice)
                .combos(combos)
                .build();

        // 7. Loyalty tier discount
        var tierInfo = loyaltyPort.getMembershipTierInfo(request.getUserId());
        if (tierInfo != null) {
            booking.setMembershipTierName(tierInfo.tierName());
            BigDecimal discount = tierInfo.discountPercent() != null
                    ? tierInfo.discountPercent()
                    : BigDecimal.ZERO;
            booking.applyTierDiscount(discount);
        }

        // 8. Coupon
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            BigDecimal currentSubtotal = booking.calculateSubtotal();
            BookingCoupon couponData = couponService.validateAndGetCoupon(
                    request.getUserId(),
                    request.getCouponCode(),
                    currentSubtotal
            );
            booking.applyCoupon(couponData);
        }

        // 9. Tính finalAmount
        booking.calculateTotal();

        // 10. Persist
        Booking savedBooking = bookingRepository.save(booking);
        return mapper.toDetailResponse(savedBooking);
    }

    private String generateBookingCode() {
        return "BOK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}