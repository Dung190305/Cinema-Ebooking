package com.cinemaebooking.backend.seat_lock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về kết quả acquire seat lock.
 *
 * @author ducthinhn
 * @since 2026
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcquireLockResponse {

    private boolean success;
    private List<LockedSeatDto> lockedSeats;
    private LocalDateTime expiredAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LockedSeatDto {
        private Long seatId;
        private String seatNumber;
        private LocalDateTime expiredAt;
    }
}
