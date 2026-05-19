package com.cinemaebooking.backend.combo.application.port;

import com.cinemaebooking.backend.combo.domain.model.Combo;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ComboRepository {

    Combo create(Combo combo);

    Combo update(Combo combo);

    Optional<Combo> findById(ComboId id);

    Optional<Combo> findByIdForUpdate(ComboId id);

    Combo reserveStock(ComboId id, Integer quantity);

    Page<Combo> findAll(Pageable pageable);

    void deleteById(ComboId id);

    boolean existsById(ComboId id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, ComboId id);
}
