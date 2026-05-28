package com.cinemaebooking.backend.user.application.usecase.verification;

import com.cinemaebooking.backend.loyalty.application.usecase.loyalty_account.CreateLoyaltyAccountUseCase;
import com.cinemaebooking.backend.otp.application.dto.VerifyOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final OtpService otpService;
    private final CreateLoyaltyAccountUseCase createLoyaltyAccountUseCase;

    /**
     * Xác minh mã OTP, kích hoạt tài khoản user và đồng thời khởi tạo ví Loyalty.
     * Sử dụng @Transactional tại đây để đảm bảo tính nguyên tử (Atomicity):
     * Kích hoạt user thành công thì buộc phải có tài khoản Loyalty được gán kèm.
     */
    @Transactional
    public VerifyOtpResponse execute(String code, Long userId) {

        // 1. Xác thực OTP và chuyển trạng thái user thành ACTIVE
        VerifyOtpResponse response = otpService.verifyOtp(code, userId);

        // 2. Khởi tạo tài khoản Loyalty ngay khi user vừa ACTIVE thành công
        if (response.isActivated()) {
            createLoyaltyAccountUseCase.execute(userId);
        }

        return response;
    }
}