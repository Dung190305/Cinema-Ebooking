package com.cinemaebooking.backend.showtime.application.port;

import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeSnapshot;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;

import java.util.List;

public interface ShowtimeInternalService {
    ShowtimeSnapshot getSnapshot(Long showtimeId);
    List<ShowtimeSeat> validateAndLockSeats(Long showtimeId, List<Long> seatIds, Long userId);
}
