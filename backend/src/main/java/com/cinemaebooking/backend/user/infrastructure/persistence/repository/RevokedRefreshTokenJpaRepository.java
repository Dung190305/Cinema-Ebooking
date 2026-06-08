package com.cinemaebooking.backend.user.infrastructure.persistence.repository;

import com.cinemaebooking.backend.user.infrastructure.persistence.entity.RevokedRefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for revoked refresh token blacklist.
 * Entries can be hard-deleted after expiration since they serve no purpose.
 */
@Repository
public interface RevokedRefreshTokenJpaRepository extends JpaRepository<RevokedRefreshTokenJpaEntity, Long> {

    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RevokedRefreshTokenJpaEntity r WHERE r.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);
}
