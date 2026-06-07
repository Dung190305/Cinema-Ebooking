package com.cinemaebooking.backend.showtime.infrastructure.persistence.entity;

import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.movie.infrastructure.persistence.entity.MovieJpaEntity;
import com.cinemaebooking.backend.room.infrastructure.persistence.entity.RoomJpaEntity;
import com.cinemaebooking.backend.showtime.domain.enums.Language;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * ShowtimeJpaEntity - Persistence model for showtimes table.
 * Responsibility:
 * - Map database table showtimes
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
        name = "showtimes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_showtimes_room_id_start_time_deleted",
                        columnNames = {"room_id", "start_time", "deleted"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class ShowtimeJpaEntity extends BaseJpaEntity {

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Language audioLanguage;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Language subtitleLanguage;

    @Column(nullable = false)
    private boolean cancelled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieJpaEntity movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomJpaEntity room;

    @Column(name = "room_layout_id", nullable = false)
    private Long roomLayoutId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "format_id", nullable = false)
    private ShowtimeFormatJpaEntity format;

    @Override
    protected void beforeSoftDelete() {
        this.startTime = this.startTime.plusSeconds(this.getId());
    }
}