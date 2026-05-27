package com.cinemaebooking.backend.booking.presentation;

import com.cinemaebooking.backend.booking.application.dto.BookingDetailResponse;
import com.cinemaebooking.backend.booking.application.dto.BookingListItemResponse;
import com.cinemaebooking.backend.booking.application.dto.CreateBookingRequest;
import com.cinemaebooking.backend.booking.application.usecase.*;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final GetBookingDetailUseCase getBookingDetailUseCase;
    private final GetUserBookingsUseCase getUserBookingsUseCase;
    private final GetAdminBookingsUseCase getAdminBookingsUseCase;
    private final GetPendingBookingUseCase getPendingBookingUseCase;

    // ================== LIST (DANH SÁCH TẤT CẢ ĐƠN HÀNG) ==================
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<BookingListItemResponse> listAllBookingsForAdmin(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) BookingStatus status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            Pageable pageable
    ) {
        return getAdminBookingsUseCase.execute(
                movieId,
                status,
                fromDate,
                toDate,
                pageable
        );
    }

    // ================== LIST (DANH SÁCH ĐƠN HÀNG) ==================
    @GetMapping
    public Page<BookingListItemResponse> listBookings(
            @RequestParam Long userId,
            @RequestParam(required = false) BookingStatus status,
            Pageable pageable) {
        return getUserBookingsUseCase.execute(userId, status, pageable);
    }

    // ================== CREATE (ĐẶT VÉ TẠM THỜI) ==================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDetailResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return createBookingUseCase.execute(request);
    }

    // ================== DETAIL (XEM CHI TIẾT) ==================
    @GetMapping("/{id}")
    public BookingDetailResponse getBookingDetail(@PathVariable Long id) {
        return getBookingDetailUseCase.execute(id);
    }

    @GetMapping("/pending")
    public BookingDetailResponse getPendingBooking(
            @RequestParam Long userId,
            @RequestParam Long showtimeId) {
        return getPendingBookingUseCase.execute(userId, showtimeId);
    }

    // ================== CANCEL (HỦY ĐẶT VÉ) ==================
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public void cancelBooking(@PathVariable Long id) {
        cancelBookingUseCase.execute(id);
    }

    // ================== CONFIRM PAYMENT (XÁC NHẬN THANH TOÁN) ==================
    @PostMapping("/{id}/confirm-payment")
    @ResponseStatus(HttpStatus.OK)
    public void confirmPayment(@PathVariable Long id) {
        confirmPaymentUseCase.execute(id);
    }
}
