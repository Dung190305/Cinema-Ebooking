package com.cinemaebooking.backend.user.infrastructure.adapter;

import com.cinemaebooking.backend.user.infrastructure.mapper.UserMapper;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import com.cinemaebooking.backend.user.domain.model.User;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity;
import com.cinemaebooking.backend.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public User create(User user) {
        UserJpaEntity entity = userMapper.toEntity(user);
        entity = userJpaRepository.save(entity);
        return userMapper.toDomain(entity);
    }

    @Override
    public User update(User user) {
        UserJpaEntity oldEntity = userJpaRepository.findByIdOrThrow(user.getId().getValue());

        oldEntity.setFullName(user.getFullName());
        oldEntity.setEmail(user.getEmail());
        oldEntity.setPassword(user.getPassword());
        oldEntity.setPhoneNumber(user.getPhoneNumber());
        oldEntity.setDateOfBirth(user.getDateOfBirth());
        oldEntity.setGender(user.getGender());
        oldEntity.setAvatarUrl(user.getAvatarUrl());
        oldEntity.setRole(user.getRole());
        oldEntity.setStatus(user.getStatus());

        return userMapper.toDomain(userJpaRepository.save(oldEntity));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.getValue())
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userJpaRepository.findAll(pageable)
                .map(userMapper::toDomain);
    }

    @Override
    public Page<User> findByFilters(String fullName, String email, UserRole role, UserStatus status, Pageable pageable) {
        return userJpaRepository.findByAllFilters(fullName, email, role, status, pageable)
                .map(userMapper::toDomain);
    }

    @Override
    public Page<User> searchByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCase(
            String fullName, String email, Pageable pageable) {
        return userJpaRepository.searchByFullNameAndEmail(fullName, email, pageable)
                .map(userMapper::toDomain);
    }

    @Override
    public void deleteById(UserId id) {
        UserJpaEntity user = userJpaRepository.findByIdOrThrow(id.getValue());
        userJpaRepository.delete(user);
    }

    @Override
    public boolean existsById(UserId id) {
        return userJpaRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, UserId id) {
        if (email == null || id == null) return false;
        return userJpaRepository.existsByEmailAndIdNot(email, id.getValue());
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userJpaRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean existsByPhoneNumberAndIdNot(String phoneNumber, UserId id) {
        if (phoneNumber == null || id == null) return false;
        return userJpaRepository.existsByPhoneNumberAndIdNot(phoneNumber, id.getValue());
    }
}