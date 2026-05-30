package com.cinemaebooking.backend.common.validation.factory;

import com.cinemaebooking.backend.common.validation.domain.*;

/**
 * ValidationFactory - Entry point to domain validation profiles.
 * Responsibility:
 * - Centralize access to validation profiles
 * - Decouple validators from direct profile dependencies
 * - Provide scalable entry point for multi-domain validation
 * Note:
 * - Lightweight abstraction (no Spring bean)
 * @author Hieu Nguyen
 * @since 2026
 */
public final class ValidationFactory {

    private ValidationFactory() {}

    public static CinemaValidationProfile cinema() {
        return CinemaValidationProfile.INSTANCE;
    }
    public static RoomValidationProfile   room(){ return RoomValidationProfile.INSTANCE;}
    public static SeatTypeValidationProfile seatType(){ return SeatTypeValidationProfile.INSTANCE;}
    public static UserValidationProfile user(){ return UserValidationProfile.INSTANCE;}
    public static MovieValidationProfile movie() { return MovieValidationProfile.INSTANCE; }
    public static GenreValidationProfile genre() { return GenreValidationProfile.INSTANCE; }
    public static CouponValidationProfile coupon() {return CouponValidationProfile.INSTANCE;}
    public static ComboValidationProfile combo() {return ComboValidationProfile.INSTANCE;}
    public static ShowtimeValidationProfile showtime(){ return ShowtimeValidationProfile.INSTANCE;}
    public static UserCouponValidationProfile userCoupon() {return UserCouponValidationProfile.INSTANCE;}
    public static BookingValidationProfile booking(){ return BookingValidationProfile.INSTANCE;}
    public static TicketValidationProfile ticket(){ return TicketValidationProfile.INSTANCE;}
    public static RefundValidationProfile refund(){ return RefundValidationProfile.INSTANCE;}
}
