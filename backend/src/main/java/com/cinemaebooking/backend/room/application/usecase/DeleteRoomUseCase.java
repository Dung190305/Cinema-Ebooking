package com.cinemaebooking.backend.room.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.RoomExceptions;
import com.cinemaebooking.backend.room.application.port.RoomRepository;
import com.cinemaebooking.backend.room.domain.model.Room;
import com.cinemaebooking.backend.room.domain.valueObject.RoomId;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeleteRoomUseCase {

    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;

    public void execute(RoomId id) {

        // ================== INPUT VALIDATION ==================
        if (id == null) {
            throw CommonExceptions.invalidInput("Room id must not be null");
        }

        // ================== BUSINESS VALIDATION ==================
        Optional<Room> room = roomRepository.findById(id);
        if (room.isEmpty()) {
            throw RoomExceptions.notFound(id);
        }
        if (showtimeRepository.existsActiveByRoomId(id.getValue())) {
            throw RoomExceptions.deleteBlockedByShowtime(room.get().getName());
        }
        roomRepository.deleteById(id);
    }
}