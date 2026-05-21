package com.cinemaebooking.backend.user.application.usecase.user;

import com.cinemaebooking.backend.common.exception.domain.UserExceptions;
import com.cinemaebooking.backend.user.application.dto.ChangeDTO.ChangePasswordRequest;
import com.cinemaebooking.backend.user.application.port.PasswordEncoder;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.application.validator.User.UserCommandValidator;
import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCommandValidator userCommandValidator;

    public void execute(Long userIdRaw, ChangePasswordRequest request) {

        UserId userId = new UserId(userIdRaw);

        User user = userRepository.findById(userId)
                .orElseThrow(UserExceptions::unauthorized);

        userCommandValidator.validateChangePasswordRequest(userId,request);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw UserExceptions.invalidCredentials();
        }

        String hashed = passwordEncoder.encode(request.getNewPassword());

        user.changePassword(hashed);

        userRepository.update(user);
    }
}