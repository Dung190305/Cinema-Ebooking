package com.cinemaebooking.backend.booking.domain.model;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking_combo.domain.model.BookingCombo;
import com.cinemaebooking.backend.booking_coupon.domain.model.BookingCoupon;
import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
public class Booking extends BaseEntity<BookingId> {
    private final String bookingCode;
    private final Long userId;
    private final Long showtimeId;
    private final Long movieId;

    private final String movieTitle;
    private final String cinemaName;
    private final String roomName;
    private final Instant showtimeStartTime;
    private final LocalDateTime createdAt;

    /**
     * IDs của ShowtimeSeat đã được validate + lock khi tạo booking.
     * Dùng bởi ConfirmPaymentUseCase để tạo Ticket sau khi thanh toán thành công.
     * Không dùng để hiển thị — dùng tickets sau khi confirm.
     */
    @Builder.Default
    private List<Long> showtimeSeatIds = new ArrayList<>();

    /**
     * Tickets chỉ tồn tại sau khi booking được CONFIRMED (thanh toán xong).
     * Trước đó list này luôn rỗng.
     */
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    @Builder.Default
    private List<BookingCombo> combos = new ArrayList<>();
    private BookingCoupon coupon;

    private BigDecimal totalTicketPrice;
    private BigDecimal totalComboPrice;
    private BigDecimal tierDiscountAmount;
    private BigDecimal couponDiscountAmount;
    private BigDecimal finalAmount;

    private BookingStatus status;
    private final LocalDateTime expiredAt;
    private LocalDateTime paidAt;
    private String membershipTierName;
    private BigDecimal membershipDiscountPercent;

    // -------------------------------------------------------------------------
    // Domain operations
    // -------------------------------------------------------------------------

    /**
     * Chuyển PENDING → CONFIRMED.
     * Chỉ gọi từ ConfirmPaymentUseCase sau khi đã setTickets().
     */
    public void confirm() {
        if (this.status != BookingStatus.PENDING) {
            throw CommonExceptions.invalidInput(
                    "Chỉ booking PENDING mới có thể confirm."
            );
        }
        this.status = BookingStatus.CONFIRMED;
    }

    /**
     * Hủy booking PENDING.
     * Không cần loop cancel từng Ticket vì Ticket chưa tồn tại ở giai đoạn này.
     */
    public void cancel() {
        if (this.status == BookingStatus.CANCELLED) return;

        if (this.status == BookingStatus.CONFIRMED) {
            throw CommonExceptions.invalidInput("Không thể hủy đơn hàng đã thanh toán.");
        }
        this.status = BookingStatus.CANCELLED;
    }

    public boolean isExpired() {
        return status == BookingStatus.PENDING
                && expiredAt != null
                && LocalDateTime.now().isAfter(expiredAt);
    }

    // -------------------------------------------------------------------------
    // Pricing
    // -------------------------------------------------------------------------

    public void applyTierDiscount(BigDecimal discountPercent) {
        this.membershipDiscountPercent = discountPercent;
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
            this.tierDiscountAmount = BigDecimal.ZERO;
            return;
        }
        BigDecimal subTotal = calculateSubtotal();
        BigDecimal tierDiscount = subTotal
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.tierDiscountAmount = tierDiscount.min(subTotal);
    }

    public void applyCoupon(BookingCoupon couponData) {
        if (couponData == null) {
            this.coupon = null;
            this.couponDiscountAmount = BigDecimal.ZERO;
            return;
        }
        this.coupon = couponData;
        this.couponDiscountAmount = couponData.getDiscountValue();
    }

    public void cancelAfterRefund(){
        if (this.status == BookingStatus.CANCELLED) return;

        this.status = BookingStatus.CANCELLED;
    }

    /**
     * Tính subtotal từ totalTicketPrice (set lúc create) + combo.
     * Sau khi confirm, có thể tính lại từ tickets nếu cần.
     */
    public BigDecimal calculateSubtotal() {
        // Khi chưa confirm: totalTicketPrice được set trực tiếp từ CreateBookingUseCase
        BigDecimal ticketSum = (totalTicketPrice != null)
                ? totalTicketPrice
                : BigDecimal.ZERO;

        BigDecimal comboSum = (combos == null) ? BigDecimal.ZERO : combos.stream()
                .map(c -> c.getUnitPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalComboPrice = comboSum;

        return ticketSum.add(comboSum);
    }

    public void calculateTotal() {
        BigDecimal subTotal = calculateSubtotal();
        BigDecimal totalDiscount =
                (couponDiscountAmount != null ? couponDiscountAmount : BigDecimal.ZERO)
                        .add(tierDiscountAmount != null ? tierDiscountAmount : BigDecimal.ZERO);

        this.finalAmount = subTotal.subtract(totalDiscount).max(BigDecimal.ZERO);
    }
}
