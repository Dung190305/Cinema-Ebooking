package com.cinemaebooking.backend.dashboard.infrastructure.persistence.repository;

import com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardCinemaJpaRepository extends SoftDeleteJpaRepository<CinemaJpaEntity> {
    List<CinemaJpaEntity> findByDeletedFalse();
    List<CinemaJpaEntity> findByIdAndDeletedFalse(Long id);
}
