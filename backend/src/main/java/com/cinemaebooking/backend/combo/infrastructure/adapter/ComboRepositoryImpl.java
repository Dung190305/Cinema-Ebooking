package com.cinemaebooking.backend.combo.infrastructure.adapter;

import com.cinemaebooking.backend.combo.application.port.ComboRepository;
import com.cinemaebooking.backend.combo.domain.model.Combo;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import com.cinemaebooking.backend.combo.infrastructure.mapper.ComboMapper;
import com.cinemaebooking.backend.combo.infrastructure.persistence.entity.ComboJpaEntity;
import com.cinemaebooking.backend.combo.infrastructure.persistence.repository.ComboJpaRepository;
import com.cinemaebooking.backend.common.exception.domain.ComboExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ComboRepositoryImpl implements ComboRepository {

    private final ComboJpaRepository jpaRepository;
    private final ComboMapper mapper;

    @Override
    public Combo create(Combo combo) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(combo)));
    }

    @Override
    public Combo update(Combo combo) {
        ComboJpaEntity entity = findEntityByIdForUpdate(combo.getId().getValue());
        mapper.updateEntity(entity, combo);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Combo> findById(ComboId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<Combo> findByIdForUpdate(ComboId id) {
        return jpaRepository.findByIdForUpdate(id.getValue()).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Combo reserveStock(ComboId id, Integer quantity) {
        ComboJpaEntity entity = findEntityByIdForUpdate(id.getValue());
        Combo combo = mapper.toDomain(entity);

        combo.sell(quantity);
        mapper.updateEntity(entity, combo);

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Page<Combo> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(ComboId id) {
        ComboJpaEntity entity = findEntityById(id.getValue());
        jpaRepository.delete(entity); // soft delete
    }

    @Override
    public boolean existsById(ComboId id) {
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, ComboId id) {
        return jpaRepository.existsByNameAndIdNot(name, id.getValue());
    }

    private ComboJpaEntity findEntityById(Long id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> ComboExceptions.notFound(ComboId.of(id)));
    }

    private ComboJpaEntity findEntityByIdForUpdate(Long id) {
        return jpaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> ComboExceptions.notFound(ComboId.of(id)));
    }
}
