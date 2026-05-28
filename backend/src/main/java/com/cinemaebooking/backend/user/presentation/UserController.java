package com.cinemaebooking.backend.user.presentation;

import com.cinemaebooking.backend.common.security.CustomUserPrincipal;
import com.cinemaebooking.backend.user.application.dto.ChangeDTO.ChangePasswordRequest;
import com.cinemaebooking.backend.user.application.dto.UserDTO.UpdateAvatarRequest;
import com.cinemaebooking.backend.user.application.dto.UserDTO.UpdateUserRequest;
import com.cinemaebooking.backend.user.application.dto.Response.UserResponse;
import com.cinemaebooking.backend.user.application.usecase.user.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final UpdateCurrentUserProfileUseCase updateCurrentUserProfileUseCase;
    private final UpdateAvatarUseCase updateAvatarUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return getCurrentUserProfileUseCase.execute(principal.getUserId());
    }

    @PutMapping("/me")
    public UserResponse updateMe(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return updateCurrentUserProfileUseCase.execute(principal.getUserId(), request);
    }

    @PatchMapping("/me/avatar")
    public UserResponse updateAvatar(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateAvatarRequest request
    ) {
        return updateAvatarUseCase.execute(principal.getUserId(), request);
    }

    @PutMapping("/me/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        changePasswordUseCase.execute(principal.getUserId(), request);
    }
}