    package com.cinemaebooking.backend.showtime_seat.domain.model;

    import com.cinemaebooking.backend.common.domain.BaseEntity;
    import com.cinemaebooking.backend.common.exception.ErrorCategory;
    import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
    import com.cinemaebooking.backend.common.exception.domain.ShowtimeSeatExceptions;
    import com.cinemaebooking.backend.room_layout.domain.model.roomLayoutSeat.RoomLayoutSeat;
    import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
    import com.cinemaebooking.backend.showtime_seat.domain.enums.ShowtimeSeatStatus;
    import com.cinemaebooking.backend.showtime_seat.domain.valueobject.ShowtimeSeatId;
    import lombok.Getter;
    import lombok.experimental.SuperBuilder;

    import java.math.BigDecimal;

    /**
     * Domain Model: ShowtimeSeat
     * - Đại diện 1 mapping giữa Showtime và Seat
     * - Không có CRUD public
     * - Không lưu trạng thái ghế (status nằm ở SeatLock / Ticket)
     * - Được auto-generate khi tạo showtime
     * - Bị hard delete khi xoá showtime
     * Domain chỉ kiểm tra dữ liệu thô, không truy database.
     */
    @Getter
    @SuperBuilder(toBuilder = true)
    public class ShowtimeSeat extends BaseEntity<ShowtimeSeatId> {

        private final Long showtimeId;
        private final Long roomLayoutSeatId;
        private String seatNumber;
        private Integer rowIndex;
        private Integer colIndex;
        private Long seatTypeId;
        private boolean active;
        private ShowtimeSeatStatus status;
        private final BigDecimal price;
        private Long coupleGroupId;


        public static ShowtimeSeat from(RoomLayoutSeat seat, Long showtimeId, BigDecimal price, int totalCols) {
            String seatNumber = buildSeatNumber(seat.getRowIndex(), seat.getColIndex(), totalCols);
            return ShowtimeSeat.builder()
                    .roomLayoutSeatId(seat.getId().getValue())
                    .showtimeId(showtimeId)
                    .seatNumber(seatNumber)
                    .rowIndex(seat.getRowIndex())
                    .colIndex(seat.getColIndex())
                    .seatTypeId(seat.getSeatTypeId())
                    .coupleGroupId(seat.getCoupleGroupId())
                    .active(seat.isActive())
                    .price(price)
                    .status(ShowtimeSeatStatus.AVAILABLE)
                    .build();
        }

        private static String buildSeatNumber(int rowIndex, int colIndex, int totalCols) {
            char rowLetter = (char) ('A' + rowIndex - 1);
            int displayedCol = totalCols - colIndex + 1;
            return String.format("%s%d", rowLetter, displayedCol);
        }

        public void book() {
            if (this.status != ShowtimeSeatStatus.LOCKED) {
                throw ShowtimeSeatExceptions.unavailable(this.id);
            }

            this.status = ShowtimeSeatStatus.BOOKED;
        }

        public void release() {
            this.status = ShowtimeSeatStatus.AVAILABLE;
        }
    }
