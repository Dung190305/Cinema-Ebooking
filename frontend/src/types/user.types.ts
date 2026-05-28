// ── Enums ──────────────────────────────────────────────────────────────────────

export type UserRole = 'USER' | 'ADMIN'

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED'

export type UserGender = 'MALE' | 'FEMALE' | 'OTHER'

// ── Response DTOs ───────────────────────────────────────────────────────────────

export interface UserResponse {
  id: number
  fullName: string
  email: string
  phoneNumber: string | null
  dateOfBirth: string | null  // ISO date string e.g. "2000-05-15"
  gender: UserGender
  avatarUrl: string | null
  role: UserRole
  status: UserStatus
  createdAt: string  // ISO datetime string e.g. "2026-01-15T10:30:00"
}

export interface UserDetailResponse {
  id: number
  fullName: string
  email: string
  phoneNumber: string | null
  dateOfBirth: string | null
  gender: UserGender
  avatarUrl: string | null
  role: UserRole
  status: UserStatus
  createdAt: string
  loyalty: LoyaltySummary | null
  bookings: BookingSummary | null
}

export interface LoyaltySummary {
  loyaltyAccountId: number | null
  membershipTierName: string | null
  loyaltyNumber: string | null
  currentPoints: number | null
  lifetimePoints: number | null
  totalSpending: string  // formatted VND string e.g. "1.500.000 ₫"
  status: string | null
}

export interface BookingSummary {
  totalBookings: number
  totalSpent: string  // formatted VND string e.g. "2.250.000 ₫"
  latestBooking: LatestBooking | null
}

export interface LatestBooking {
  bookingId: number
  bookingCode: string
  movieTitle: string
  cinemaName: string
  roomName: string
  showtimeStartTime: string  // formatted e.g. "15/05/2026, 19:00"
  finalAmount: string        // formatted VND string e.g. "150.000 ₫"
  status: string
  bookedAt?: string
}

// ── Filter params ──────────────────────────────────────────────────────────────

export interface UserFilterParams {
  fullName?: string
  email?: string
  role?: UserRole | ''
  status?: UserStatus | ''
}

// ── API page response (Spring Data Page) ───────────────────────────────────────

export interface UserPageResponse {
  content: UserResponse[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}
