// ==================== ENUMS ====================
export type BookingStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'CANCELLED'
  | 'EXPIRED'

// ==================== REQUEST DTOs ====================

/** Item combo khi tạo booking (client gửi lên) */
export interface ComboSelectionItem {
  comboId: number
  quantity: number
}

/** Request tạo booking (CreateBookingRequest.java) */
export interface CreateBookingRequest {
  userId: number
  showtimeId: number
  showTimeSeatIds: number[]       // danh sách ID ghế trong suất chiếu
  couponCode: string | null
  combos: ComboSelectionItem[]
}

/** Request tạo booking combo (CreateBookingComboRequest.java) - dùng nếu có API riêng */
export interface CreateBookingComboRequest {
  comboId: number
  comboName: string
  unitPrice: number
  quantity: number
}

/** Request áp dụng coupon (ApplyBookingCouponRequest.java) */
export interface ApplyBookingCouponRequest {
  userCouponId: number | null
  code: string | null
  discountValue: number
}

// ==================== RESPONSE DTOs ====================

/** Response sau khi tạo booking thành công (CreateBookingResponse.java) */
export interface CreateBookingResponse {
  bookingId: number
  bookingCode: string
  totalTicketPrice: number
  totalComboPrice: number
  tierDiscountAmount: number
  couponDiscountAmount: number
  discountAmount: number
  finalAmount: number
  membershipTierName: string | null
  membershipDiscountPercent: number | null
  status: BookingStatus
  expiredAt: string               // ISO datetime
  showTimeSeatIds: number[]
}

/** Thông tin combo trong booking (BookingComboResponse.java) */
export interface BookingComboResponse {
  id: number
  bookingId: number
  comboId: number
  comboName: string
  unitPrice: number
  quantity: number
  totalPrice: number
}

/** Thông tin coupon áp dụng cho booking (BookingCouponResponse.java) */
export interface BookingCouponResponse {
  id: number
  bookingId: number
  userCouponId: number
  code: string
  discountValue: number
  appliedAt: string               // ISO datetime
}

/** Chi tiết ghế trong booking (dùng trong BookingDetailResponse) */
export interface SeatInfo {
  showtimeSeatId: number
  seatName: string                // ví dụ "A12"
  seatType: string                // "VIP", "NORMAL"
  price: number
}

/** Chi tiết combo trong booking (dùng trong BookingDetailResponse) */
export interface ComboInfo {
  comboId: number
  comboName: string
  quantity: number
  unitPrice: number
  totalPrice: number
}

/** Thông tin coupon (dùng trong BookingDetailResponse) */
export interface CouponInfo {
  couponId: number
  code: string
  discountValue: number
}

/** Response chi tiết một booking (BookingDetailResponse.java) */
export interface BookingDetailResponse {
  bookingId: number
  bookingCode: string
  userId: number
  showtimeId: number
  movieTitle: string
  cinemaName: string
  roomName: string
  showtimeStartTime: string       // ISO datetime
  totalTicketPrice: number
  totalComboPrice: number
  tierDiscountAmount: number
  couponDiscountAmount: number
  discountAmount: number
  finalAmount: number
  membershipTierName: string | null
  membershipDiscountPercent: number | null
  status: BookingStatus
  createdAt: string
  expiredAt: string | null
  paidAt: string | null
  seats: SeatInfo[]
  combos: ComboInfo[]
  coupon: CouponInfo | null
}

/** Response danh sách booking rút gọn (BookingListItemResponse.java) */
export interface BookingListItemResponse {
  bookingId: number
  bookingCode: string
  movieTitle: string
  showtime: string                // ISO datetime
  finalAmount: number
  status: BookingStatus
  createdAt: string
  paidAt: string | null
}
