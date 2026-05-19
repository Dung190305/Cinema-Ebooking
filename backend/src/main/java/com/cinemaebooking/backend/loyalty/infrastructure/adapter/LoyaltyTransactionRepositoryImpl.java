package com.cinemaebooking.backend.loyalty.infrastructure.adapter;

import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.loyalty.application.port.LoyaltyTransactionRepository;
import com.cinemaebooking.backend.loyalty.domain.model.LoyaltyTransaction;
import com.cinemaebooking.backend.loyalty.infrastructure.mapper.loyalty_transaction.LoyaltyTransactionMapper;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyAccountJpaEntity;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyTransactionJpaEntity;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.repository.LoyaltyAccountJpaRepository;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.repository.LoyaltyTransactionJpaRepository;
import com.cinemaebooking.backend.user_coupon.infrastructure.persistence.repository.UserCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoyaltyTransactionRepositoryImpl implements LoyaltyTransactionRepository {

    private final LoyaltyTransactionJpaRepository jpaRepository;
    private final LoyaltyAccountJpaRepository accountJpaRepository;
    private final BookingJpaRepository bookingJpaRepository;
    private final UserCouponJpaRepository userCouponJpaRepository;
    private final LoyaltyTransactionMapper mapper;

    @Override
    public LoyaltyTransaction save(LoyaltyTransaction transaction) {
        LoyaltyAccountJpaEntity accountRef = accountJpaRepository.getReferenceById(transaction.getLoyaltyAccountId());

        LoyaltyTransactionJpaEntity.LoyaltyTransactionJpaEntityBuilder<?, ?> builder = LoyaltyTransactionJpaEntity.builder()
                .id(transaction.getId() != null ? transaction.getId().getValue() : null)
                .loyaltyAccount(accountRef)
                .type(transaction.getType())
                .changePoint(transaction.getChangePoint())
                .balanceAfter(transaction.getBalanceAfter())
                .changeDate(transaction.getChangeDate());

        if (transaction.getBookingId() != null) {
            builder.booking(bookingJpaRepository.getReferenceById(transaction.getBookingId()));
        }
        if (transaction.getUserCouponId() != null) {
            builder.userCoupon(userCouponJpaRepository.getReferenceById(transaction.getUserCouponId()));
        }

        return mapper.toDomain(jpaRepository.save(builder.build()));
    }

    @Override
    public List<LoyaltyTransaction> findByLoyaltyAccountId(Long loyaltyAccountId) {
        return jpaRepository.findByLoyaltyAccountIdOrderByChangeDateDesc(loyaltyAccountId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<LoyaltyTransaction> findByBookingId(Long bookingId) {
        return jpaRepository.findByBookingId(bookingId)
                .map(mapper::toDomain);
    }
}
