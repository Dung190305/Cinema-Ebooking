package com.cinemaebooking.backend.user.infrastructure.mapper;

import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .id(entity.getId() != null ? new UserId(entity.getId()) : null)
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .phoneNumber(entity.getPhoneNumber())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public UserJpaEntity toEntity(User user) {
        if (user == null) return null;

        return UserJpaEntity.builder()
                .id(user.getId() != null ? user.getId().getValue() : null)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
