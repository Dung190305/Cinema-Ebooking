package com.cinemaebooking.backend.showtime.application.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * ShowtimeSnapshot - Đối tượng vận chuyển dữ liệu Snapshot từ Showtime sang Booking.
 */
@AllArgsConstructor // Biến tất cả field thành private final, tạo Constructor và Getter
@Builder
@Getter
public class ShowtimeSnapshot {
    String movieTitle;
    String cinemaName;
    String roomName;
    Instant startTime;
}
