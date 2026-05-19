package com.cinemaebooking.backend.combo.application.usecase;

import com.cinemaebooking.backend.combo.application.dto.ComboResponse;
import com.cinemaebooking.backend.combo.application.dto.UpdateComboRequest;
import com.cinemaebooking.backend.combo.application.mapper.ComboResponseMapper;
import com.cinemaebooking.backend.combo.application.port.ComboRepository;
import com.cinemaebooking.backend.combo.application.validator.ComboCommandValidator;
import com.cinemaebooking.backend.combo.domain.model.Combo;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import com.cinemaebooking.backend.common.exception.domain.ComboExceptions;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateComboUseCase {

    private final ComboRepository comboRepository;
    private final ComboResponseMapper mapper;
    private final ComboCommandValidator validator;

    @Transactional
    public ComboResponse execute(ComboId id, UpdateComboRequest request) {
        validator.validateUpdateRequest(id, request);

        Combo combo = loadComboForUpdate(id);

        applyStockIncrease(combo, request.getStock());

        combo.update(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getImageUrl(),
                request.getStatus()
        );

        Combo saved = persist(combo);
        return mapper.toResponse(saved);
    }

    private Combo loadComboForUpdate(ComboId id) {
        return comboRepository.findByIdForUpdate(id)
                .orElseThrow(() -> ComboExceptions.notFound(id));
    }

    private void applyStockIncrease(Combo combo, Integer newStock) {
        if (newStock == null) {
            return;
        }

        int currentStock = combo.getStock() == null ? 0 : combo.getStock();
        int increaseAmount = newStock - currentStock;
        if (increaseAmount > 0) {
            combo.increaseStock(increaseAmount);
        }
    }

    private Combo persist(Combo combo) {
        try {
            return comboRepository.update(combo);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException ex) {
            throw CommonExceptions.concurrencyConflict();
        }
    }
}
