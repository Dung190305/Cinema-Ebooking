package com.cinemaebooking.backend.user.application.usecase.auth;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.loyalty.application.usecase.loyalty_account.CreateLoyaltyAccountUseCase;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.RegisterRequest;
import com.cinemaebooking.backend.user.application.port.PasswordEncoder;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.application.validator.Auth.RegisterValidator;
import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterValidator registerValidator;
    private final CreateLoyaltyAccountUseCase createLoyaltyAccountUseCase;

    @Transactional
    public void execute(RegisterRequest request) {

        if (request == null) {
            throw CommonExceptions.invalidInput("Register request must not be null");
        }

        registerValidator.validate(request);

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.create(user);
        createLoyaltyAccountUseCase.execute(savedUser.getId().getValue());
    }
}
