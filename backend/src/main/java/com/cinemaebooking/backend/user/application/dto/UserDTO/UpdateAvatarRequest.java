package com.cinemaebooking.backend.user.application.dto.UserDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvatarRequest {
    @NotBlank(message = "Avatar URL must not be blank")
    private String avatarUrl;
}