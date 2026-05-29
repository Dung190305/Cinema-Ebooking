package com.cinemaebooking.backend.coupon.application.usecase;

import com.cinemaebooking.backend.coupon.application.dto.PublicCouponResponse;
import com.cinemaebooking.backend.coupon.application.mapper.CouponResponseMapper;
import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.coupon.domain.enums.CouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPublicCouponListUseCase {

    private final CouponRepository couponRepository;
    private final CouponResponseMapper couponMapper;

    public Page<PublicCouponResponse> execute(Pageable pageable) {
        // Chỉ trả ACTIVE coupon còn hạn
        return couponRepository
                .findByStatusAndEndDateAfter(CouponStatus.ACTIVE, LocalDate.now(), pageable)
                .map(couponMapper::toPublicResponse);
    }
}
