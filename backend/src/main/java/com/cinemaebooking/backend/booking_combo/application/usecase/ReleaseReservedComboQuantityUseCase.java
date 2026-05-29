package com.cinemaebooking.backend.booking_combo.application.usecase;

import com.cinemaebooking.backend.booking_combo.application.port.BookingComboRepository;
import com.cinemaebooking.backend.booking_combo.domain.model.BookingCombo;
import com.cinemaebooking.backend.combo.application.port.ComboRepository;
import com.cinemaebooking.backend.combo.domain.model.Combo;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import com.cinemaebooking.backend.common.exception.domain.ComboExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReleaseReservedComboQuantityUseCase {
    private final ComboRepository comboRepository;
    private final BookingComboRepository bookingComboRepository;

    public void execute(Long bookingId) {
        List<BookingCombo> bookingCombos = bookingComboRepository.findByBookingId(bookingId);
        bookingCombos.forEach(bookingCombo -> {
            ComboId comboId = ComboId.of(bookingCombo.getComboId());
            Combo combo = comboRepository.findById(comboId).orElseThrow(
                    () -> ComboExceptions.notFound(comboId)
            );

            combo.increaseStock(bookingCombo.getQuantity());
            comboRepository.updateQuantity(combo);
        });

    }
}
