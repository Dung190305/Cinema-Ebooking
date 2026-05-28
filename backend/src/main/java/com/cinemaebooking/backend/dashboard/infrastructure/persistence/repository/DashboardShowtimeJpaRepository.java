package com.cinemaebooking.backend.dashboard.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DashboardShowtimeJpaRepository extends SoftDeleteJpaRepository<ShowtimeJpaEntity> {

    @Query("""
        SELECT s FROM ShowtimeJpaEntity s
        JOIN FETCH s.movie m
        JOIN FETCH s.room r
        JOIN FETCH r.cinema c
        WHERE s.deleted = false
          AND s.startTime > :now
          AND s.startTime <= :cutoff
          AND (:cinemaId IS NULL OR c.id = :cinemaId)
        ORDER BY s.startTime ASC
        """)
    List<ShowtimeJpaEntity> findUpcomingShowtimes(
            @Param("now") Instant now,
            @Param("cutoff") Instant cutoff,
            @Param("cinemaId") Long cinemaId
    );
}
