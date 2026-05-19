package com.cinemaebooking.backend.otp.application.port;

import com.cinemaebooking.backend.otp.application.dto.RegisterWithOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.SendOtpResponse;
import com.cinemaebooking.backend.otp.application.dto.VerifyOtpResponse;

/**
 * OtpService - Port interface cho OTP operations.
 *
 * <p>Định nghĩa 3 operations chính của luồng đăng ký với OTP:
 * <ol>
 *   <li>registerAndSendOtp: Tạo tài khoản INACTIVE + gửi OTP qua email</li>
 *   <li>resendOtp: Gửi lại OTP (có cooldown 60s để chống spam)</li>
 *   <li>verifyOtp: Xác minh OTP → kích hoạt tài khoản (ACTIVE)</li>
 * </ol>
 *
 * <p>Implementation: OtpServiceImpl
 *
 * @author ducthinhn
 * @since 2026
 */
public interface OtpService {

    /**
     * Bước 1 của luồng đăng ký: Tạo tài khoản mới ở trạng thái INACTIVE
     * và gửi mã OTP đến email của user.
     *
     * <p>Luồng:
     * <ol>
     *   <li>Kiểm tra email chưa tồn tại → throw nếu đã tồn tại</li>
     *   <li>Tạo User với status = INACTIVE</li>
     *   <li>Tạo loyalty account cho user</li>
     *   <li>Sinh mã OTP 6 chữ số ngẫu nhiên</li>
     *   <li>Lưu OTP vào database</li>
     *   <li>Gửi email chứa mã OTP</li>
     *   <li>Trả về thông tin cho frontend (userId, expiresAt)</li>
     * </ol>
     *
     * @param request thông tin đăng ký từ user (email, password, fullName, ...)
     * @return SendOtpResponse chứa userId và thời điểm OTP hết hạn
     */
    SendOtpResponse registerAndSendOtp(RegisterWithOtpRequest request);

    /**
     * Bước 2 của luồng: Gửi lại mã OTP mới.
     *
     * <p>Anti-spam: Kiểm tra đã qua ít nhất 60 giây kể từ OTP cuối cùng.
     * Nếu chưa đủ 60s → throw OTP_RESEND_TOO_SOON.
     *
     * <p>Luồng:
     * <ol>
     *   <li>Fetch user → throw nếu không tìm thấy</li>
     *   <li>Kiểm tra user chưa verify (status != ACTIVE)</li>
     *   <li>Kiểm tra cooldown 60s</li>
     *   <li>Xóa OTP cũ (nếu có)</li>
     *   <li>Tạo OTP mới và lưu</li>
     *   <li>Gửi email</li>
     * </ol>
     *
     * @param userId ID của user cần gửi lại OTP
     * @return SendOtpResponse chứa thông tin OTP mới
     */
    SendOtpResponse resendOtp(Long userId);

    /**
     * Bước 3 của luồng: Xác minh mã OTP.
     *
     * <p>Luồng:
     * <ol>
     *   <li>Fetch user → throw nếu không tìm thấy</li>
     *   <li>Kiểm tra user chưa verify (tránh verify lại)</li>
     *   <li>Tìm OTP active của user → throw nếu không có</li>
     *   <li>Kiểm tra OTP chưa hết hạn → throw nếu hết hạn</li>
     *   <li>Kiểm tra attempts < 5 → throw nếu quá số lần sai</li>
     *   <li>So sánh code với OTP trong DB:
     *     <ul>
     *       <li>Đúng → verified = true, user.activate() → ACTIVE, trả success</li>
     *       <li>Sai → incrementAttempts(), throw OTP_INCORRECT</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param code mã OTP user nhập vào
     * @param userId ID của user đang verify
     * @return VerifyOtpResponse chứa kết quả xác minh
     */
    VerifyOtpResponse verifyOtp(String code, Long userId);
}
