package com.cinemaebooking.backend.user.application.usecase.admin;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.common.exception.domain.UserExceptions;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyAccountJpaEntity;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.repository.LoyaltyAccountJpaRepository;
import com.cinemaebooking.backend.user.application.dto.Response.UserDetailResponse;
import com.cinemaebooking.backend.user.application.mapper.UserDetailMapper;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUserByIdUseCase {

    private final UserRepository userRepository;
    private final BookingJpaRepository bookingJpaRepository;
    private final LoyaltyAccountJpaRepository loyaltyAccountJpaRepository;
    private final UserDetailMapper detailMapper;

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

    public UserDetailResponse execute(UserId id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserExceptions.notFound(id));

        Long userDbId = id.getValue();

        // ── Booking summary ───────────────────────────────────────────────────
        Long totalBookings = bookingJpaRepository.countByUserIdAndDeletedFalse(userDbId);
        BigDecimal totalSpentRaw = bookingJpaRepository
                .sumFinalAmountByUserIdAndStatusPaid(userDbId, BookingStatus.CONFIRMED);
        String totalSpent = formatVND(totalSpentRaw);

        var latestOpt = bookingJpaRepository.findFirstByUserIdAndDeletedFalseOrderByCreatedAtDesc(id.getValue());

        Long latestBookingId = null;
        String latestBookingCode = null;
        String latestMovieTitle = null;
        String latestCinemaName = null;
        String latestRoomName = null;
        String latestShowtimeStartTime = null;
        String latestFinalAmount = null;
        String latestStatus = null;
        String latestBookedAt = null;

        if (latestOpt.isPresent()) {
            var latest = latestOpt.get();
            latestBookingId = latest.getId();
            latestBookingCode = latest.getBookingCode();
            latestMovieTitle = latest.getMovieTitle();
            latestCinemaName = latest.getCinemaName();
            latestRoomName = latest.getRoomName();
            latestShowtimeStartTime = latest.getShowtimeStartTime() != null
                    ? latest.getShowtimeStartTime().format(DATETIME_FORMATTER)
                    : null;
            latestFinalAmount = formatVND(latest.getFinalAmount());
            latestStatus = latest.getStatus() != null ? latest.getStatus().name() : null;
            latestBookedAt = latest.getCreatedAt() != null
                    ? latest.getCreatedAt().format(DATETIME_FORMATTER)
                    : null;
        }

        // ── Loyalty summary ───────────────────────────────────────────────────
        Long loyaltyAccountId = null;
        String membershipTierName = null;
        String loyaltyNumber = null;
        Long currentPoints = null;
        Long lifetimePoints = null;
        String loyaltyTotalSpending = null;
        String loyaltyStatus = null;

        Optional<LoyaltyAccountJpaEntity> loyaltyOpt =
                loyaltyAccountJpaRepository.findByUserId(userDbId);

        if (loyaltyOpt.isPresent()) {
            LoyaltyAccountJpaEntity loyalty = loyaltyOpt.get();
            loyaltyAccountId = loyalty.getId();
            loyaltyNumber = loyalty.getLoyaltyNumber();
            currentPoints = loyalty.getCurrentPoints() != null
                    ? loyalty.getCurrentPoints().longValue() : 0L;
            lifetimePoints = loyalty.getLifetimePoints() != null
                    ? loyalty.getLifetimePoints().longValue() : 0L;
            loyaltyTotalSpending = formatVND(loyalty.getTotalSpending());
            loyaltyStatus = loyalty.getStatus() != null
                    ? loyalty.getStatus().name() : null;

            if (loyalty.getMembershipTier() != null) {
                membershipTierName = loyalty.getMembershipTier().getName();
            }
        }

        return detailMapper.toDetailResponse(
                user,
                totalBookings,
                totalSpent,
                membershipTierName,
                loyaltyNumber,
                currentPoints,
                lifetimePoints,
                loyaltyTotalSpending,
                loyaltyStatus,
                loyaltyAccountId,
                latestBookingId,
                latestBookingCode,
                latestMovieTitle,
                latestCinemaName,
                latestRoomName,
                latestShowtimeStartTime,
                latestFinalAmount,
                latestStatus,
                latestBookedAt
        );
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(0);
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}
