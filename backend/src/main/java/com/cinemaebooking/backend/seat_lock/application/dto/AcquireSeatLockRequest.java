package com.cinemaebooking.backend.seat_lock.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO để khóa ghế tạm thời cho user.
 * FE gọi khi user click chọn ghế trên seat map.
 *
 * @author ducthinhn
 * @since 2026
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcquireSeatLockRequest {

    @NotNull(message = "userId không được để trống")
    @Positive(message = "userId phải là số dương")
    private Long userId;

    @NotNull(message = "showtimeId không được để trống")
    @Positive(message = "showtimeId phải là số dương")
    private Long showtimeId;

    @NotEmpty(message = "Danh sách ghế không được để trống")
    private List<@NotNull(message = "seatId không được null") Long> seatIds;
}
