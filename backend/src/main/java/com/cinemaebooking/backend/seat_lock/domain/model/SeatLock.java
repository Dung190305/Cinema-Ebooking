package com.cinemaebooking.backend.seat_lock.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.seat_lock.domain.valueobject.SeatLockId;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * SeatLock - Domain model đại diện cho việc khóa ghế tạm thời.
 *
 * <p>Responsibility:
 * <ul>
 *   <li>Gói trạng thái khóa ghế: showtimeSeatId, userId, bookingId, expiredAt</li>
 *   <li>Cung cấp domain logic kiểm tra hết hạn</li>
 *   <li>KHÔNG chứa logic persistence (nằm ở JPA entity)</li>
 *   <li>KHÔNG chứa logic lock ghế (nằm ở UseCase)</li>
 * </ul>
 *
 * <p>Luồng hoạt động:
 * <ol>
 *   <li>User chọn ghế → AcquireSeatLockUseCase → tạo SeatLock → FE hiển thị ghế "đang giữ"</li>
 *   <li>User thanh toán thành công → Booking confirmed → ghế chuyển BOOKED</li>
 *   <li>Booking hết hạn / bị hủy → ReleaseSeatLockUseCase → ghế chuyển AVAILABLE</li>
 *   <li>Scheduled job chạy định kỳ dọn lock hết hạn → ReleaseExpiredSeatLocksUseCase</li>
 * </ol>
 *
 * <p>Lưu ý: Ghế bị khóa sẽ không thể bị book bởi user khác.
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class SeatLock extends BaseEntity<SeatLockId> {

    private final Long showtimeSeatId;
    private final Long userId;
    private final Long bookingId;
    private final LocalDateTime lockedAt;
    private final LocalDateTime expiredAt;

    /**
     * Kiểm tra xem lock có đã hết hạn chưa.
     *
     * @return true nếu lock đã hết hạn
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * Kiểm tra xem lock có đang active (chưa hết hạn).
     *
     * @return true nếu lock còn hiệu lực
     */
    public boolean isActive() {
        return !isExpired();
    }

    /**
     * Kiểm tra xem lock này có thuộc về user không.
     *
     * @param checkUserId userId cần kiểm tra
     * @return true nếu lock thuộc về user đó
     */
    public boolean belongsTo(Long checkUserId) {
        return this.userId != null && this.userId.equals(checkUserId);
    }
}
