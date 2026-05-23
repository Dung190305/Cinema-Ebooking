package com.cinemaebooking.backend.user.presentation;

import com.cinemaebooking.backend.user.application.dto.ChangeDTO.ChangeUserRoleRequest;
import com.cinemaebooking.backend.user.application.dto.Response.UserDetailResponse;
import com.cinemaebooking.backend.user.application.dto.Response.UserResponse;
import com.cinemaebooking.backend.user.application.dto.UserDTO.AdminUpdateUserRequest;
import com.cinemaebooking.backend.user.application.dto.UserDTO.UserFilterRequest;
import com.cinemaebooking.backend.user.application.usecase.admin.*;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetAllUsersUseCase getUserListUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @GetMapping("/{id}")
    public UserDetailResponse getUserById(@PathVariable Long id) {
        return getUserByIdUseCase.execute(UserId.of(id));
    }

    @GetMapping
    public Page<UserResponse> getAllUsers(
            Pageable pageable,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        if (fullName == null && email == null && role == null && status == null) {
            return getUserListUseCase.execute(pageable);
        }

        UserFilterRequest filter = UserFilterRequest.builder()
                .fullName(fullName)
                .email(email)
                .role(role != null ? com.cinemaebooking.backend.user.domain.enums.UserRole.valueOf(role) : null)
                .status(status != null ? com.cinemaebooking.backend.user.domain.enums.UserStatus.valueOf(status) : null)
                .build();

        return getUserListUseCase.executeWithFilters(filter, pageable);
    }

    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable Long id) {
        activateUserUseCase.execute(UserId.of(id));
    }

    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        deactivateUserUseCase.execute(UserId.of(id));
    }

    @PutMapping("/{id}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(
            @PathVariable Long id,
            @RequestBody ChangeUserRoleRequest request
    ) {
        changeUserRoleUseCase.execute(UserId.of(id), request.getRole());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        deleteUserUseCase.execute(UserId.of(id));
    }
}
