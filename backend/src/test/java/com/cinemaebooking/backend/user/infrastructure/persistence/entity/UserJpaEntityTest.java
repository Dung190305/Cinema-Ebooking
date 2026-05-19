package com.cinemaebooking.backend.user.infrastructure.persistence.entity;

import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserJpaEntityTest {

    @Test
    @DisplayName("Should create UserJpaEntity successfully with builder")
    void shouldCreateUserJpaEntityWithBuilder() {
        UserJpaEntity user = UserJpaEntity.builder()
                .fullName("Nguyen Van A")
                .email("nguyenvana@gmail.com")
                .password("hashed_password_123")
                .phoneNumber("0987654321")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .avatarUrl("https://example.com/avatar.jpg")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .loyaltyAccountId(100L)
                .build();

        assertNotNull(user);
        assertEquals("Nguyen Van A", user.getFullName());
        assertEquals("nguyenvana@gmail.com", user.getEmail());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(100L, user.getLoyaltyAccountId());
    }

    @Test
    @DisplayName("Should support toBuilder() for immutable updates")
    void shouldSupportToBuilder() {
        UserJpaEntity user = UserJpaEntity.builder()
                .fullName("Old Name")
                .email("old@email.com")
                .password("oldpass")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        UserJpaEntity updated = user.toBuilder()
                .fullName("New Name")
                .status(UserStatus.INACTIVE)
                .build();

        assertEquals("New Name", updated.getFullName());
        assertEquals("old@email.com", updated.getEmail()); // unchanged
        assertEquals(UserStatus.INACTIVE, updated.getStatus());
    }
}