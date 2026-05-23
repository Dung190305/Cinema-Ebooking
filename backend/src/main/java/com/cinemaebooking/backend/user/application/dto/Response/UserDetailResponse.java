package com.cinemaebooking.backend.user.application.dto.Response;

import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import com.cinemaebooking.backend.user.domain.valueObject.UserGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UserDetailResponse — Enriched user detail for admin view.
 * Includes avatar, profile, role, status, timestamps,
 * loyalty summary, and latest booking summary.
 *
 * <p>Used by GET /api/v1/admin/{id}</p>
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponse {

    // ── Profile ────────────────────────────────────────────────────────────────
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserGender gender;
    private String avatarUrl;

    // ── Role & Status ─────────────────────────────────────────────────────────
    private UserRole role;
    private UserStatus status;

    // ── Audit ─────────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;

    // ── Loyalty Summary ───────────────────────────────────────────────────────
    private LoyaltySummary loyalty;

    // ── Booking Summary ────────────────────────────────────────────────────────
    private BookingSummary bookings;

    /**
     * Loyalty account summary embedded in user detail.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoyaltySummary {
        private Long loyaltyAccountId;
        private String membershipTierName;
        private String loyaltyNumber;
        private Long currentPoints;
        private Long lifetimePoints;
        private String totalSpending;   // formatted as String (VND)
        private String status;
    }

    /**
     * Booking statistics summary embedded in user detail.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingSummary {
        private Long totalBookings;
        private String totalSpent;     // formatted as String (VND)
        private LatestBooking latestBooking;
    }

    /**
     * The most recent booking of the user.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LatestBooking {
        private Long bookingId;
        private String bookingCode;
        private String movieTitle;
        private String cinemaName;
        private String roomName;
        private String showtimeStartTime; // formatted String
        private String finalAmount;       // formatted String (VND)
        private String status;
        private String bookedAt;          // formatted String "dd/MM/yyyy, HH:mm"
    }
}
