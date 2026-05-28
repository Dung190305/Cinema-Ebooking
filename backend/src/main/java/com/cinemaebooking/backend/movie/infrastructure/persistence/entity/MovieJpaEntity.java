package com.cinemaebooking.backend.movie.infrastructure.persistence.entity;

import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.movie.domain.enums.AgeRating;
import com.cinemaebooking.backend.movie.domain.enums.MovieStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;

/**
 * MovieJpaEntity - Persistence model for movies table.
 * Responsibility:
 * - Map database table movies
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
        name = "movies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_movies_title_deleted",
                        columnNames = {"title", "deleted"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MovieJpaEntity extends BaseJpaEntity {

    @NotNull
    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer duration;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AgeRating ageRating;

    @NotNull
    @Column(nullable = false)
    private LocalDate releaseDate;

    @Column(name = "showing_end_date")
    private LocalDate showingEndDate;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(length = 255)
    private String director;

    @Column(length = 500)
    private String actors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<GenreJpaEntity> genres;

    @PositiveOrZero
    @Column(nullable = false)
    private Double rating;

    @PositiveOrZero
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    @Override
    protected void beforeSoftDelete() {
        this.title = markDeleted(this.title);
    }
}