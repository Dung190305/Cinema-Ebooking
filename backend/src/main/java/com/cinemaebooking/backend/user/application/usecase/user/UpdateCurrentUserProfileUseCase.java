package com.cinemaebooking.backend.user.application.usecase.user;

import com.cinemaebooking.backend.common.exception.domain.UserExceptions;
import com.cinemaebooking.backend.user.application.dto.Response.UserResponse;
import com.cinemaebooking.backend.user.application.dto.UserDTO.UpdateUserRequest;
import com.cinemaebooking.backend.user.application.mapper.UserResponseMapper;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCurrentUserProfileUseCase {

    private final UserRepository userRepository;
    private final UserResponseMapper mapper;

    public UserResponse execute(Long userIdRaw, UpdateUserRequest request) {

        UserId userId = new UserId(userIdRaw);

        User user = userRepository.findById(userId)
                .orElseThrow(UserExceptions::unauthorized);

        user.updateProfile(
                request.getFullName(),
                request.getPhoneNumber(),
                request.getDateOfBirth(),
                request.getGender()
        );

        User saved = userRepository.update(user);

        return mapper.toUserResponse(saved);
    }
}