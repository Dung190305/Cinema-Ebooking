package com.cinemaebooking.backend.user.application.mapper;


import com.cinemaebooking.backend.user.application.dto.Response.UserResponse;
import com.cinemaebooking.backend.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserResponseMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;

        return new UserResponse(
                user.getId() != null ? user.getId().getValue() : null,
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}