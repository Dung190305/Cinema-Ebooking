package com.cinemaebooking.backend.payment.presentation;

import com.cinemaebooking.backend.payment.application.dto.CompletePaymentResponse;
import com.cinemaebooking.backend.payment.application.dto.CreatePaymentRequest;
import com.cinemaebooking.backend.payment.application.dto.CreatePaymentResponse;
import com.cinemaebooking.backend.payment.application.usecase.CancelPaymentUseCase;
import com.cinemaebooking.backend.payment.application.usecase.CompletePaymentUseCase;
import com.cinemaebooking.backend.payment.application.usecase.CreatePaymentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final CompletePaymentUseCase completePaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;

    @PostMapping
    public CreatePaymentResponse create(@RequestBody CreatePaymentRequest request) {
        return createPaymentUseCase.execute(request);
    }

    @PostMapping("/{code}/complete")
    public CompletePaymentResponse complete(@PathVariable String code) {
        return completePaymentUseCase.executeAndGetResponse(code);
    }

    @PostMapping("/{code}/cancel")
    public void cancel(@PathVariable String code) {
        cancelPaymentUseCase.execute(code);
    }
}
