package com.cinemaebooking.backend.movie.domain.enums;

import java.time.LocalDate;

/**
 * MovieStatus: Trạng thái phát hành của phim.
 * @author Hieu Nguyen
 * @since 2026
 */
public enum MovieStatus {
    COMING_SOON,
    NOW_SHOWING,
    ENDED;

    // ✅ Logic tập trung tại đây, dễ test, dễ thay đổi
    public static MovieStatus from(LocalDate releaseDate, LocalDate showingEndDate) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(releaseDate)) {
            return COMING_SOON;
        }
        if (showingEndDate != null && today.isAfter(showingEndDate)) {
            return ENDED;
        }
        return NOW_SHOWING;
    }
}