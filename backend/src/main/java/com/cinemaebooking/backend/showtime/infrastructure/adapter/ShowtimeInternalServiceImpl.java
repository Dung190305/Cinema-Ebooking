package com.cinemaebooking.backend.showtime.infrastructure.adapter;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.ShowtimeExceptions;
import com.cinemaebooking.backend.common.exception.domain.ShowtimeSeatExceptions;
import com.cinemaebooking.backend.room_layout.application.port.roomLayout.RoomLayoutInternalService;
import com.cinemaebooking.backend.room_layout.application.port.seatType.SeatTypeRepository;
import com.cinemaebooking.backend.room_layout.domain.model.roomLayoutSeat.RoomLayoutSeat;
import com.cinemaebooking.backend.room_layout.domain.model.seatType.SeatType;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeSnapshot;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeInternalService;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import com.cinemaebooking.backend.showtime_seat.application.port.ShowtimeSeatRepository;
import com.cinemaebooking.backend.showtime_seat.domain.enums.ShowtimeSeatStatus;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeInternalServiceImpl implements ShowtimeInternalService {
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository seatRepository;
    private final RoomLayoutInternalService layoutService;
    private final SeatTypeRepository seatTypeRepository;

    @Override
    public ShowtimeSnapshot getSnapshot(Long showtimeId) {
        return showtimeRepository.findSnapshotById(showtimeId)
                .orElseThrow(() -> ShowtimeExceptions.notFound(ShowtimeId.of(showtimeId)));
    }

    @Override
    @Transactional // Quan trọng: Đảm bảo việc giữ ghế và lấy thông tin diễn ra trong 1 đơn vị công việc
    public List<Ticket> getTicketsBySeatIds(Long showtimeId, List<Long> seatIds) {
        // 1. Lấy danh sách ShowtimeSeat từ DB
        List<ShowtimeSeat> seats = seatRepository.findAllByIds(seatIds);

        // 2. Kiểm tra xem có tìm đủ số ghế yêu cầu không
        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Một số ghế không tồn tại trong hệ thống.");
        }

        // 3. Kiểm tra trạng thái: Ghế phải còn trống (AVAILABLE)
        validateSeatsAvailability(seats);
        validateCoupleSeatsInPairs(seats);

        // 5. Gom ID để truy vấn Bulk (Tránh N+1)
        List<Long> layoutIds = seats.stream()
                .map(ShowtimeSeat::getRoomLayoutSeatId)
                .distinct()
                .toList();

        Map<Long, RoomLayoutSeat> layoutMap = layoutService.getMapByIds(layoutIds);

        List<Long> seatTypeIds = layoutMap.values().stream()
                .map(RoomLayoutSeat::getSeatTypeId)
                .distinct()
                .toList();
        Map<Long, String> seatTypeNameMap = layoutService.getSeatTypeNameMap(seatTypeIds);

        // 6. Mapping sang Ticket Entity (Snapshot thông tin tại thời điểm mua)
        return seats.stream().map(seat -> {
            RoomLayoutSeat layout = layoutMap.get(seat.getRoomLayoutSeatId());

            String typeName = seatTypeNameMap.getOrDefault(layout.getSeatTypeId(), "STANDARD");

            return Ticket.builder()
                    .showtimeSeatId(seat.getId().getValue())
                    .seatType(typeName)
                    .seatName(seat.getSeatNumber())
                    .price(seat.getPrice()) // Lưu giá tại thời điểm này
                    .status(TicketStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .ticketCode(generateTicketCode())
                    .build();
        }).collect(Collectors.toList());
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

    private void validateSeatsAvailability(List<ShowtimeSeat> seats) {
        for (ShowtimeSeat seat : seats) {
            if (seat.getStatus() != ShowtimeSeatStatus.AVAILABLE) {
                throw ShowtimeSeatExceptions.unavailable(seat.getId());
            }
        }
    }

    private String generateTicketCode() {
        return "TIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
