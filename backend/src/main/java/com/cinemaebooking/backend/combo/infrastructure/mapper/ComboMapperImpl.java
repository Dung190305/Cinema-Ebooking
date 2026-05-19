package com.cinemaebooking.backend.combo.infrastructure.mapper;

import com.cinemaebooking.backend.combo.domain.model.Combo;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import com.cinemaebooking.backend.combo.infrastructure.persistence.entity.ComboJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ComboMapperImpl implements ComboMapper {

    @Override
    public ComboJpaEntity toEntity(Combo domain) {
        if (domain == null) return null;

        return ComboJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .originalPrice(domain.getOriginalPrice())
                .stock(domain.getStock())
                .imageUrl(domain.getImageUrl())
                .status(domain.getStatus())
                .build();
    }

    @Override
    public Combo toDomain(ComboJpaEntity entity) {
        if (entity == null) return null;

        return Combo.builder()
                .id(ComboId.ofNullable(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .originalPrice(entity.getOriginalPrice())
                .stock(entity.getStock())
                .imageUrl(entity.getImageUrl())
                .status(entity.getStatus())
                .build();
    }

    @Override
    public void updateEntity(ComboJpaEntity entity, Combo domain) {
        if (entity == null || domain == null) return;

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPrice(domain.getPrice());
        entity.setOriginalPrice(domain.getOriginalPrice());
        entity.setStock(domain.getStock());
        entity.setImageUrl(domain.getImageUrl());
        entity.setStatus(domain.getStatus());
    }
}
