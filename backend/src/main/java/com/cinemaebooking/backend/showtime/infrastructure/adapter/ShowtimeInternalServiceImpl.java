package com.cinemaebooking.backend.showtime.infrastructure.adapter;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.ShowtimeExceptions;
import com.cinemaebooking.backend.common.exception.domain.ShowtimeSeatExceptions;
import com.cinemaebooking.backend.room_layout.application.port.roomLayout.RoomLayoutInternalService;
import com.cinemaebooking.backend.room_layout.application.port.seatType.SeatTypeRepository;
import com.cinemaebooking.backend.room_layout.domain.model.seatType.SeatType;
import com.cinemaebooking.backend.seat_lock.application.port.SeatLockService;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeSnapshot;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeInternalService;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import com.cinemaebooking.backend.showtime_seat.application.port.ShowtimeSeatRepository;
import com.cinemaebooking.backend.showtime_seat.domain.enums.ShowtimeSeatStatus;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ShowtimeInternalServiceImpl implements ShowtimeInternalService {
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository seatRepository;
    private final RoomLayoutInternalService layoutService;
    private final SeatTypeRepository seatTypeRepository;
    private final SeatLockService seatLockService;

    @Override
    public ShowtimeSnapshot getSnapshot(Long showtimeId) {
        return showtimeRepository.findSnapshotById(showtimeId)
                .orElseThrow(() -> ShowtimeExceptions.notFound(ShowtimeId.of(showtimeId)));
    }

    @Override
    @Transactional
    public List<ShowtimeSeat> validateAndLockSeats(Long showtimeId, List<Long> seatIds, Long userId) {
        // 1. Lấy danh sách ShowtimeSeat từ DB
        List<ShowtimeSeat> seats = seatRepository.findAllByIds(seatIds);

        // 2. Kiểm tra tìm đủ số ghế
        if (seats.size() != seatIds.size()) {
            throw CommonExceptions.resourceNotFound("Một số ghế không tồn tại trong hệ thống.");
        }

        // 3. Kiểm tra ghế thuộc đúng showtime (Giữ logic kiểm tra an toàn từ HEAD)
        for (ShowtimeSeat seat : seats) {
            if (!seat.getShowtimeId().equals(showtimeId)) {
                throw ShowtimeSeatExceptions.unavailable(seat.getId());
            }
        }

        // 4. Validate trạng thái trống/lock và ghế đôi (Theo logic mới của develop)
        validateSeatsAvailability(seats, userId);
        validateCoupleSeatsInPairs(seats);

        // Chỉ validate + lock, không tạo Ticket tại đây
        return seats;
    }

    private void validateCoupleSeatsInPairs(List<ShowtimeSeat> seats) {
        // Lấy coupleTypeId một lần từ DB — tránh hardcode magic number
        SeatType seatType = seatTypeRepository.findByNameIgnoreCase("COUPLE")
                .orElse(null);
        if (seatType == null) throw CommonExceptions.resourceNotFound("Seat type not found");
        Long coupleTypeId = seatType.getId().getValue();
        if (coupleTypeId == null) return; // Không có loại ghế đôi → skip

        Map<Long, List<ShowtimeSeat>> coupleGroups = seats.stream()
                .filter(s -> coupleTypeId.equals(s.getSeatTypeId())
                        && s.getCoupleGroupId() != null)
                .collect(Collectors.groupingBy(ShowtimeSeat::getCoupleGroupId));

        for (var entry : coupleGroups.entrySet()) {
            if (entry.getValue().size() != 2) {
                throw CommonExceptions.invalidInput(
                        "Ghế đôi phải được đặt theo cặp. Vui lòng chọn cả hai ghế trong cùng một cặp."
                );
            }
        }
    }

    private void validateSeatsAvailability(List<ShowtimeSeat> seats, Long currentUserId) {
        for (ShowtimeSeat seat : seats) {
            ShowtimeSeatStatus status = seat.getStatus();
            if (status == ShowtimeSeatStatus.AVAILABLE) {
                continue;
            }

            // Trường hợp 2: ghế đang LOCKED
            if (status == ShowtimeSeatStatus.LOCKED) {
                // Kiểm tra xem có đúng user này lock không
                if (seatLockService.isLockedByUser(seat.getId().getValue(), currentUserId)) {
                    continue; // cho phép
                } else {
                    throw ShowtimeSeatExceptions.unavailable(seat.getId());
                }
            }
            throw ShowtimeSeatExceptions.unavailable(seat.getId());
        }
    }
}