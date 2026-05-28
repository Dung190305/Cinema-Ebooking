package com.cinemaebooking.backend.booking.application.validator;

import com.cinemaebooking.backend.booking.application.dto.CreateBookingRequest;
import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.validation.engine.ValidationEngine;
import com.cinemaebooking.backend.common.validation.factory.ValidationFactory;
import com.cinemaebooking.backend.ticket.application.port.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * BookingCommandValidator
 * Sử dụng BookingValidationProfile thông qua ValidationFactory để kiểm tra DTO.
 */
@Component
@RequiredArgsConstructor
public class BookingCommandValidator {

    private final TicketRepository ticketRepository;

    public void validateCreateRequest(CreateBookingRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Create booking request must not be null");
        }

        // ================== PHASE 1: FORMAT VALIDATION ==================
        // Sử dụng các rules đã định nghĩa trong BookingValidationProfile
        ValidationEngine engine = ValidationEngine.of()
                .validate(request.getShowtimeId(), "showtimeId", ValidationFactory.booking().showtimeIdRules())
                .validate(request.getShowTimeSeatIds(), "showTimeSeatIds", ValidationFactory.booking().seatIdsRules())
                .validate(request.getCouponCode(), "couponCode", ValidationFactory.booking().couponCodeRules());

        // Validate từng combo trong danh sách (nếu có)
        if (request.getCombos() != null) {
            for (int i = 0; i < request.getCombos().size(); i++) {
                String fieldPath = "combos[" + i + "].quantity";
                engine.validate(request.getCombos().get(i).getQuantity(),
                        fieldPath,
                        ValidationFactory.booking().comboQuantityRules());
            }
        }

        // Nếu có lỗi định dạng (ví dụ: > 8 ghế), dừng lại và quăng lỗi ngay
        engine.throwIfInvalid();


    }
}