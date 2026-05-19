package com.cinemaebooking.backend.otp.infrastructure.adapter;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.OtpExceptions;
import com.cinemaebooking.backend.loyalty.application.usecase.loyalty_account.CreateLoyaltyAccountUseCase;
import com.cinemaebooking.backend.otp.application.dto.RegisterWithOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.SendOtpResponse;
import com.cinemaebooking.backend.otp.application.dto.VerifyOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import com.cinemaebooking.backend.otp.application.port.OtpRepository;
import com.cinemaebooking.backend.otp.domain.model.Otp;
import com.cinemaebooking.backend.otp.domain.model.OtpType;
import com.cinemaebooking.backend.user.application.port.EmailService;
import com.cinemaebooking.backend.user.application.port.PasswordEncoder;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import com.cinemaebooking.backend.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * OtpServiceImpl - Implementation của OtpService port.
 *
 * <p>Chứa toàn bộ business logic cho luồng đăng ký với xác minh email qua OTP.
 *
 * <p>Luồng đăng ký hoàn chỉnh:
 * <pre>
 * 1. registerAndSendOtp()
 *    User điền form đăng ký
 *    → Tạo user (INACTIVE)
 *    → Tạo OTP 6 số
 *    → Gửi email
 *    → Trả userId cho frontend
 *
 * 2. resendOtp()  [tuỳ chọn]
 *    User không nhận được email
 *    → Xóa OTP cũ, tạo OTP mới
 *    → Gửi lại email
 *
 * 3. verifyOtp()
 *    User nhập mã OTP
 *    → Kiểm tra: tồn tại, chưa hết hạn, chưa quá 5 lần sai
 *    → Đúng: activate user (INACTIVE → ACTIVE)
 *    → Sai: throw exception
 * </pre>
 *
 * <p>Security measures:
 * <ul>
 *   <li>OTP 6 chữ số ngẫu nhiên (1 triệu combinations)</li>
 *   <li>Hết hạn sau 5 phút</li>
 *   <li>Tối đa 5 lần nhập sai</li>
 *   <li>Resend cooldown 60 giây (chống spam email)</li>
 *   <li>User chỉ có 1 OTP active tại một thời điểm</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CreateLoyaltyAccountUseCase createLoyaltyAccountUseCase;

    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    // =======================================================================
    // PUBLIC METHODS
    // =======================================================================

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

        // Bước 3: Lưu user và tạo loyalty account
        User savedUser = userRepository.create(user);
        createLoyaltyAccountUseCase.execute(savedUser.getId().getValue());

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

        // Bước 6: Gửi email
        sendOtpEmail(request.getEmail(), otpCode);

        // Bước 7: Trả response
        return SendOtpResponse.builder()
                .message("Mã OTP đã được gửi đến email của bạn")
                .userId(savedUser.getId().getValue())
                .email(request.getEmail())
                .expiresAt(expiredAt)
                .build();
    }

    @Override
    @Transactional
    public SendOtpResponse resendOtp(Long userId) {
        // Bước 1: Fetch user
        User user = userRepository.findById(
                        new com.cinemaebooking.backend.user.domain.valueObject.UserId(userId))
                .orElseThrow(() -> CommonExceptions.resourceNotFound(
                        "Không tìm thấy người dùng với id: " + userId));

        // Bước 2: Kiểm tra user chưa verify
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw OtpExceptions.userAlreadyVerified(userId);
        }

        // Bước 3: Kiểm tra cooldown 60 giây
        otpRepository.findActiveByUserIdAndType(userId, OtpType.EMAIL_VERIFICATION)
                .ifPresent(existing -> {
                    long secondsSinceCreated = TimeUnit.SECONDS.convert(
                            java.time.Duration.between(existing.getCreatedAt(), LocalDateTime.now()));
                    if (secondsSinceCreated < RESEND_COOLDOWN_SECONDS) {
                        throw OtpExceptions.resendTooSoon();
                    }
                });

        // Bước 4: Xóa OTP cũ
        otpRepository.deleteByUserIdAndType(userId, OtpType.EMAIL_VERIFICATION);

        // Bước 5: Tạo OTP mới
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

        // Bước 6: Gửi email
        sendOtpEmail(user.getEmail(), otpCode);

        return SendOtpResponse.builder()
                .message("Mã OTP mới đã được gửi đến email")
                .userId(userId)
                .email(user.getEmail())
                .expiresAt(expiredAt)
                .build();
    }

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(String code, Long userId) {
        // Bước 1: Fetch user
        User user = userRepository.findById(
                        new com.cinemaebooking.backend.user.domain.valueObject.UserId(userId))
                .orElseThrow(() -> CommonExceptions.resourceNotFound(
                        "Không tìm thấy người dùng với id: " + userId));

        // Bước 2: Kiểm tra user chưa verify
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw OtpExceptions.userAlreadyVerified(userId);
        }

        // Bước 3: Tìm OTP active
        Otp otp = otpRepository
                .findActiveByUserIdAndType(userId, OtpType.EMAIL_VERIFICATION)
                .orElseThrow(() -> OtpExceptions.notFound(userId));

        // Bước 4: Kiểm tra hết hạn
        if (otp.isExpired()) {
            throw OtpExceptions.expired();
        }

        // Bước 5: Kiểm tra số lần sai
        if (otp.isMaxAttemptsReached()) {
            throw OtpExceptions.maxAttemptsReached();
        }

        // Bước 6: So sánh mã OTP
        if (!otp.getCode().equals(code)) {
            otp.incrementAttempts();
            otpRepository.save(otp);
            throw OtpExceptions.incorrect();
        }

        // Bước 7: Verify thành công
        otp.markAsVerified();
        otpRepository.save(otp);

        // Bước 8: Kích hoạt tài khoản
        user.activate();
        userRepository.update(user);

        // Bước 9: Trả response
        return VerifyOtpResponse.builder()
                .success(true)
                .message("Xác minh thành công! Tài khoản của bạn đã được kích hoạt")
                .userId(userId)
                .isActivated(true)
                .build();
    }

    // =======================================================================
    // PRIVATE HELPER METHODS
    // =======================================================================

    private String generateOtp() {
        int code = RANDOM.nextInt(900_000) + 100_000;
        return String.valueOf(code);
    }

    private void sendOtpEmail(String to, String otpCode) {
        String subject = "Mã xác minh đăng ký tài khoản - Cinema E-Booking";
        String body = String.format("""
                Xin chào,

                Cảm ơn bạn đã đăng ký tài khoản tại Cinema E-Booking.

                Mã xác minh của bạn là: %s

                Mã này có hiệu lực trong 5 phút.
                Vui lòng không chia sẻ mã này với bất kỳ ai.

                Trân trọng,
                Đội ngũ Cinema E-Booking
                """, otpCode);
        emailService.send(to, subject, body);
    }
}
