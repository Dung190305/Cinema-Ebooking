package com.cinemaebooking.backend.coupon.infrastructure.persistence.repository;

import com.cinemaebooking.backend.coupon.domain.enums.CouponType;
import com.cinemaebooking.backend.coupon.infrastructure.persistence.entity.CouponJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("CouponJpaRepository - Data JPA Tests (Soft Delete + Business Logic)")
class CouponJpaRepositoryDataJpaTest {

    private static final String VALID_CODE = "SUMMER2026";
    private static final String EXPIRED_CODE = "WINTER2025";
    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CouponJpaEntity activeCoupon;
    private CouponJpaEntity expiredCoupon;

    @BeforeEach
    void setUp() {
        activeCoupon = CouponJpaEntity.builder()
                .code(VALID_CODE)
                .type(CouponType.PERCENT)
                .value(BigDecimal.valueOf(15.0))
                .usageLimit(100)
                .perUserUsage(1)
                .pointsToRedeem(0)
                .startDate(TODAY)
                .endDate(TODAY.plusMonths(3))
                .build();

        expiredCoupon = CouponJpaEntity.builder()
                .code(EXPIRED_CODE)
                .type(CouponType.FIXED)
                .value(BigDecimal.valueOf(50000))
                .usageLimit(50)
                .perUserUsage(1)
                .pointsToRedeem(100)
                .startDate(TODAY.minusMonths(2))
                .endDate(TODAY.minusDays(1))
                .build();

        entityManager.persistAndFlush(activeCoupon);
        entityManager.persistAndFlush(expiredCoupon);
    }

    // ===================== EXISTENCE QUERIES =====================

    @Nested
    @DisplayName("Existence Queries")
    class ExistenceQueries {

        @Test
        @DisplayName("existsByCode should return true when coupon exists")
        void shouldReturnTrueWhenCodeExists() {
            assertThat(couponJpaRepository.existsByCodeIgnoreCase(VALID_CODE)).isTrue();
        }

        @Test
        @DisplayName("existsByCode should return false when coupon does not exist")
        void shouldReturnFalseWhenCodeNotExists() {
            assertThat(couponJpaRepository.existsByCodeIgnoreCase("INVALID")).isFalse();
        }

        @Test
        @DisplayName("findByCodeIgnoreCase should find coupon ignoring case")
        void shouldFindByCodeIgnoreCase() {
            Optional<CouponJpaEntity> found = couponJpaRepository.findByCodeIgnoreCase("summer2026");
            assertThat(found).isPresent();
            assertThat(found.get().getCode()).isEqualTo(VALID_CODE);
        }
    }

    // ===================== BUSINESS LOGIC QUERIES =====================

    @Nested
    @DisplayName("Business Logic Queries")
    class BusinessLogicQueries {

        @Test
        @DisplayName("Should return true for valid coupon (not expired and has usage)")
        void shouldReturnTrueWhenCouponIsValid() {
            boolean exists = couponJpaRepository
                    .existsByCodeIgnoreCaseAndEndDateAfterAndUsageLimitGreaterThan(VALID_CODE, TODAY, 10);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when coupon is expired")
        void shouldReturnFalseWhenCouponIsExpired() {
            boolean exists = couponJpaRepository
                    .existsByCodeIgnoreCaseAndEndDateAfter(EXPIRED_CODE, TODAY);

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false when usage limit is exceeded")
        void shouldReturnFalseWhenUsageLimitExceeded() {
            boolean exists = couponJpaRepository
                    .existsByCodeIgnoreCaseAndUsageLimitGreaterThan(VALID_CODE, 200);

            assertThat(exists).isFalse();
        }
    }

    // ===================== DUPLICATE CHECK =====================

    @Nested
    @DisplayName("Duplicate Code Validation")
    class DuplicateCodeValidation {

        @Test
        @DisplayName("existsByCodeAndIdNot should detect duplicate except for the same id")
        void shouldDetectDuplicateExceptSameId() {
            boolean otherId = couponJpaRepository.existsByCodeIgnoreCaseAndIdNot(VALID_CODE, 999L);
            assertThat(otherId).isTrue();

            boolean sameId = couponJpaRepository.existsByCodeIgnoreCaseAndIdNot(VALID_CODE, activeCoupon.getId());
            assertThat(sameId).isFalse();
        }
    }

    // ===================== SOFT DELETE BEHAVIOR =====================

    @Nested
    @DisplayName("Soft Delete Behavior")
    class SoftDeleteBehavior {

        @Test
        @DisplayName("Soft delete should set deletedAt and keep record in database (no hard delete)")
        void shouldSoftDeleteSetDeletedAtAndKeepInDatabase() {
            couponJpaRepository.delete(activeCoupon);
            entityManager.flush();

            // Verify record still exists in DB
            Long count = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM coupons WHERE code = :code")
                    .setParameter("code", VALID_CODE)
                    .getSingleResult();

            assertThat(count).isEqualTo(1);

            // Verify deleted_at is set
            Object deletedAt = entityManager.getEntityManager()
                    .createNativeQuery("SELECT deleted_at FROM coupons WHERE code = :code")
                    .setParameter("code", VALID_CODE)
                    .getSingleResult();

            assertThat(deletedAt).isNotNull();
        }

        @Test
        @DisplayName("All repository queries should ignore soft-deleted records")
        void shouldIgnoreSoftDeletedRecordsInAllQueries() {
            couponJpaRepository.delete(activeCoupon);
            entityManager.flush();

            // Standard queries
            assertThat(couponJpaRepository.existsByCodeIgnoreCase(VALID_CODE)).isFalse();
            assertThat(couponJpaRepository.findByCodeIgnoreCase(VALID_CODE)).isEmpty();

            List<CouponJpaEntity> allCoupons = couponJpaRepository.findAll();
            assertThat(allCoupons)
                    .extracting(CouponJpaEntity::getCode)
                    .doesNotContain(VALID_CODE);

            // Business logic queries
            assertThat(couponJpaRepository
                    .existsByCodeIgnoreCaseAndEndDateAfterAndUsageLimitGreaterThan(VALID_CODE, TODAY, 10))
                    .isFalse();

            assertThat(couponJpaRepository
                    .existsByCodeIgnoreCaseAndEndDateAfter(VALID_CODE, TODAY))
                    .isFalse();
        }


        @Test
        @DisplayName("Soft delete should also work correctly with expired coupons")
        void shouldIgnoreSoftDeletedExpiredCoupon() {
            couponJpaRepository.delete(expiredCoupon);
            entityManager.flush();

            assertThat(couponJpaRepository.existsByCodeIgnoreCase(EXPIRED_CODE)).isFalse();

            List<CouponJpaEntity> all = couponJpaRepository.findAll();
            assertThat(all)
                    .extracting(CouponJpaEntity::getCode)
                    .doesNotContain(EXPIRED_CODE);
        }

        @Test
        @DisplayName("Re-persisting after soft delete should still be ignored by queries")
        void shouldStillIgnoreRecordAfterRePersist() {
            couponJpaRepository.delete(expiredCoupon);
            entityManager.flush();

            // Try to re-persist
            entityManager.persistAndFlush(expiredCoupon);

            assertThat(couponJpaRepository.existsByCodeIgnoreCase(EXPIRED_CODE)).isFalse();
            assertThat(couponJpaRepository.findAll())
                    .extracting(CouponJpaEntity::getCode)
                    .doesNotContain(EXPIRED_CODE);
        }
    }
}
