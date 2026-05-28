export type UserCouponStatus = 'ACTIVE' | 'USED' | 'EXPIRED' | 'REVOKED';

export interface UserCouponResponse {
  id: number;
  userId: number;
  couponId: number;
  receivedAt: string;
  usageRemain: number;
  usedAt: string | null;
  expiredAt: string;
  status: UserCouponStatus;
}

// Request redeem (đổi coupon từ code)
export interface RedeemCouponRequest {
  userId: number;
  couponCode: string;
}

// Request sử dụng coupon (đánh dấu đã dùng)
export interface UseUserCouponRequest {
  userCouponId: number;
  userId: number;
}

// Request khôi phục lại lượt dùng (khi huỷ thanh toán)
export interface RestoreUserCouponRequest {
  userCouponId: number;
  userId: number;
}

// Request thu hồi coupon
export interface RevokeUserCouponRequest {
  userCouponId: number;
  userId: number;
}