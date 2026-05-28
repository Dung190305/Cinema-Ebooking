package com.cinemaebooking.backend.showtime.application.usecase.showtime;

import com.cinemaebooking.backend.room.application.port.RoomRepository;
import com.cinemaebooking.backend.room.domain.valueObject.RoomId;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeResponse;
import com.cinemaebooking.backend.showtime.application.mapper.ShowtimeResponseMapper;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import com.cinemaebooking.backend.showtime.domain.model.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
@Service
@RequiredArgsConstructor
public class GetShowtimeUseCase {

    private final ShowtimeRepository repository;
    private final RoomRepository roomRepository;
    private final ShowtimeResponseMapper mapper;

    public Page<ShowtimeResponse> execute(
            Long cinemaId,
            Long movieId,
            Long roomId,
            String statusStr,
            LocalDate date,
            String city,
            Pageable pageable
    ) {
        ShowtimeStatus status = null;
        if (statusStr != null) {
            status = ShowtimeStatus.valueOf(statusStr);
        }

        Page<Showtime> page = repository.search(
                cinemaId,
                movieId,
                roomId,
                status,
                date,
                city,
                pageable
        );

        return page.map(showtime -> {
            // Sửa: dùng roomId của chính showtime
            Long cId = roomRepository.getCinemaIdByRoomId(RoomId.of(showtime.getRoomId()));
            return mapper.toResponse(showtime, cId);
        });
    }
}
