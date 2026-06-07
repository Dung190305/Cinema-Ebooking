package com.cinemaebooking.backend.showtime.application.port;

import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeSnapshot;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import com.cinemaebooking.backend.showtime.domain.model.Showtime;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository {

    Showtime create(Showtime showtime);

    Showtime update(Showtime showtime);

    Optional<Showtime> findById(ShowtimeId id);

    void deleteById(ShowtimeId id);

    boolean existsById(ShowtimeId id);

    boolean existsByRoomLayoutId(Long roomLayoutId);

    boolean existsActiveByRoomId(Long roomId);

    Page<Showtime> search(
            Long cinemaId,
            Long movieId,
            Long roomId,
            ShowtimeStatus status,
            LocalDate date,
            String city,
            Pageable pageable
    );

    boolean existsRoomConflict(
            Long roomId,
            Instant startTime,
            Instant endTime,
            ShowtimeId excludeId
    );

    Optional<ShowtimeSnapshot> findSnapshotById(Long showtimeId);
}