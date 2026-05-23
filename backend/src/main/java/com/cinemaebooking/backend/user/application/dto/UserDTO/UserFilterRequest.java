package com.cinemaebooking.backend.user.application.dto.UserDTO;

import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UserFilterRequest — Search and filter parameters for admin user listing.
 *
 * <p>Used as query parameters on GET /api/v1/admin
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilterRequest {
    private String fullName;
    private String email;
    private UserRole role;
    private UserStatus status;
}
