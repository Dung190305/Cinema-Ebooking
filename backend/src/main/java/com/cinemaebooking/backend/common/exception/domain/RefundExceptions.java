package com.cinemaebooking.backend.common.exception.domain;

import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import com.cinemaebooking.backend.refund.domain.valueobject.RefundId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RefundExceptions {

    public static BaseException notFound(RefundId id) {
        return new BaseException(ErrorCode.REFUND_NOT_FOUND,
                "Không tìm thấy yêu cầu hoàn tiền: " + id.getValue());
    }

    public static BaseException alreadyProcessed(RefundId id) {
        return new BaseException(ErrorCode.REFUND_ALREADY_PROCESSED,
                "Yêu cầu hoàn tiền đã được xử lý: " + id.getValue());
    }

    public static BaseException notEligible(String reason) {
        return new BaseException(ErrorCode.REFUND_NOT_ELIGIBLE, reason);
    }

    public static BaseException failed(String detail) {
        return new BaseException(ErrorCode.REFUND_FAILED,
                "Xử lý hoàn tiền thất bại: " + detail);
    }
}
