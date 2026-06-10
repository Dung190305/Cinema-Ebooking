package com.cinemaebooking.backend.review.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    private Long userId;
    /**
     * Booking code (không phải ID) của vé đã check-in gần nhất.
     * Backend sẽ tra bookingId từ code này để validate eligibility.
     */
    private String bookingCode;
    private Long movieId;
    private Integer rating;
    private String comment;
    private Boolean isSpoiler;
}
