package com.cinemaebooking.backend.seat_lock.presentation;

import com.cinemaebooking.backend.seat_lock.application.dto.AcquireLockResponse;
import com.cinemaebooking.backend.seat_lock.application.dto.AcquireSeatLockRequest;
import com.cinemaebooking.backend.seat_lock.application.dto.ReleaseSeatLockRequest;
import com.cinemaebooking.backend.seat_lock.application.port.SeatLockService;
import com.cinemaebooking.backend.seat_lock.domain.valueobject.SeatLockId;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SeatLockController - Public API để FE gọi khi user chọn / hủy ghế.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/v1/seat-locks/acquire - Khóa ghế</li>
 *   <li>POST /api/v1/seat-locks/release - Giải phóng lock của user cho 1 suất chiếu</li>
 *   <li>GET  /api/v1/seat-locks/check/{seatId} - Kiểm tra ghế có bị lock bởi user khác không</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
@RestController
@RequestMapping("/api/v1/seat-locks")
@RequiredArgsConstructor
public class SeatLockController {

    private final SeatLockService seatLockService;

    // ================== ACQUIRE (KHÓA GHẾ) ==================
    @PostMapping("/acquire")
    @ResponseStatus(HttpStatus.CREATED)
    public AcquireLockResponse acquireLocks(@Valid @RequestBody AcquireSeatLockRequest request) {
        return seatLockService.acquireLocks(
                request.getUserId(),
                request.getShowtimeId(),
                request.getSeatIds()
        );
    }

    // ================== RELEASE (GIẢI PHÓNG LOCK) ==================
    @PostMapping("/release")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, Boolean>> releaseLocks(@Valid @RequestBody ReleaseSeatLockRequest request) {
        seatLockService.releaseUserLocks(request.getUserId(), request.getShowtimeId());
        return ResponseEntity.ok(Map.of("released", true));
    }

    // ================== CHECK (KIỂM TRA LOCK STATUS) ==================
    @GetMapping("/check/{seatId}")
    public ResponseEntity<Map<String, Object>> checkLock(
            @PathVariable Long seatId,
            @RequestParam Long currentUserId
    ) {
        SeatLockId.of(seatId);
        if (currentUserId == null) {
            throw CommonExceptions.invalidInput("currentUserId không được để trống");
        }

        boolean lockedByOther = seatLockService.isLockedByOther(seatId, currentUserId);
        return ResponseEntity.ok(Map.of(
                "seatId", seatId,
                "lockedByOther", lockedByOther
        ));
    }
}
