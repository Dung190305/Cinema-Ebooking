package com.cinemaebooking.backend.user.application.usecase.auth;

import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.UserExceptions;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.LoginRequest;
import com.cinemaebooking.backend.user.application.dto.Response.LoginResponse;
import com.cinemaebooking.backend.user.application.port.JwtProvider;
import com.cinemaebooking.backend.user.application.port.PasswordEncoder;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.application.validator.Auth.LoginValidator;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import com.cinemaebooking.backend.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final LoginValidator loginValidator;

    public LoginResponse execute(LoginRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Login request must not be null");
        }
        loginValidator.validate(request);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserExceptions::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw UserExceptions.invalidCredentials();
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BaseException(ErrorCode.USER_NOT_VERIFIED,
                    "Tài khoản chưa được xác minh email. Vui lòng kiểm tra hộp thư và nhập mã OTP.");
        }

        // Tạo access token (ngắn hạn, ví dụ 15-30 phút)
        String accessToken = jwtProvider.generateToken(user.getId(), user.getRole().name());
        // Tạo refresh token (dài hạn, ví dụ 7 ngày)
        String refreshToken = jwtProvider.generateRefreshToken(user.getId().getValue());

        // Trả về cả hai token
        return new LoginResponse(accessToken, refreshToken, user.getRole().name());
    }
}