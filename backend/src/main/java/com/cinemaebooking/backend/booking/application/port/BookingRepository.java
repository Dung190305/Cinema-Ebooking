package com.cinemaebooking.backend.booking.application.port;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    /**
     * Tìm booking với pessimistic lock — dùng trong ConfirmPaymentUseCase để tránh race condition.
     */
    Optional<Booking> findByIdForUpdate(Long id);

    Optional<Booking> findByBookingCode(String bookingCode);

    Page<Booking> findByUserId(Long userId, BookingStatus status, Pageable pageable);

    /**
     * laays booking của tất cả các user
     */

    Page<Booking> findAllForAdmin(
            Long movieId,
            BookingStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );

    Optional<Booking> findByUserIdAndShowtimeIdAndStatus(Long userId, Long showtimeId, BookingStatus status);

    List<Booking> findAllByShowtimeIdAndStatus(Long showtimeId, BookingStatus status);

    /**
     * Tìm Booking kèm theo tất cả các Ticket, Combo, Coupon.
     */
    Optional<Booking> findWithDetailsById(Long id);

    /**
     * Tìm các đơn hàng đã hết hạn nhưng chưa thanh toán.
     */
    List<Booking> findAllExpired(LocalDateTime now, BookingStatus status);

    /**
     * Kiểm tra xem một code đã tồn tại chưa (dùng khi generate bookingCode)
     */
    boolean existsByBookingCode(String bookingCode);


    Page<Booking> findAllForUser(
            Long userId,
            Long movieId,
            BookingStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );
}
