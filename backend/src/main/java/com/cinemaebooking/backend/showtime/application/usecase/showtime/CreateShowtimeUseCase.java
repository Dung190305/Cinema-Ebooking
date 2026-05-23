package com.cinemaebooking.backend.showtime.application.usecase.showtime;

import com.cinemaebooking.backend.room.application.port.RoomRepository;
import com.cinemaebooking.backend.room.domain.valueObject.RoomId;
import com.cinemaebooking.backend.room_layout.application.port.roomLayout.RoomLayoutRepository;
import com.cinemaebooking.backend.room_layout.application.port.roomLayoutSeat.RoomLayoutSeatRepository;
import com.cinemaebooking.backend.room_layout.application.port.seatType.SeatTypeRepository;
import com.cinemaebooking.backend.room_layout.domain.model.roomLayout.RoomLayout;
import com.cinemaebooking.backend.room_layout.domain.model.roomLayoutSeat.RoomLayoutSeat;
import com.cinemaebooking.backend.room_layout.domain.valueObject.seatType.SeatTypeId;
import com.cinemaebooking.backend.showtime.application.dto.showtime.CreateShowtimeRequest;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeResponse;
import com.cinemaebooking.backend.showtime.application.mapper.ShowtimeResponseMapper;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeFormatRepository;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import com.cinemaebooking.backend.showtime.application.validator.ShowtimeCommandValidator;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import com.cinemaebooking.backend.showtime.domain.model.Showtime;
import com.cinemaebooking.backend.showtime.domain.model.ShowtimeFormat;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeFormatId;
import com.cinemaebooking.backend.showtime_seat.application.port.ShowtimeSeatRepository;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateShowtimeUseCase {

    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final RoomLayoutRepository roomLayoutRepository;
    private final RoomLayoutSeatRepository roomLayoutSeatRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final ShowtimeFormatRepository showtimeFormatRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeResponseMapper mapper;
    private final ShowtimeCommandValidator validator;

    @Transactional
    public ShowtimeResponse execute(CreateShowtimeRequest request) {
        validator.validateCreateRequest(request);
        Instant startInstant = request.getStartTime();
        Instant endInstant = request.getEndTime();

        LocalDate startDate = startInstant.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate();

        RoomLayout layout = roomLayoutRepository.findCurrentByRoomIdAndDate(request.getRoomId(), startDate)
                .orElseThrow(() -> new IllegalStateException("No active layout for room " + request.getRoomId() + " at " + startDate));
        int totalCols = layout.getTotalCols();

        Showtime showtime = Showtime.builder()
                .movieId(request.getMovieId())
                .roomId(request.getRoomId())
                .roomLayoutId(layout.getId().getValue())
                .formatId(request.getFormatId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .audioLanguage(request.getAudioLanguage())
                .subtitleLanguage(request.getSubtitleLanguage())
                .status(ShowtimeStatus.SCHEDULED)
                .build();
        showtime.validateForCreate();

        Showtime saved = showtimeRepository.create(showtime);
        if (!layout.isUsed()) roomLayoutRepository.markAsUsedAndSetLastUsedDate(layout, startDate);
        // Lấy tất cả ghế của layout
        List<RoomLayoutSeat> layoutSeats = roomLayoutSeatRepository.findByRoomLayoutId(layout.getId().getValue());

        ShowtimeFormat format = showtimeFormatRepository.findById(ShowtimeFormatId.of(showtime.getFormatId())).orElseThrow();
        BigDecimal formatSurcharge = format.getExtraPrice();
        List<ShowtimeSeat> showtimeSeats = layoutSeats.stream()
                .map(seat -> {
                    Long seatTypeId = seat.getSeatTypeId();
                    BigDecimal basePrice = seatTypeRepository.findBasePriceById(SeatTypeId.of(seatTypeId)).orElseThrow();
                    BigDecimal finalPrice = basePrice.add(formatSurcharge);
                    return ShowtimeSeat.from(seat, saved.getId().getValue(), finalPrice, totalCols);
                })
                .toList();

        showtimeSeatRepository.saveAll(showtimeSeats);

        Long cinemaId = roomRepository.getCinemaIdByRoomId(RoomId.of(request.getRoomId()));
        return mapper.toResponse(saved, cinemaId);
    }
}