package com.cinemaebooking.backend.refund.domain.policy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Component
public class RefundPolicy {

    private static final long HOURS_72 = 72 * 60L;
    private static final long HOURS_24 = 24 * 60L;
    private static final long HOURS_6  = 6  * 60L;
    private static final long HOURS_2  = 2  * 60L;

    /**
     * Tính % hoàn tiền dựa trên thời gian còn lại trước suất chiếu.
     *
     * >= 72 giờ (3 ngày) -> 70%
     * 24-72 giờ          -> 50%
     * 6-24 giờ           -> 25%
     * 2-6 giờ            -> 10%
     * < 2 giờ            -> 0%
     */
    public int calculatePercentage(Instant now, Instant showtimeStartTime) {
        if (now == null || showtimeStartTime == null) {
            return 0;
        }

        long minutesBeforeShowtime = Duration.between(now, showtimeStartTime).toMinutes();

        if (minutesBeforeShowtime >= HOURS_72) {
            return 70;
        }

        if (minutesBeforeShowtime >= HOURS_24) {
            return 50;
        }

        if (minutesBeforeShowtime >= HOURS_6) {
            return 25;
        }

        if (minutesBeforeShowtime >= HOURS_2) {
            return 10;
        }

        return 0;
    }

    public BigDecimal calculateRefundAmount(BigDecimal originalAmount, int refundPercentage) {
        if (originalAmount == null || refundPercentage <= 0) {
            return BigDecimal.ZERO;
        }

        return originalAmount
                .multiply(BigDecimal.valueOf(refundPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public String buildMessage(int refundPercentage) {
        return switch (refundPercentage) {
            case 70  -> "Hủy trước 72 giờ so với suất chiếu, được hoàn 70%.";
            case 50  -> "Hủy từ 24-72 giờ trước suất chiếu, được hoàn 50%.";
            case 25  -> "Hủy từ 6-24 giờ trước suất chiếu, được hoàn 25%.";
            case 10  -> "Hủy từ 2-6 giờ trước suất chiếu, được hoàn 10%.";
            default  -> "Hủy dưới 2 giờ hoặc sau suất chiếu, không đủ điều kiện hoàn tiền.";
        };
    }
}
