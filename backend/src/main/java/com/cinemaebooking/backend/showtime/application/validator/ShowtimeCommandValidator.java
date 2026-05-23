package com.cinemaebooking.backend.showtime.application.validator;

import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.RoomExceptions;
import com.cinemaebooking.backend.common.exception.domain.ShowtimeExceptions;
import com.cinemaebooking.backend.common.validation.engine.ValidationEngine;
import com.cinemaebooking.backend.common.validation.factory.ValidationFactory;
import com.cinemaebooking.backend.movie.application.port.MovieRepository;
import com.cinemaebooking.backend.movie.domain.model.Movie;
import com.cinemaebooking.backend.movie.domain.valueobject.MovieId;
import com.cinemaebooking.backend.room.application.port.RoomRepository;
import com.cinemaebooking.backend.room.domain.enums.RoomType;
import com.cinemaebooking.backend.room.domain.model.Room;
import com.cinemaebooking.backend.room.domain.valueObject.RoomId;
import com.cinemaebooking.backend.showtime.application.dto.showtime.CreateShowtimeRequest;
import com.cinemaebooking.backend.showtime.application.dto.showtime.UpdateShowtimeRequest;
import com.cinemaebooking.backend.showtime.application.port.ShowtimeRepository;
import com.cinemaebooking.backend.showtime.domain.model.Showtime;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShowtimeCommandValidator {

    private final ShowtimeRepository showtimeRepository;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;

    private static final int MAX_EXTRA_MINUTES = 60;
    private static final int PREPARATION_MINUTES = 15;
    private static final int MAX_FUTURE_DAYS = 30;
    private static final int MIN_HOURS_BEFORE = 24;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final Map<Long, RoomType> FORMAT_TO_ROOM_TYPE = Map.of(
            1L, RoomType.TYPE_2D,
            2L, RoomType.TYPE_3D,
            3L, RoomType.IMAX
    );

    // ================== CREATE ==================

    public void validateCreateRequest(CreateShowtimeRequest request) {
        validateCreateInput(request);
        validateCreateFields(request);
        validateRoomTypeMatchesFormat(request.getRoomId(), request.getFormatId());
        validateTimeWithDuration(request.getMovieId(), request.getStartTime(), request.getEndTime());
        validateStartTimeBounds(request.getStartTime());
        validateConflict(null, request.getRoomId(), request.getStartTime(), request.getEndTime());
    }

    // ================== UPDATE ==================

    public void validateUpdateRequest(ShowtimeId id, UpdateShowtimeRequest request) {
        validateUpdateInput(id, request);
        // ... (giữ nguyên logic update nếu có)
    }

    // ================== INPUT VALIDATION ==================

    private void validateCreateInput(CreateShowtimeRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Request must not be null");
        }
    }

    private void validateUpdateInput(ShowtimeId id, UpdateShowtimeRequest request) {
        if (id == null || request == null) {
            throw CommonExceptions.invalidInput("Showtime id and request must not be null");
        }
    }

    // ================== FIELD VALIDATION ==================

    private void validateCreateFields(CreateShowtimeRequest request) {
        var profile = ValidationFactory.showtime();

        ValidationEngine.of()
                .validate(request.getRoomId(), "roomId", profile.roomIdRules())
                .validate(request.getMovieId(), "movieId", profile.movieIdRules())
                .validate(request.getFormatId(), "formatId", profile.formatIdRules())
                .validate(request.getStartTime(), "startTime", profile.startTimeRules())
                .validate(request.getEndTime(), "endTime", profile.endTimeRules())
                .validate(request.getAudioLanguage(), "audioLanguage", profile.audioLanguageRules())
                .validate(request.getSubtitleLanguage(), "subtitleLanguage", profile.subtitleLanguageRules())
                .throwIfInvalid();
    }

    private void validateUpdateFields(UpdateShowtimeRequest request) {
        var profile = ValidationFactory.showtime();
        ValidationEngine.of()
                .validate(request.getAudioLanguage(), "audioLanguage", profile.audioLanguageRules())
                .validate(request.getSubtitleLanguage(), "subtitleLanguage", profile.subtitleLanguageRules())
                .throwIfInvalid();
    }

    // ================== TIME VALIDATION (ĐÃ CHUYỂN SANG INSTANT) ==================

    private void validateStartTimeBounds(Instant startTime) {
        if (startTime == null) return;

        Instant now = Instant.now();

        // 1. Không được tạo suất chiếu trong quá khứ
        if (startTime.isBefore(now)) {
            throw CommonExceptions.invalidInput(
                    "startTime",
                    ErrorCategory.INVALID_VALUE,
                    "Không thể tạo suất chiếu ở thời điểm trong quá khứ"
            );
        }

        // 2. Không được tạo suất chiếu trong vòng 24 giờ tới
        Instant minStartTime = now.plusSeconds(MIN_HOURS_BEFORE * 3600L);
        if (startTime.isBefore(minStartTime)) {
            throw CommonExceptions.invalidInput(
                    "startTime",
                    ErrorCategory.INVALID_VALUE,
                    String.format("Không thể tạo suất chiếu trong vòng 24 giờ tới. Thời gian bắt đầu tối thiểu là %s",
                            formatDateTime(minStartTime))
            );
        }

        // 3. Không được tạo suất chiếu quá xa trong tương lai
        Instant limit = now.plusSeconds(MAX_FUTURE_DAYS * 24L * 3600L);
        if (startTime.isAfter(limit)) {
            throw CommonExceptions.invalidInput(
                    "startTime",
                    ErrorCategory.INVALID_VALUE,
                    String.format("Không thể tạo suất chiếu quá %d ngày trong tương lai. Thời gian tối đa cho phép: %s",
                            MAX_FUTURE_DAYS, formatDateTime(limit))
            );
        }
    }

    private void validateTimeWithDuration(Long movieId, Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) return;

        Movie movie = movieRepository.findById(MovieId.of(movieId))
                .orElseThrow(() -> CommonExceptions.invalidInput("movieId", ErrorCategory.NOT_FOUND, "Phim không tồn tại"));

        // 1. startTime phải trước endTime
        if (!startTime.isBefore(endTime)) {
            throw CommonExceptions.invalidInput("endTime", ErrorCategory.INVALID_VALUE,
                    "Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        int duration = movie.getDuration();
        Instant minEnd = startTime.plusSeconds((duration + PREPARATION_MINUTES) * 60L);
        Instant maxEnd = minEnd.plusSeconds(MAX_EXTRA_MINUTES * 60L);

        // 2. endTime quá ngắn
        if (endTime.isBefore(minEnd)) {
            throw CommonExceptions.invalidInput("endTime", ErrorCategory.INVALID_VALUE,
                    String.format("Thời gian kết thúc phải từ %s trở đi (cần %d phút phim + %d phút chuẩn bị)",
                            formatTime(minEnd), duration, PREPARATION_MINUTES));
        }

        // 3. endTime quá dài
        if (endTime.isAfter(maxEnd)) {
            throw CommonExceptions.invalidInput("endTime", ErrorCategory.INVALID_VALUE,
                    String.format("Thời gian kết thúc tối đa là %s. Khoảng hợp lệ: %s – %s",
                            formatTime(maxEnd), formatTime(minEnd), formatTime(maxEnd)));
        }
    }

    // ================== CONFLICT ==================

    private void validateRoomTypeMatchesFormat(Long roomId, Long formatId) {
        // (giữ nguyên)
        Room room = roomRepository.findById(RoomId.of(roomId))
                .orElseThrow(() -> RoomExceptions.notFound(RoomId.of(roomId)));

        RoomType expectedType = FORMAT_TO_ROOM_TYPE.get(formatId);
        if (expectedType == null) {
            throw CommonExceptions.invalidInput("formatId", ErrorCategory.INVALID_VALUE, "Định dạng không hợp lệ");
        }

        if (room.getRoomType() != expectedType) {
            throw CommonExceptions.invalidInput("roomId", ErrorCategory.INVALID_VALUE,
                    String.format("Phòng '%s' có loại %s, không phù hợp với định dạng %s",
                            room.getName(), room.getRoomType(), expectedType));
        }
    }

    private void validateConflict(ShowtimeId excludeId, Long roomId, Instant startTime, Instant endTime) {
        if (roomId == null || startTime == null || endTime == null) return;

        boolean conflict = showtimeRepository.existsRoomConflict(roomId, startTime, endTime, excludeId);

        if (conflict) {
            Optional<Room> room = roomRepository.findById(RoomId.of(roomId));
            throw CommonExceptions.invalidInput("startTime", ErrorCategory.INVALID_VALUE,
                    String.format("Phòng chiếu %s đã có suất chiếu khác trong khung giờ %s – %s",
                            room.map(Room::getName).orElse("N/A"),
                            formatTime(startTime), formatTime(endTime)));
        }
    }

    // ================== FORMATTER ==================

    private String formatTime(Instant instant) {
        return instant.atZone(VIETNAM_ZONE)
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String formatDateTime(Instant instant) {
        return instant.atZone(VIETNAM_ZONE)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}