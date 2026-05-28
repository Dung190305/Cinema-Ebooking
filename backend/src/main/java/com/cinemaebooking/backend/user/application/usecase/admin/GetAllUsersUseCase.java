package com.cinemaebooking.backend.user.application.usecase.admin;

import com.cinemaebooking.backend.user.application.dto.Response.UserResponse;
import com.cinemaebooking.backend.user.application.dto.UserDTO.UserFilterRequest;
import com.cinemaebooking.backend.user.application.mapper.UserResponseMapper;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAllUsersUseCase {

    private final UserRepository userRepository;
    private final UserResponseMapper mapper;

    public Page<UserResponse> execute(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(mapper::toUserResponse);
    }

    public Page<UserResponse> executeWithFilters(UserFilterRequest filter, Pageable pageable) {
        return userRepository.findByFilters(
                filter.getFullName(),
                filter.getEmail(),
                filter.getRole(),
                filter.getStatus(),
                pageable
        ).map(mapper::toUserResponse);
    }
}
