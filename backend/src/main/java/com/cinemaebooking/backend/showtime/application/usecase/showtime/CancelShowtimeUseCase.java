// CancelShowtimeUsecase.java
package com.cinemaebooking.backend.showtime.application.usecase.showtime;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.ShowtimeExceptions;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeResponse;
import com.cinemaebooking.backend.showtime.application.mapper.ShowtimeResponseMapper;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import com.cinemaebooking.backend.showtime.domain.model.Showtime;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import com.cinemaebooking.backend.room.application.port.RoomRepository;
import com.cinemaebooking.backend.room.domain.valueObject.RoomId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelShowtimeUseCase {

    private final ShowtimeRepository showtimeRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeResponseMapper mapper;

    @Transactional
    public ShowtimeResponse execute(ShowtimeId id) {
        if (id == null) {
            throw CommonExceptions.invalidInput("Showtime id must not be null");
        }

        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> ShowtimeExceptions.notFound(id));

        showtime.cancel();
        Showtime saved = showtimeRepository.update(showtime);

        Long cinemaId = roomRepository.getCinemaIdByRoomId(RoomId.of(saved.getRoomId()));
        return mapper.toResponse(saved, cinemaId);
    }
}