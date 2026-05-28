package com.cinemaebooking.backend.refund.domain.policy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Component
public class RefundPolicy {

    public int calculatePercentage(Instant now, Instant showtimeStartTime) {
        if (now == null || showtimeStartTime == null) {
            return 0;
        }

        long minutesBeforeShowtime = Duration.between(now, showtimeStartTime).toMinutes();

        if (minutesBeforeShowtime >= 24 * 60) {
            return 100;
        }

        if (minutesBeforeShowtime >= 2 * 60) {
            return 50;
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
        if (refundPercentage == 100) {
            return "Hủy trước 24 giờ so với suất chiếu, được hoàn 100%.";
        }

        if (refundPercentage == 50) {
            return "Hủy trong vòng 2-24 giờ trước suất chiếu, được hoàn 50%.";
        }

        return "Hủy dưới 2 giờ hoặc sau suất chiếu, không đủ điều kiện hoàn tiền.";
    }
}
