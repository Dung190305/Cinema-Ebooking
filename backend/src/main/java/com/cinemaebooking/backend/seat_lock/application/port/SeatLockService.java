package com.cinemaebooking.backend.seat_lock.application.port;

import com.cinemaebooking.backend.seat_lock.application.dto.AcquireLockResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SeatLockService - Internal service port (dùng nội bộ bởi các module khác).
 *
 * <p>Module khác (booking, payment) sẽ dùng interface này thay vì gọi trực tiếp UseCase,
 * để giữ tính encapsulation và dễ mock trong unit test.
 *
 * <p>Responsibility:
 * <ul>
 *   <li>Acquire seat lock: Khóa ghế tạm thời cho user</li>
 *   <li>Release seat lock: Giải phóng lock khi booking thành công</li>
 *   <li>Release user locks: Giải phóng tất cả lock của user cho một suất chiếu</li>
 *   <li>Release expired locks: Giải phóng lock hết hạn (scheduled job)</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
public interface SeatLockService {

    /**
     * Khóa một hoặc nhiều ghế cho user trong một suất chiếu.
     * Tạo lock với thời hạn mặc định (7 phút).
     *
     * @param userId      user đang chọn ghế
     * @param showtimeId  suất chiếu muốn khóa ghế
     * @param seatIds     danh sách showtimeSeatId cần khóa
     * @return kết quả khóa ghế (danh sách ghế đã khóa + thời hạn)
     * @throws com.cinemaebooking.backend.common.exception.BaseException nếu ghế đã bị khóa
     */
    AcquireLockResponse acquireLocks(Long userId, Long showtimeId, List<Long> seatIds);

    /**
     * Extend seat locks cho một booking — dùng khi tạo payment.
     * Ghế đã lock sẽ được giữ thêm đến thời điểm paymentExpiredAt.
     * Nếu ghế chưa bị lock bởi user này thì bỏ qua (booking ghế qua seat map trước đó).
     *
     * @param bookingId booking đang thanh toán
     * @param userId user sở hữu booking
     * @param showtimeId suất chiếu
     * @param paymentExpiredAt thời điểm hết hạn thanh toán (thường = 15 phút)
     */
    void extendLocksForPayment(Long bookingId, Long userId, Long showtimeId, LocalDateTime paymentExpiredAt);

    /**
     * Giải phóng tất cả lock của user cho một suất chiếu.
     * Dùng khi user hủy chọn ghế (FE gọi trước khi booking).
     *
     * @param userId     user cần giải phóng lock
     * @param showtimeId suất chiếu cần giải phóng
     */
    void releaseUserLocks(Long userId, Long showtimeId);

    /**
     * Giải phóng tất cả lock của một booking.
     * Dùng khi booking bị hủy hoặc hết hạn — ghế trả về AVAILABLE.
     *
     * @param bookingId booking cần giải phóng
     */
    void releaseLocksByBookingId(Long bookingId);

    /**
     * Giải phóng tất cả lock đã hết hạn.
     * Dùng bởi scheduled job (Spring Scheduler).
     */
    void releaseExpiredLocks();

    /**
     * Kiểm tra ghế có đang bị lock không (dùng trong seat map API).
     *
     * @param seatId showtimeSeatId
     * @param currentUserId user hiện tại (để check xem có phải lock của mình không)
     * @return true nếu ghế bị lock bởi user khác
     */
    boolean isLockedByOther(Long seatId, Long currentUserId);

    /**
     * Kiểm tra một ghế có đang được lock bởi user cụ thể hay không (lock còn hiệu lực).
     *
     * @param seatId showtimeSeatId
     * @param userId user cần kiểm tra
     * @return true nếu ghế đang được lock bởi user này và chưa hết hạn
     */
    boolean isLockedByUser(Long seatId, Long userId);
}
