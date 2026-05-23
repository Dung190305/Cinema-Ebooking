package com.cinemaebooking.backend.user.application.dto.UserDTO;

import com.cinemaebooking.backend.user.domain.valueObject.UserGender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserGender gender;
}
