package com.cinemaebooking.backend.seat_lock.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO để giải phóng tất cả seat locks của user cho một suất chiếu.
 * FE gọi khi user bỏ chọn tất cả ghế hoặc hủy booking.
 *
 * @author ducthinhn
 * @since 2026
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseSeatLockRequest {

    @NotNull(message = "userId không được để trống")
    @Positive(message = "userId phải là số dương")
    private Long userId;

    @NotNull(message = "showtimeId không được để trống")
    @Positive(message = "showtimeId phải là số dương")
    private Long showtimeId;
}
