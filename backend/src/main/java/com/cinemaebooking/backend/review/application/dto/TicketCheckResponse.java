package com.cinemaebooking.backend.review.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response trả về cho endpoint GET /reviews/movies/{movieId}/check-ticket.
 * Kiểm tra user có vé đã check-in cho phim này chưa.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCheckResponse {

    /**
     * true nếu user có ít nhất 1 vé đã check-in cho phim này.
     */
    private boolean hasCheckedInTicket;

    /**
     * Số lượng vé đã check-in cho phim này.
     * Dùng để FE hiển thị (ví dụ: "Bạn đã check-in 2 vé").
     */
    private int checkedInCount;

    /**
     * Mã booking của vé đã check-in gần nhất.
     * Trả về null nếu chưa có vé nào.
     */
    private String latestBookingCode;

    /**
     * ID booking của vé đã check-in gần nhất.
     * Dùng để tạo review (bookingId bắt buộc).
     */
    private Long latestBookingId;
}
