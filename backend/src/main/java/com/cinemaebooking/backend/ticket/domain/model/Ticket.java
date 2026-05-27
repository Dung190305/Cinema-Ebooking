package com.cinemaebooking.backend.ticket.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import com.cinemaebooking.backend.ticket.domain.valueObject.TicketId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ticket Domain Model
 * Chứa logic nghiệp vụ liên quan đến từng tấm vé lẻ trong một Booking.
 */
@Getter
@SuperBuilder
public class Ticket extends BaseEntity<TicketId> {

    private  String ticketCode;
    private  Long bookingId;
    private  Long showtimeSeatId;
    private String seatName;
    private  String seatType;     // Lấy từ SeatType (VIP, NORMAL...)
    private  BigDecimal price;

    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime checkedInAt;

    // ================== BUSINESS METHODS ==================

    /**
     * Thực hiện check-in vé khi khách vào phòng chiếu
     */
    public void checkIn() {
        if (this.status != TicketStatus.ACTIVE) {
            throw CommonExceptions.invalidInput("Chỉ có vé ở trạng thái ACTIVE mới có thể check-in.");
        }
        if (this.checkedInAt != null) {
            throw CommonExceptions.invalidInput("Vé này đã được check-in trước đó.");
        }

        this.status = TicketStatus.USED;
        this.checkedInAt = LocalDateTime.now();
    }

    /**
     * Hủy vé (thường đi kèm khi hủy Booking)
     */
    public void cancel() {
        if (this.status == TicketStatus.USED) {
            throw CommonExceptions.invalidInput("Không thể hủy vé đã qua sử dụng.");
        }
        this.status = TicketStatus.CANCELLED;
    }

    /**
     * Tiện ích lấy label (tương thích với Mapper bạn đã viết)
     */

    public boolean isUsed() {
        return this.status == TicketStatus.USED;
    }

    public static String generateTicketCode() {
        return "TIC" + System.currentTimeMillis() + (int)(Math.random() * 9000 + 1000);
    }
}
