package com.cinemaebooking.backend.user.application.port;

import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {

    User create(User user);

    User update(User user);

    User updateAvatar(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    void deleteById(UserId id);

    boolean existsById(UserId id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UserId id);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UserId id);
}

