import type { CouponType } from './coupon.types'

export type UserCouponStatus = 'ACTIVE' | 'USED' | 'EXPIRED' | 'REVOKED'

// ─── Response ─────────────────────────────────────────────────────────────────
// Backend cần bổ sung couponCode / couponType / couponValue / couponDescription
// vào UserCouponResponse (join từ Coupon entity) để FE hiển thị đủ thông tin

export interface UserCouponResponse {
  id: number
  userId: number
  couponId: number
  // ── Enriched fields (join từ Coupon) ──────────────────────────────────────
  couponCode: string
  couponType: CouponType
  couponValue: number
  minimumBookingValue: number
  maximumDiscountAmount: number
  // ─────────────────────────────────────────────────────────────────────────
  receivedAt: string
  usageRemain: number
  usedAt: string | null
  expiredAt: string
  status: UserCouponStatus
}

// ─── Requests ─────────────────────────────────────────────────────────────────

// Đổi coupon bằng mã code (pointsToRedeem == 0)
// NOTE: Backend hiện nhận couponId — cần đổi sang couponCode để user nhập mã
export interface RedeemCouponRequest {
  userId: number
  couponCode: string   // thay vì couponId — backend cần lookup Coupon by code
}

// Sử dụng coupon
export interface UseUserCouponRequest {
  userCouponId: number
  userId: number
}

// Khôi phục lượt dùng (khi huỷ booking/payment)
// NOTE: Backend RestoreUserCouponRequest chỉ có userCouponId — userId bị bỏ, FE cũng gửi không có vấn đề gì vì server ignore
export interface RestoreUserCouponRequest {
  userCouponId: number
  userId: number       // server hiện ignore, giữ để FE nhất quán
}

// Thu hồi coupon (admin)
export interface RevokeUserCouponRequest {
  userCouponId: number
  userId: number       // server hiện ignore, giữ để FE nhất quán
}