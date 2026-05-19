package com.cinemaebooking.backend.combo.application.mapper;

import com.cinemaebooking.backend.combo.application.dto.ComboResponse;
import com.cinemaebooking.backend.combo.domain.model.Combo;
import org.springframework.stereotype.Component;

@Component
public class ComboResponseMapper {

    public ComboResponse toResponse(Combo combo) {
        if (combo == null) return null;

        return new ComboResponse(
                combo.getId() != null ? combo.getId().getValue() : null,
                combo.getName(),
                combo.getDescription(),
                combo.getPrice(),
                combo.getOriginalPrice(),
                combo.getStock(),
                combo.getImageUrl(),
                combo.getStatus(),
                combo.getDisplayStatus()
        );
    }
}
