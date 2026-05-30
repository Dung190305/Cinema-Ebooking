package com.cinemaebooking.backend.ticket.presentation;

import com.cinemaebooking.backend.booking.application.dto.BookingDetailResponse;
import com.cinemaebooking.backend.booking.application.usecase.GetBookingDetailUseCase;
import com.cinemaebooking.backend.ticket.application.dto.TicketCheckInResponse;
import com.cinemaebooking.backend.ticket.application.usecase.BatchCheckInUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CheckInScannerController - Dành cho màn hình quét QR check-in của Admin.
 *
 * Luồng:
 *  1. [GET] /lookup?code=ABC123  → lấy thông tin booking (hiển thị trước khi check-in)
 *  2. [POST] /checkin/{bookingCode} → check-in tất cả vé trong booking
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CheckInScannerController {

    private final GetBookingDetailUseCase getBookingDetailUseCase;
    private final BatchCheckInUseCase batchCheckInUseCase;

    /**
     * [GET] /api/v1/checkin/lookup?code=ABC123
     *
     * Tra cuu thong tin booking tu bookingCode (dung de hien thi truoc khi xac nhan check-in).
     * Dung boi scanner HTML khi quet QR thanh cong.
     */
    @GetMapping("/lookup")
    public ResponseEntity<BookingDetailResponse> lookupBooking(
            @RequestParam("code") String bookingCode
    ) {
        String trimmed = bookingCode.trim();
        log.info("[CHECKIN] Lookup request for bookingCode: '{}'", trimmed);
        BookingDetailResponse booking = getBookingDetailUseCase.executeByBookingCode(trimmed);
        log.info("[CHECKIN] Found booking: id={}, code={}, status={}", booking.getBookingId(), booking.getBookingCode(), booking.getStatus());
        return ResponseEntity.ok(booking);
    }

    /**
     * [POST] /api/v1/checkin/{bookingCode}
     *
     * Check-in tat ca cac ve trong mot booking.
     * Dung khi admin xac nhan tu trang scanner.
     *
     * Tra ve danh sach cac ve da duoc check-in (thanh cong) va loi (neu co).
     */
    @PostMapping("/{bookingCode}")
    public ResponseEntity<BatchCheckInResponse> checkInBooking(
            @PathVariable String bookingCode
    ) {
        List<TicketCheckInResponse> results = batchCheckInUseCase.execute(bookingCode.trim());
        long successCount = results.stream().filter(r -> r.getSuccess()).count();
        long failCount = results.size() - successCount;
        return ResponseEntity.ok(new BatchCheckInResponse(
                bookingCode,
                results.size(),
                (int) successCount,
                (int) failCount,
                results
        ));
    }

    public record BatchCheckInResponse(
            String bookingCode,
            int totalTickets,
            int successCount,
            int failCount,
            List<TicketCheckInResponse> tickets
    ) {}
}
