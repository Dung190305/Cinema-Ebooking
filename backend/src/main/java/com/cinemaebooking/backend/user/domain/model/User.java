package com.cinemaebooking.backend.user.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import com.cinemaebooking.backend.user.domain.valueObject.UserGender;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder(toBuilder = true)
public class User extends BaseEntity<UserId> {

    private String fullName;
    private String email;
    private String password;  // hashed password
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserGender gender;
    @Setter
    private String avatarUrl;
    private UserRole role;
    private UserStatus status;

    // ================== BUSINESS METHODS ==================

    /**
     * Update basic profile fields (not including password or role)
     */
    public void updateProfile(String fullName, String phoneNumber, LocalDate dateOfBirth, UserGender gender) {
        validateName(fullName);
        validatePhone(phoneNumber);

        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    /**
     * Update password
     */
    public void changePassword(String hashedPassword) {
        validatePassword(hashedPassword);
        this.password = hashedPassword;
    }

    /**
     * Admin can change role
     */
    public void changeRole(UserRole newRole) {
        validateRole(newRole);
        this.role = newRole;
    }

    /**
     * Change account status
     */
    public void changeStatus(UserStatus newStatus) {
        validateStatus(newStatus);
        this.status = newStatus;
    }

    /**
     * Activate user account
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Deactivate / ban user
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    // ================== VALIDATION ==================

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw CommonExceptions.invalidInput("Full name must not be empty");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw CommonExceptions.invalidInput("Email must not be empty");
        }
        if (!email.contains("@")) {
            throw CommonExceptions.invalidInput("Email is invalid");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw CommonExceptions.invalidInput("Password must not be empty");
        }
    }

    private void validatePhone(String phone) {
        if (phone != null && phone.length() > 15) {
            throw CommonExceptions.invalidInput("Phone number is too long");
        }
    }

    private void validateRole(UserRole role) {
        if (role == null) {
            throw CommonExceptions.invalidInput("User role must not be null");
        }
    }

    private void validateStatus(UserStatus status) {
        if (status == null) {
            throw CommonExceptions.invalidInput("User status must not be null");
        }
    }
}