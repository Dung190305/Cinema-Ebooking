package com.cinemaebooking.backend.showtime_seat.application.dto;

import com.cinemaebooking.backend.showtime_seat.domain.enums.ShowtimeSeatStatus;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ShowtimeSeatResponse {
    private Long id;
    private Long roomLayoutSeatId;
    private String seatNumber;
    private Integer rowIndex;
    private Integer colIndex;
    private Long seatTypeId;
    private boolean isActive;
    private ShowtimeSeatStatus status;
    private BigDecimal price;
    private Long coupleGroupId;
}