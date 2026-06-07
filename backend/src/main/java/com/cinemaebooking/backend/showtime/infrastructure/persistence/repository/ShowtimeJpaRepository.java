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

    @Query(value = """
    SELECT s.id, s.start_time, s.end_time, s.audio_language, s.subtitle_language,
           s.cancelled, s.room_id, s.movie_id, s.format_id, s.room_layout_id,
           s.deleted, s.deleted_at, s.created_at, s.updated_at, s.version
    FROM showtimes s
    JOIN rooms r ON s.room_id = r.id AND r.deleted_at IS NULL
    JOIN cinemas c ON r.cinema_id = c.id AND c.deleted_at IS NULL
    JOIN movies m ON s.movie_id = m.id AND m.deleted_at IS NULL
    WHERE s.deleted_at IS NULL
      AND (:cinemaId IS NULL OR r.cinema_id = :cinemaId)
      AND (:movieId  IS NULL OR s.movie_id  = :movieId)
      AND (:roomId   IS NULL OR s.room_id   = :roomId)
      AND (:date     IS NULL OR DATE(s.start_time) = :date)
      AND (:city     IS NULL OR c.city = :city)
      AND (:status   IS NULL OR (
            CASE
              WHEN s.cancelled = true                         THEN 'CANCELLED'
              WHEN NOW() < s.start_time                      THEN 'SCHEDULED'
              WHEN NOW() BETWEEN s.start_time AND s.end_time THEN 'ONGOING'
              ELSE                                               'FINISHED'
            END
          ) = :status
      )
    ORDER BY s.start_time DESC
    """,
            countQuery = """
    SELECT COUNT(*) FROM showtimes s
    JOIN rooms r ON s.room_id = r.id AND r.deleted_at IS NULL
    JOIN cinemas c ON r.cinema_id = c.id AND c.deleted_at IS NULL
    JOIN movies m ON s.movie_id = m.id AND m.deleted_at IS NULL
    WHERE s.deleted_at IS NULL
      AND (:cinemaId IS NULL OR r.cinema_id = :cinemaId)
      AND (:movieId  IS NULL OR s.movie_id  = :movieId)
      AND (:roomId   IS NULL OR s.room_id   = :roomId)
      AND (:date     IS NULL OR DATE(s.start_time) = :date)
      AND (:city     IS NULL OR c.city = :city)
      AND (:status   IS NULL OR (
            CASE
              WHEN s.cancelled = true                         THEN 'CANCELLED'
              WHEN NOW() < s.start_time                      THEN 'SCHEDULED'
              WHEN NOW() BETWEEN s.start_time AND s.end_time THEN 'ONGOING'
              ELSE                                               'FINISHED'
            END
          ) = :status
      )
    """,
            nativeQuery = true)
    Page<ShowtimeJpaEntity> search(
            @Param("cinemaId") Long cinemaId,
            @Param("movieId")  Long movieId,
            @Param("roomId")   Long roomId,
            @Param("status")   String status,
            @Param("date")     LocalDate date,
            @Param("city")     String city,
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

    @Query("""
    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    FROM ShowtimeJpaEntity s
    WHERE s.deleted = false
      AND s.cancelled = false
      AND s.room.id = :roomId
      AND CURRENT_TIMESTAMP < s.endTime
""")
    boolean existsActiveByRoomId(@Param("roomId") Long roomId);

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