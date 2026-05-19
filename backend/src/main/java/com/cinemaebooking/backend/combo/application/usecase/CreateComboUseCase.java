package com.cinemaebooking.backend.combo.application.usecase;

import com.cinemaebooking.backend.combo.application.dto.ComboResponse;
import com.cinemaebooking.backend.combo.application.dto.CreateComboRequest;
import com.cinemaebooking.backend.combo.application.mapper.ComboResponseMapper;
import com.cinemaebooking.backend.combo.application.port.ComboRepository;
import com.cinemaebooking.backend.combo.application.validator.ComboCommandValidator;
import com.cinemaebooking.backend.combo.domain.enums.ComboStatus;
import com.cinemaebooking.backend.combo.domain.model.Combo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateComboUseCase {

    private final ComboRepository comboRepository;
    private final ComboResponseMapper mapper;
    private final ComboCommandValidator validator;

    public ComboResponse execute(CreateComboRequest request) {
        validator.validateCreateRequest(request);

        Combo combo = buildCombo(request);
        combo.validateForCreate(); // domain-level validation

        Combo saved = comboRepository.create(combo);
        return mapper.toResponse(saved);
    }

    private Combo buildCombo(CreateComboRequest request) {
        return Combo.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null)
                .status(determineInitialStatus(request.getStock()))
                .build();
    }

    private ComboStatus determineInitialStatus(Integer stock) {
        return stock != null && stock > 0 ? ComboStatus.ACTIVE : ComboStatus.INACTIVE;
    }
}
