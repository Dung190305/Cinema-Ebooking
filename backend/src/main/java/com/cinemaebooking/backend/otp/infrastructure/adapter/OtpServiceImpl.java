package com.cinemaebooking.backend.otp.infrastructure.adapter;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.OtpExceptions;
import com.cinemaebooking.backend.otp.application.dto.RegisterWithOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.SendOtpResponse;
import com.cinemaebooking.backend.otp.application.dto.VerifyOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import com.cinemaebooking.backend.otp.application.port.OtpRepository;
import com.cinemaebooking.backend.otp.domain.model.Otp;
import com.cinemaebooking.backend.otp.domain.model.OtpType;
import com.cinemaebooking.backend.user.application.port.PasswordEncoder;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public SendOtpResponse registerAndSendOtp(RegisterWithOtpRequest request) {
        // Bước 1: Kiểm tra email chưa tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw OtpExceptions.emailAlreadyExists(request.getEmail());
        }

        // Bước 2: Tạo User với status = INACTIVE
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth() != null
                        ? java.time.LocalDate.parse(request.getDateOfBirth())
                        : null)
                .gender(request.getGender() != null
                        ? com.cinemaebooking.backend.user.domain.valueObject.UserGender.valueOf(request.getGender())
                        : null)
                .role(UserRole.USER)
                .status(UserStatus.INACTIVE)
                .build();

        // Bước 3: Lưu user (Không tạo loyalty account ở đây nữa để tránh rác DB nếu không kích hoạt)
        User savedUser = userRepository.create(user);

        // Bước 4: Sinh OTP
        String otpCode = generateOtp();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);

        // Bước 5: Tạo và lưu OTP domain model
        Otp otp = Otp.builder()
                .userId(savedUser.getId().getValue())
                .code(otpCode)
                .otpType(OtpType.EMAIL_VERIFICATION)
                .expiredAt(expiredAt)
                .attempts(0)
                .verified(false)
                .build();

        otpRepository.save(otp);

        // Bước 6: Trả response chứa OTP code lên cho UseCase xử lý việc gửi email ngoài Transaction
        return SendOtpResponse.builder()
                .message("Mã OTP đã được tạo thành công")
                .userId(savedUser.getId().getValue())
                .email(request.getEmail())
                .expiresAt(expiredAt)
                .generatedOtpCode(otpCode) // Đảm bảo trường này được map lên UseCase
                .build();
    }

    @Override
    @Transactional
    public SendOtpResponse resendOtp(Long userId) {
        User user = userRepository.findById(
                        new com.cinemaebooking.backend.user.domain.valueObject.UserId(userId))
                .orElseThrow(() -> CommonExceptions.resourceNotFound(
                        "Không tìm thấy người dùng với id: " + userId));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw OtpExceptions.userAlreadyVerified(userId);
        }

        otpRepository.findActiveByUserIdAndType(userId, OtpType.EMAIL_VERIFICATION)
                .ifPresent(existing -> {
                    long secondsSinceCreated = TimeUnit.SECONDS.convert(
                            java.time.Duration.between(existing.getCreatedAt(), LocalDateTime.now()));
                    if (secondsSinceCreated < RESEND_COOLDOWN_SECONDS) {
                        throw OtpExceptions.resendTooSoon();
                    }
                });

        otpRepository.deleteByUserIdAndType(userId, OtpType.EMAIL_VERIFICATION);

        String otpCode = generateOtp();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);

        Otp newOtp = Otp.builder()
                .userId(userId)
                .code(otpCode)
                .otpType(OtpType.EMAIL_VERIFICATION)
                .expiredAt(expiredAt)
                .attempts(0)
                .verified(false)
                .build();

        otpRepository.save(newOtp);

        return SendOtpResponse.builder()
                .message("Mã OTP mới đã được tạo")
                .userId(userId)
                .email(user.getEmail())
                .expiresAt(expiredAt)
                .generatedOtpCode(otpCode)
                .build();
    }

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(String code, Long userId) {
        User user = userRepository.findById(
                        new UserId(userId))
                .orElseThrow(() -> CommonExceptions.resourceNotFound(
                        "Không tìm thấy người dùng với id: " + userId));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw OtpExceptions.userAlreadyVerified(userId);
        }

        Otp otp = otpRepository
                .findActiveByUserIdAndType(userId, OtpType.EMAIL_VERIFICATION)
                .orElseThrow(() -> OtpExceptions.notFound(userId));

        if (otp.isExpired()) {
            throw OtpExceptions.expired();
        }

        if (otp.isMaxAttemptsReached()) {
            throw OtpExceptions.maxAttemptsReached();
        }

        if (!otp.getCode().equals(code)) {
            otp.incrementAttempts();
            otpRepository.save(otp);
            throw OtpExceptions.incorrect();
        }

        // Verify thành công dữ liệu OTP
        otp.markAsVerified();
        otpRepository.save(otp);

        // Kích hoạt trạng thái tài khoản người dùng
        user.activate();
        userRepository.update(user);

        return VerifyOtpResponse.builder()
                .success(true)
                .message("Mã hợp lệ. Tài khoản sẵn sàng để kích hoạt dịch vụ thành viên")
                .userId(userId)
                .isActivated(true)
                .build();
    }

    private String generateOtp() {
        int code = RANDOM.nextInt(900_000) + 100_000;
        return String.valueOf(code);
    }
}