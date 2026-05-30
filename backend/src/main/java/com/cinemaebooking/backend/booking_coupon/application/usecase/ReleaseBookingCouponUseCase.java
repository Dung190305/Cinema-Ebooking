package com.cinemaebooking.backend.booking_coupon.application.usecase;

import com.cinemaebooking.backend.booking_coupon.application.port.BookingCouponRepository;
import com.cinemaebooking.backend.user_coupon.application.port.UserCouponRepository;
import com.cinemaebooking.backend.user_coupon.domain.enums.UserCouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReleaseBookingCouponUseCase {

    private final BookingCouponRepository bookingCouponRepository;
    private final UserCouponRepository userCouponRepository; // Gọi sang module UserCoupon

    @Transactional
    public void execute(Long bookingId) {
        // 1. Tìm coupon đã áp dụng cho booking này
        bookingCouponRepository.findByBookingId(bookingId).ifPresent(coupon -> {

            // 2. Chuyển trạng thái UserCoupon về ACTIVE (Sẵn sàng sử dụng)
            // Lưu ý: userCouponId là ID của bản ghi sở hữu coupon của User
            userCouponRepository.updateStatus(coupon.getUserCouponId(), UserCouponStatus.ACTIVE);

            // 3. Đánh dấu bản ghi BookingCoupon này là đã hủy/thu hồi
            bookingCouponRepository.delete(coupon);
        });
    }
}
