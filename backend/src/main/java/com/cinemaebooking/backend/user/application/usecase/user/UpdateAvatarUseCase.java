package com.cinemaebooking.backend.user.application.usecase.user;

import com.cinemaebooking.backend.common.exception.domain.UserExceptions;
import com.cinemaebooking.backend.user.application.dto.Response.UserResponse;
import com.cinemaebooking.backend.user.application.dto.UserDTO.UpdateAvatarRequest;
import com.cinemaebooking.backend.user.application.mapper.UserResponseMapper;
import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateAvatarUseCase {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    @Transactional
    public UserResponse execute(Long userId, UpdateAvatarRequest request) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> UserExceptions.notFound(UserId.of(userId)));
        user.setAvatarUrl(request.getAvatarUrl());
        User savedUser = userRepository.updateAvatar(user);
        return userResponseMapper.toUserResponse(savedUser);
    }
}