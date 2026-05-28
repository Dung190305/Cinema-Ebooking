package com.cinemaebooking.backend.seat_lock.application.usecase;

import com.cinemaebooking.backend.seat_lock.application.port.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ScheduledJob giải phóng seat lock hết hạn.
 *
 * <p>Chạy mỗi 1 phút, tìm tất cả lock đã hết hạn và giải phóng ghế về AVAILABLE.
 * Đồng thời xóa hard record trong seat_locks table.
 *
 */
@Component
@RequiredArgsConstructor
public class ReleaseExpiredSeatLocksJob {

    private final SeatLockService seatLockService;

    /**
     * Chạy mỗi 1 phút để cleanup lock hết hạn.
     * Dùng fixedDelay để đảm bảo job này không chồng chéo nhau.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanupExpiredLocks() {
        seatLockService.releaseExpiredLocks();
    }
}
