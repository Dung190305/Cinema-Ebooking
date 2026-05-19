package com.cinemaebooking.backend.combo.infrastructure.persistence.repository;

import com.cinemaebooking.backend.combo.infrastructure.persistence.entity.ComboJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComboJpaRepository extends SoftDeleteJpaRepository<ComboJpaEntity> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ComboJpaEntity c where c.id = :id")
    Optional<ComboJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
