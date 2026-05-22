package com.cinemaebooking.backend.showtime.infrastructure.adapter;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.movie.infrastructure.persistence.repository.MovieJpaRepository;
import com.cinemaebooking.backend.room.infrastructure.persistence.repository.RoomJpaRepository;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeSnapshot;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import com.cinemaebooking.backend.showtime.domain.enums.Language;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import com.cinemaebooking.backend.showtime.domain.model.Showtime;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import com.cinemaebooking.backend.showtime.infrastructure.mappers.ShowtimeMapper;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.repository.ShowtimeFormatJpaRepository;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.repository.ShowtimeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShowtimeRepositoryImpl implements ShowtimeRepository {

    private final ShowtimeJpaRepository jpaRepository;
    private final ShowtimeMapper mapper;
    private final MovieJpaRepository movieRepository;
    private final RoomJpaRepository roomRepository;
    private final ShowtimeFormatJpaRepository formatRepository;

    @Override
    public Showtime create(Showtime showtime) {

        var entity = mapper.toEntity(showtime);
        entity.setMovie(
                movieRepository.getReferenceById(showtime.getMovieId())
        );

        entity.setRoom(
                roomRepository.getReferenceById(showtime.getRoomId())
        );

        entity.setFormat(
                formatRepository.getReferenceById(showtime.getFormatId())
        );

        var saved = jpaRepository.save(entity);

        return mapper.toDomain(saved);
    }


    @Override
    public Showtime update(Showtime showtime) {

        if (showtime.getId() == null) {
            throw CommonExceptions.invalidInput("Showtime id must not be null.");
        }

        var entity = jpaRepository.findByIdOrThrow(showtime.getId().getValue());

        entity.setStartTime(showtime.getStartTime());
        entity.setEndTime(showtime.getEndTime());
        entity.setAudioLanguage(showtime.getAudioLanguage());
        entity.setSubtitleLanguage(showtime.getSubtitleLanguage());
        entity.setStatus(showtime.getStatus());

        var saved = jpaRepository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Showtime> findById(ShowtimeId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(ShowtimeId id) {
        ShowtimeJpaEntity showtime = jpaRepository.findByIdOrThrow(id.getValue());
        jpaRepository.delete(showtime);
    }

    @Override
    public boolean existsById(ShowtimeId id) {
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByRoomIdAndStatusIn(Long roomId, List<ShowtimeStatus> status) {
        return jpaRepository.existsByRoomIdAndStatusIn(roomId, status);
    }

    @Override
    public Page<Showtime> search(Long cinemaId, Long movieId, Long roomId,
                                 ShowtimeStatus status, LocalDate date, String city, Pageable pageable) {
        return jpaRepository.search(cinemaId, movieId, roomId, status, date, city, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsRoomConflict(Long roomId,
                                      LocalDateTime startTime,
                                      LocalDateTime endTime,
                                      ShowtimeId excludeId) {

        Long exclude = (excludeId != null) ? excludeId.getValue() : null;

        return jpaRepository.existsRoomConflict(
                roomId,
                startTime,
                endTime,
                exclude
        );
    }

    @Override
    public Optional<ShowtimeSnapshot> findSnapshotById(Long showtimeId) {
        return jpaRepository.findSnapshot(showtimeId);
    }

    @Override
    public boolean existsByRoomLayoutId(Long roomLayoutId){
        return jpaRepository.existsByRoomLayoutId(roomLayoutId);
    }
}