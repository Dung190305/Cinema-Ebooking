package com.cinemaebooking.backend.combo.infrastructure.persistence.entity;

import com.cinemaebooking.backend.combo.domain.enums.ComboStatus;
import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * ComboJpaEntity - Persistence model for combos table.
 * Responsibility:
 * - Map database table combos
 * - Handle persistence concerns only
 * - No business logic allowed
 * Note:
 * - This is NOT a domain model
 * - Must be converted via Mapper
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Entity
@Table(
        name = "combos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_combos_name_deleted",
                        columnNames = {"name", "deleted"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ComboJpaEntity extends BaseJpaEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Positive
    @Column(name = "original_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    @PositiveOrZero
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComboStatus status;

    @Override
    protected void beforeSoftDelete() {
        this.name = markDeleted(this.name);
    }
}