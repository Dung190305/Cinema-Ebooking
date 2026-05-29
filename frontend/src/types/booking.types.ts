export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED'

export interface BookingListItemResponse {
  bookingId: number
  bookingCode: string

  movieId: number
  movieTitle: string
  showtime: string

  finalAmount: number
  status: BookingStatus

  createdAt: string
  paidAt?: string | null
}

export interface BookingSeatInfo {
  showtimeSeatId: number
  seatName: string
  seatType: string
  price: number
}

export interface BookingComboInfo {
  comboId: number
  comboName: string
  quantity: number
  unitPrice: number
  totalPrice: number
}

export interface BookingCouponInfo {
  couponId?: number
  code: string
  discountValue: number
}

export interface BookingDetailResponse {
  bookingId: number
  bookingCode: string

  userId: number
  showtimeId: number
  movieId: number

  movieTitle: string
  cinemaName: string
  roomName: string
  showtimeStartTime: string

  totalTicketPrice: number
  totalComboPrice: number
  tierDiscountAmount: number
  couponDiscountAmount: number
  discountAmount: number
  finalAmount: number

  membershipTierName?: string | null
  membershipDiscountPercent?: number | string | null

  status: BookingStatus

  createdAt: string
  expiredAt?: string | null
  paidAt?: string | null

  seats: BookingSeatInfo[]
  combos: BookingComboInfo[]
  coupon?: BookingCouponInfo | null
}

export interface CreateBookingRequest {
  userId: number;
  showtimeId: number;
  showTimeSeatIds: number[];
  couponCode?: string;
  combos?: Array<{
    comboId: number;
    quantity: number;
  }>;
}

export interface CreateBookingResponse {
  bookingId: number;
  bookingCode: string;
  totalTicketPrice: number;
  totalComboPrice: number;
  tierDiscountAmount: number;
  couponDiscountAmount: number;
  discountAmount: number;
  finalAmount: number;
  membershipTierName?: string | null;
  membershipDiscountPercent?: number | string | null;
  status: BookingStatus;
  expiredAt: string; // LocalDateTime
  showTimeSeatIds: number[];
}
