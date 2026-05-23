package com.cinemaebooking.backend.user.application.mapper;

import com.cinemaebooking.backend.user.application.dto.Response.UserDetailResponse;
import com.cinemaebooking.backend.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserDetailMapper {

    public UserDetailResponse toDetailResponse(
            User user,
            Long totalBookings,
            String totalSpent,
            String membershipTierName,
            String loyaltyNumber,
            Long currentPoints,
            Long lifetimePoints,
            String loyaltyTotalSpending,
            String loyaltyStatus,
            Long loyaltyAccountId,
            Long latestBookingId,
            String latestBookingCode,
            String latestMovieTitle,
            String latestCinemaName,
            String latestRoomName,
            String latestShowtimeStartTime,
            String latestFinalAmount,
            String latestStatus,
            String latestBookedAt
    ) {
        if (user == null) return null;

        UserDetailResponse.LoyaltySummary loyalty = null;
        if (loyaltyAccountId != null) {
            loyalty = UserDetailResponse.LoyaltySummary.builder()
                    .loyaltyAccountId(loyaltyAccountId)
                    .membershipTierName(membershipTierName)
                    .loyaltyNumber(loyaltyNumber)
                    .currentPoints(currentPoints)
                    .lifetimePoints(lifetimePoints)
                    .totalSpending(loyaltyTotalSpending)
                    .status(loyaltyStatus)
                    .build();
        }

        UserDetailResponse.BookingSummary bookings = null;
        if (totalBookings != null && totalBookings > 0) {
            UserDetailResponse.LatestBooking latest = null;
            if (latestBookingId != null) {
                latest = UserDetailResponse.LatestBooking.builder()
                        .bookingId(latestBookingId)
                        .bookingCode(latestBookingCode)
                        .movieTitle(latestMovieTitle)
                        .cinemaName(latestCinemaName)
                        .roomName(latestRoomName)
                        .showtimeStartTime(latestShowtimeStartTime)
                        .finalAmount(latestFinalAmount)
                        .status(latestStatus)
                        .bookedAt(latestBookedAt)
                        .build();
            }
            bookings = UserDetailResponse.BookingSummary.builder()
                    .totalBookings(totalBookings)
                    .totalSpent(totalSpent)
                    .latestBooking(latest)
                    .build();
        }

        return UserDetailResponse.builder()
                .id(user.getId() != null ? user.getId().getValue() : null)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .loyalty(loyalty)
                .bookings(bookings)
                .build();
    }
}
