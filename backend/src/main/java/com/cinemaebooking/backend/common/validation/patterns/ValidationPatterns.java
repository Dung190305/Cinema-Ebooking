package com.cinemaebooking.backend.common.validation.patterns;

/**
 * ValidationPatterns - Centralized regex definitions.
 * Responsibility:
 * - Store reusable regex patterns
 * - Avoid duplication across validation profiles
 * - Ensure consistency of format validation
 */
public final class ValidationPatterns {

    // ===================== CINEMA =====================
    public static final String CITY = "^[\\p{L}0-9\\s\\-\\.]+$";
    public static final String ADDRESS = "^[\\p{L}0-9\\s,\\-./#]+$";
    public static final String CINEMA_NAME = "^[\\p{L}0-9\\s\\-\\(\\)\\.&']+$";

    // ===================== ROOM =====================
    public static final String ROOM_NAME = "^[\\p{L}0-9\\s\\-\\(\\)]+$";

    // ===================== SEAT =====================

    // Ví dụ: A, B, C, AA, BB...
    public static final String ROW_LABEL = "^[A-Z]{1,3}$";



    // ===================== SEAT TYPE =====================
    // Ví dụ: Standard, VIP, Couple, Premium...
    public static final String SEAT_TYPE_NAME = "^[\\p{L}0-9\\s\\-]+$";

    // ===================== USER =====================
    public static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public static final String FULLNAME = "^\\p{L}+( \\p{L}+)*$";

    public static final String PHONE = "^\\+?\\d{7,15}$";

    public static final String TIME_HH_MM = "^([01]\\d|2[0-3]):[0-5]\\d$";
    private ValidationPatterns() {}


    // ===================== MOVIE_TITLE =====================
    public static final String MOVIE_TITLE =  "^[\\p{L}\\p{M}0-9\\s\\-:,.!?&'()]+$";

    // ===================== GENRE_NAME =====================
    public static final String GENRE_NAME = "^[\\p{L}\\s\\-]+$";

    public static final String SEAT_LABEL = "^[A-Z][1-9][0-9]{0,1}$";
}
