package com.cinemaebooking.backend.user.application.validator.User;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.validation.engine.ValidationEngine;
import com.cinemaebooking.backend.common.validation.factory.ValidationFactory;
import com.cinemaebooking.backend.user.application.dto.ChangeDTO.ChangePasswordRequest;
import com.cinemaebooking.backend.user.application.dto.UserDTO.AdminUpdateUserRequest;
import com.cinemaebooking.backend.user.application.dto.UserDTO.CreateUserRequest;
import com.cinemaebooking.backend.user.application.dto.UserDTO.UpdateUserRequest;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCommandValidator {

    private final UserRepository userRepository;
    // ================== UPDATE (USER SELF) ==================
    public void validateUpdateRequest(UserId id, UpdateUserRequest request) {

        if (id == null || request == null) {
            throw CommonExceptions.invalidInput("User id and request must not be null");
        }

        var profile = ValidationFactory.user();

        ValidationEngine.of()
                .validate(request.getFullName(), "fullName", profile.fullNameRules())
                .validate(request.getPhoneNumber(), "phoneNumber", profile.phoneRules())
                .validate(request.getGender(), "gender", profile.genderRules())
                .validate(request.getDateOfBirth(), "dateOfBirth", profile.dobRules())
                .throwIfInvalid();
    }



    // ================== CHANGE PASSWORD ==================
    public void validateChangePasswordRequest(UserId id, ChangePasswordRequest request) {

        if (id == null || request == null) {
            throw CommonExceptions.invalidInput("User id and request must not be null");
        }

        var profile = ValidationFactory.user();

        ValidationEngine.of()
                .validate(request.getNewPassword(), "password", profile.passwordRules())
                .throwIfInvalid();
    }

    // ================== BUSINESS CHECKS ==================

    private boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}