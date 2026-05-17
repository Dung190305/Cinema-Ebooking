package com.cinemaebooking.backend.combo.application.usecase;

import com.cinemaebooking.backend.combo.application.dto.ComboResponse;
import com.cinemaebooking.backend.combo.application.mapper.ComboResponseMapper;
import com.cinemaebooking.backend.combo.application.port.ComboRepository;
import com.cinemaebooking.backend.combo.domain.enums.ComboStatus;
import com.cinemaebooking.backend.combo.domain.model.Combo;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import com.cinemaebooking.backend.common.exception.domain.ComboExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivateComboUseCase {

    private final ComboRepository comboRepository;
    private final ComboResponseMapper mapper;

    @Transactional
    public ComboResponse execute(ComboId id) {
        Combo combo = comboRepository.findByIdForUpdate(id)
                .orElseThrow(() -> ComboExceptions.notFound(id));

        if (combo.getStock() == null || combo.getStock() <= 0) {
            throw ComboExceptions.outOfStock();
        }

        combo.changeStatus(ComboStatus.ACTIVE);
        Combo saved = comboRepository.update(combo);
        return mapper.toResponse(saved);
    }
}
