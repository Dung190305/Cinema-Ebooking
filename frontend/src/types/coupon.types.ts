// Discount type
export type CouponType = 'PERCENT' | 'FIXED'

// Coupon status (khớp với Java enum CouponStatus)
export type CouponStatus = 'DRAFT' | 'ACTIVE' | 'DISABLED' | 'EXPIRED' | 'OUT_OF_STOCK'

// ─── Shared Base ──────────────────────────────────────────────────────────────

export interface CouponFullUpdatePayload {
  code: string
  type: CouponType
  value: number
  usageLimit: number
  perUserUsage: number
  pointsToRedeem: number
  minimumBookingValue: number
  maximumDiscountAmount: number
  startDate: string
  endDate: string
}

// ─── Create ───────────────────────────────────────────────────────────────────

export interface CreateCouponRequest
  extends CouponFullUpdatePayload {}

// ─── Draft Update ─────────────────────────────────────────────────────────────

export interface UpdateDraftCouponRequest
  extends CouponFullUpdatePayload {}

// ─── Active Coupon Restricted Update ─────────────────────────────────────────

export interface UpdateCouponRequest {
  usageLimit: number
  endDate: string
}