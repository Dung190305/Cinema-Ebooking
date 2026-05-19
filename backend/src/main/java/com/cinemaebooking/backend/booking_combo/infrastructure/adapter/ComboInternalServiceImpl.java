package com.cinemaebooking.backend.booking_combo.infrastructure.adapter;

import com.cinemaebooking.backend.booking.application.dto.CreateBookingRequest;
import com.cinemaebooking.backend.booking_combo.application.port.ComboInternalService;
import com.cinemaebooking.backend.booking_combo.domain.model.BookingCombo;
import com.cinemaebooking.backend.combo.application.port.ComboRepository;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComboInternalServiceImpl implements ComboInternalService {

    private final ComboRepository comboRepository;

    @Override
    public List<BookingCombo> getBookingCombos(List<CreateBookingRequest.ComboSelectionItem> selections) {

        if (selections == null || selections.isEmpty()) {
            return new ArrayList<>();
        }

        return selections.stream()
                .map(item -> {

                    ComboId comboId = ComboId.of(item.getComboId());
                    var combo = comboRepository.reserveStock(comboId, item.getQuantity());

                    var bookingCombo = BookingCombo.builder()
                            .comboId(combo.getId().getValue())
                            .comboName(combo.getName())
                            .unitPrice(combo.getPrice())
                            .build();
                    bookingCombo.updateQuantity(item.getQuantity());

                    return bookingCombo;
                })
                .collect(Collectors.toList());
    }
}
