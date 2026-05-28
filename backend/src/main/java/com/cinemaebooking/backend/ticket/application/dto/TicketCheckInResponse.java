package com.cinemaebooking.backend.ticket.application.dto;

import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCheckInResponse {

    private Long ticketId;
    private String ticketCode;
    private String seatName;
    private String seatType;
    private TicketStatus status;
    private BigDecimal price;
    private LocalDateTime checkedInAt;
    private Boolean success;
    private String message; // Lý do lỗi nếu có
}
