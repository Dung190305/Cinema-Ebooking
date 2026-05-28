package com.cinemaebooking.backend.user.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity;
import com.cinemaebooking.backend.user.domain.enums.UserRole;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserJpaRepository - JPA repository for User entity.
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Repository
public interface UserJpaRepository extends SoftDeleteJpaRepository<UserJpaEntity> {
    // ===== BASIC =====
    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhoneNumber(String phone);

    boolean existsByPhoneNumberAndIdNot(String phone, Long id);
    // ===== LOGIN =====
    Optional<UserJpaEntity> findByEmailAndStatus(String email, UserStatus status);

    // ===== FILTERING =====
    Page<UserJpaEntity> findByRole(UserRole role, Pageable pageable);

    Page<UserJpaEntity> findByStatus(UserStatus status, Pageable pageable);

    Page<UserJpaEntity> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    // ===== SEARCH =====
    @Query("""
            SELECT u FROM UserJpaEntity u
            WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))
              AND LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))
            """)
    Page<UserJpaEntity> searchByFullNameAndEmail(
            @Param("fullName") String fullName,
            @Param("email") String email,
            Pageable pageable
    );

    @Query("""
            SELECT u FROM UserJpaEntity u
            WHERE (:role IS NULL OR u.role = :role)
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<UserJpaEntity> findByRoleAndStatusFilters(
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT u FROM UserJpaEntity u
            WHERE (:fullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))
              AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<UserJpaEntity> findByAllFilters(
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}