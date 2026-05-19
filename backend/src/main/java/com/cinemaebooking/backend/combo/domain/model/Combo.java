package com.cinemaebooking.backend.combo.domain.model;

import com.cinemaebooking.backend.combo.domain.enums.ComboDisplayStatus;
import com.cinemaebooking.backend.combo.domain.enums.ComboStatus;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.domain.ComboExceptions;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class Combo extends BaseEntity<ComboId> {

    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String imageUrl;
    private ComboStatus status;

    // ================== BUSINESS METHODS ==================

    public void update(String name,
                       String description,
                       BigDecimal price,
                       String imageUrl,
                       ComboStatus newStatus) {
        validateName(name);
        validatePrice(price);
        validateStatus(newStatus);
        validateOptionalFields(this.originalPrice, price);

        this.name = name.trim();
        this.description = normalize(description);
        this.price = price;
        this.imageUrl = normalize(imageUrl);

        // Chỉ auto-sync khi stock = 0 và người dùng KHÔNG chủ động set ACTIVE
        if (currentStock() <= 0 && newStatus != ComboStatus.ACTIVE) {
            this.status = ComboStatus.INACTIVE;
        } else {
            this.status = newStatus;
        }
    }

    public void increaseStock(Integer quantity) {
        validatePositiveQuantity(quantity, "stock");

        this.stock = currentStock() + quantity;
        if (this.status == null) {
            this.status = ComboStatus.INACTIVE;
        }
    }

    public void sell(Integer quantity) {
        validatePositiveQuantity(quantity, "quantity");

        if (this.status != ComboStatus.ACTIVE) {
            throw CommonExceptions.invalidInput(
                    "status",
                    ErrorCategory.INVALID_VALUE,
                    "Combo is not active"
            );
        }

        if (currentStock() < quantity) {
            throw ComboExceptions.outOfStock();
        }

        this.stock = currentStock() - quantity;
        syncStatusWithStock();
    }

    public void changeStatus(ComboStatus status) {
        validateStatus(status);

        if (status == ComboStatus.ACTIVE && currentStock() <= 0) {
            throw ComboExceptions.outOfStock();
        }

        this.status = status;
    }

    public ComboDisplayStatus getDisplayStatus() {
        if (currentStock() <= 0) {
            return ComboDisplayStatus.OUT_OF_STOCK;
        }

        return switch (this.status) {
            case ACTIVE -> ComboDisplayStatus.ACTIVE;
            case INACTIVE -> ComboDisplayStatus.INACTIVE;
        };
    }

    // ================== VALIDATION ==================

    public void validateForCreate() {
        validateName(this.name);
        validatePrice(this.price);
        validateStock(this.stock);
        validateStatus(this.status != null ? this.status : ComboStatus.ACTIVE);
        validateOptionalFields(this.originalPrice, this.price);
        syncStatusWithStock();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw CommonExceptions.invalidInput("Combo name must not be empty");
        }
        if (name.trim().length() < 3 || name.trim().length() > 150) {
            throw CommonExceptions.invalidInput("Combo name length must be between 3 and 150");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw CommonExceptions.invalidInput("Combo price must not be null");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw CommonExceptions.invalidInput("Combo price must be greater than 0");
        }
    }

    private void validateStock(Integer stock) {
        if (stock == null) {
            throw CommonExceptions.invalidInput("Combo stock must not be null");
        }
        if (stock < 0) {
            throw CommonExceptions.invalidInput("Combo stock must not be negative");
        }
    }

    private void validatePositiveQuantity(Integer quantity, String field) {
        if (quantity == null || quantity <= 0) {
            throw CommonExceptions.invalidInput(
                    field,
                    ErrorCategory.INVALID_VALUE,
                    field + " must be greater than 0"
            );
        }
    }

    private void validateStatus(ComboStatus status) {
        if (status == null) {
            throw CommonExceptions.invalidInput("Combo status must not be null");
        }
    }

    private void validateOptionalFields(BigDecimal originalPrice, BigDecimal price) {
        if (originalPrice == null) {
            throw CommonExceptions.invalidInput("Original price must not be null");
        }
        if (originalPrice != null && price != null && originalPrice.compareTo(price) < 0) {
            throw CommonExceptions.invalidInput("Original price cannot be less than current price");
        }
    }

    private int currentStock() {
        return this.stock == null ? 0 : this.stock;
    }

    private void syncStatusWithStock() {
        if (currentStock() <= 0 && this.status == ComboStatus.ACTIVE) {
            this.status = ComboStatus.INACTIVE;
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
