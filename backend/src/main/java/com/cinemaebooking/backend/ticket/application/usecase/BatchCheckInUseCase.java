package com.cinemaebooking.backend.ticket.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.common.exception.domain.TicketExceptions;
import com.cinemaebooking.backend.ticket.application.dto.TicketCheckInResponse;
import com.cinemaebooking.backend.ticket.application.port.TicketRepository;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import com.cinemaebooking.backend.ticket.domain.valueObject.TicketId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Check-in tất cả vé trong một booking cùng lúc.
 * Dùng cho màn hình QR Scanner của Admin.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchCheckInUseCase {

    private final TicketRepository ticketRepository;
    private final BookingJpaRepository bookingJpaRepository;

    @Transactional
    public List<TicketCheckInResponse> execute(String bookingCode) {
        var optBooking = bookingJpaRepository.findByBookingCodeAndDeletedFalse(bookingCode);
        if (optBooking.isEmpty()) {
            throw TicketExceptions.notFound(bookingCode);
        }

        var booking = optBooking.get();
        if (booking.getStatus() == com.cinemaebooking.backend.booking.domain.enums.BookingStatus.CANCELLED) {
            List<TicketCheckInResponse> results = new ArrayList<>();
            for (Ticket ticket : ticketRepository.findByBookingCode(bookingCode)) {
                results.add(TicketCheckInResponse.builder()
                        .ticketId(ticket.getId().getValue())
                        .ticketCode(ticket.getTicketCode())
                        .seatName(ticket.getSeatName())
                        .seatType(ticket.getSeatType())
                        .status(ticket.getStatus())
                        .price(ticket.getPrice())
                        .checkedInAt(ticket.getCheckedInAt())
                        .success(false)
                        .message("Booking đã bị hủy (refund), không thể check-in")
                        .build());
            }
            return results;
        }

        List<Ticket> tickets = ticketRepository.findByBookingCode(bookingCode);

        if (tickets.isEmpty()) {
            throw TicketExceptions.notFound(bookingCode);
        }

        List<TicketCheckInResponse> results = new ArrayList<>();

        for (Ticket ticket : tickets) {
            TicketCheckInResponse result = checkInSingleTicket(ticket);
            results.add(result);
        }

        log.info("Batch check-in for booking {}: {} tickets processed", bookingCode, results.size());
        return results;
    }

    private TicketCheckInResponse checkInSingleTicket(Ticket ticket) {
        try {
            TicketStatus currentStatus = ticket.getStatus();

            if (currentStatus == TicketStatus.USED) {
                return TicketCheckInResponse.builder()
                        .ticketId(ticket.getId().getValue())
                        .ticketCode(ticket.getTicketCode())
                        .seatName(ticket.getSeatName())
                        .seatType(ticket.getSeatType())
                        .status(currentStatus)
                        .price(ticket.getPrice())
                        .checkedInAt(ticket.getCheckedInAt())
                        .success(false)
                        .message("Vé đã được check-in trước đó")
                        .build();
            }

            if (currentStatus == TicketStatus.CANCELLED) {
                return TicketCheckInResponse.builder()
                        .ticketId(ticket.getId().getValue())
                        .ticketCode(ticket.getTicketCode())
                        .seatName(ticket.getSeatName())
                        .seatType(ticket.getSeatType())
                        .status(currentStatus)
                        .price(ticket.getPrice())
                        .checkedInAt(ticket.getCheckedInAt())
                        .success(false)
                        .message("Vé đã bị hủy")
                        .build();
            }

            ticket.checkIn();
            Ticket saved = ticketRepository.save(ticket);

            return TicketCheckInResponse.builder()
                    .ticketId(saved.getId().getValue())
                    .ticketCode(saved.getTicketCode())
                    .seatName(saved.getSeatName())
                    .seatType(saved.getSeatType())
                    .status(saved.getStatus())
                    .price(saved.getPrice())
                    .checkedInAt(saved.getCheckedInAt())
                    .success(true)
                    .message("Check-in thành công")
                    .build();

        } catch (Exception e) {
            log.error("Failed to check-in ticket {}: {}", ticket.getTicketCode(), e.getMessage());
            return TicketCheckInResponse.builder()
                    .ticketId(ticket.getId() != null ? ticket.getId().getValue() : null)
                    .ticketCode(ticket.getTicketCode())
                    .seatName(ticket.getSeatName())
                    .seatType(ticket.getSeatType())
                    .status(ticket.getStatus())
                    .price(ticket.getPrice())
                    .success(false)
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
        }
    }
}
