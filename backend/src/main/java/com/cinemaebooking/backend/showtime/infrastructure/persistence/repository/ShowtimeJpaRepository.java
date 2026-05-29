package com.cinemaebooking.backend.showtime.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeSnapshot;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeJpaRepository extends SoftDeleteJpaRepository<ShowtimeJpaEntity> {

    @Query("""
        SELECT s FROM ShowtimeJpaEntity s
        WHERE s.deleted = false
          AND (:cinemaId IS NULL OR s.room.cinema.id = :cinemaId)
          AND (:movieId IS NULL OR s.movie.id = :movieId)
          AND (:roomId IS NULL OR s.room.id = :roomId)
          AND (:status IS NULL OR s.status = :status)
          AND (:date IS NULL OR DATE(s.startTime) = :date)
          AND (:city IS NULL OR s.room.cinema.city = :city)
    """)
    Page<ShowtimeJpaEntity> search(
            @Param("cinemaId") Long cinemaId,
            @Param("movieId") Long movieId,
            @Param("roomId") Long roomId,
            @Param("status") ShowtimeStatus status,
            @Param("date") LocalDate date,
            @Param("city") String city,      // ← thêm
            Pageable pageable
    );

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM ShowtimeJpaEntity s
        WHERE s.deleted = false
          AND s.room.id = :roomId
          AND (:excludeId IS NULL OR s.id <> :excludeId)
          AND s.startTime < :endTime
          AND s.endTime > :startTime
    """)
    boolean existsRoomConflict(
            Long roomId,
            Instant startTime,
            Instant endTime,
            Long excludeId
    );

    boolean existsByRoomIdAndStatusIn(Long roomId, List<ShowtimeStatus> status);

    @Query("""
        SELECT new com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeSnapshot(
            m.id,
            m.title, 
            c.name, 
            r.name, 
            s.startTime
        )
        FROM ShowtimeJpaEntity s
        JOIN s.movie m
        JOIN s.room r
        JOIN r.cinema c
        WHERE s.id = :showtimeId
    """)
    Optional<ShowtimeSnapshot> findSnapshot(@Param("showtimeId") Long showtimeId);

    boolean existsByRoomLayoutId(Long roomLayoutId);
}