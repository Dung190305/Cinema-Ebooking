package com.cinemaebooking.backend.movie.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.movie.domain.enums.AgeRating;
import com.cinemaebooking.backend.movie.domain.enums.MovieStatus;
import com.cinemaebooking.backend.movie.infrastructure.persistence.entity.MovieJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MovieJpaRepository extends SoftDeleteJpaRepository<MovieJpaEntity> {

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MovieJpaEntity m WHERE m.title = :title AND m.deletedAt IS NULL")
    boolean existsByTitle(@Param("title") String title);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MovieJpaEntity m WHERE m.title = :title AND m.id != :id AND m.deletedAt IS NULL")
    boolean existsByTitleAndIdNot(@Param("title") String title, @Param("id") Long id);

    Optional<MovieJpaEntity> findByTitleIgnoreCase(@Param("title") String title);

    @Query("""
    SELECT m FROM MovieJpaEntity m
    WHERE m.deletedAt IS NULL
      AND (:status     IS NULL OR (
            CASE
              WHEN m.releaseDate > CURRENT_DATE THEN 'COMING_SOON'
              WHEN m.showingEndDate IS NULL OR m.showingEndDate >= CURRENT_DATE THEN 'NOW_SHOWING'
              ELSE 'ENDED'
            END
          ) = :#{#status?.name()})
      AND (:ageRating  IS NULL OR m.ageRating = :ageRating)
    """)
    Page<MovieJpaEntity> findByFilters(
            @Param("status")    MovieStatus status,
            @Param("ageRating") AgeRating ageRating,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT m FROM MovieJpaEntity m
        LEFT JOIN FETCH m.genres
        WHERE m.id IN :ids
          AND m.deletedAt IS NULL
    """)
    List<MovieJpaEntity> findAllByIdInWithGenres(@Param("ids") List<Long> ids);

}