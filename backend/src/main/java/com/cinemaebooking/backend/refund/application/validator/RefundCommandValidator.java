package com.cinemaebooking.backend.refund.application.validator;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.validation.engine.ValidationEngine;
import com.cinemaebooking.backend.common.validation.factory.ValidationFactory;
import com.cinemaebooking.backend.refund.application.dto.CreateRefundRequest;
import com.cinemaebooking.backend.refund.application.dto.ProcessRefundRequest;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundCommandValidator {

    private final RefundRepository refundRepository;

    public void validateCreateRequest(CreateRefundRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Create refund request must not be null");
        }

        ValidationEngine engine = ValidationEngine.of()
                .validate(request.getBookingId(), "bookingId", ValidationFactory.refund().bookingIdRules())
                .validate(request.getReason(), "reason", ValidationFactory.refund().reasonRules());

        if (engine.hasErrors()) {
            engine.throwIfInvalid();
            return;
        }

        if (refundRepository.existsByBookingId(request.getBookingId())) {
            throw CommonExceptions.invalidInput("Booking này đã có yêu cầu hoàn tiền.");
        }
    }

    public void validateProcessRequest(ProcessRefundRequest request) {
        if (request == null) {
            return;
        }

        ValidationEngine.of()
                .validate(request.getAdminNote(), "adminNote", ValidationFactory.refund().adminNoteRules())
                .throwIfInvalid();
    }
}
