package com.cinemaebooking.backend.combo.application.validator;

import com.cinemaebooking.backend.combo.application.dto.CreateComboRequest;
import com.cinemaebooking.backend.combo.application.dto.UpdateComboRequest;
import com.cinemaebooking.backend.combo.application.port.ComboRepository;
import com.cinemaebooking.backend.combo.domain.model.Combo;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.domain.ComboExceptions;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.validation.engine.ValidationEngine;
import com.cinemaebooking.backend.common.validation.factory.ValidationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ComboCommandValidator {

    private final ComboRepository comboRepository;

    // ================== CREATE ==================
    public void validateCreateRequest(CreateComboRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Request must not be null");
        }

        validateFields(request);
        validateOriginalPrice(request.getPrice(), request.getOriginalPrice());
        validateUniqueName(request.getName(), null);
    }

    // ================== UPDATE ==================
    public void validateUpdateRequest(ComboId id, UpdateComboRequest request) {
        if (id == null || request == null) {
            throw CommonExceptions.invalidInput("Combo id and request must not be null");
        }

        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> ComboExceptions.notFound(id));
        validateFields(request);
        validateOriginalPrice(request.getPrice(), combo.getOriginalPrice());
        validateStockForUpdate(request.getStock(), combo.getStock());
        validateUniqueName(request.getName(), id);
    }

    // ================== FIELD VALIDATION ==================
    private void validateFields(Object request) {
        var profile = ValidationFactory.combo();
        ValidationEngine engine = ValidationEngine.of();

        if (request instanceof CreateComboRequest req) {
            engine
                    .validate(req.getName(), "name", profile.nameRules())
                    .validate(req.getDescription(), "description", profile.descriptionRules())
                    .validate(req.getPrice(), "price", profile.priceRules())
                    .validate(req.getOriginalPrice(), "originalPrice", profile.originalPriceRules())
                    .validate(req.getStock(), "stock", profile.stockRules())
                    .validate(req.getImageUrl(), "imageUrl", profile.imageUrlRules());
        }

        if (request instanceof UpdateComboRequest req) {
            engine
                    .validate(req.getName(), "name", profile.nameRules())
                    .validate(req.getDescription(), "description", profile.descriptionRules())
                    .validate(req.getPrice(), "price", profile.priceRules())
                    .validate(req.getStock(), "stock", profile.optionalStockRules())
                    .validate(req.getImageUrl(), "imageUrl", profile.imageUrlRules())
                    .validate(req.getStatus(), "status", profile.statusRules());
        }

        engine.throwIfInvalid();
    }

    // ================== BUSINESS VALIDATION ==================
    private void validateOriginalPrice(BigDecimal price, BigDecimal originalPrice) {
        if (price != null
                && originalPrice != null
                && originalPrice.compareTo(price) < 0) {
            throw CommonExceptions.invalidInput(
                    "originalPrice",
                    ErrorCategory.INVALID_VALUE,
                    "Giá gốc phải lớn hơn 0 và giá bán"
            );
        }
    }

    private void validateStockForUpdate(Integer newStock, Integer currentStock) {
        if (newStock == null) {
            return;
        }

        int current = currentStock == null ? 0 : currentStock;
        if (newStock < current) {
            throw CommonExceptions.invalidInput(
                    "stock",
                    ErrorCategory.INVALID_VALUE,
                    "Chỉ có thể tăng số lượng khi cập nhật combo"
            );
        }
    }

    private void validateUniqueName(String name, ComboId id) {
        String normalizedName = normalize(name);

        if (normalizedName == null) {
            return;
        }

        boolean exists = id == null
                ? comboRepository.existsByName(normalizedName)
                : comboRepository.existsByNameAndIdNot(normalizedName, id);

        if (exists) {
            throw ComboExceptions.alreadyExists(normalizedName);
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
