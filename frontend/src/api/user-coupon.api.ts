import apiClient from './axios';
import type {
  UserCouponResponse,
  RedeemCouponRequest,
  UseUserCouponRequest,
  RestoreUserCouponRequest,
  RevokeUserCouponRequest,
} from '@/types/user-coupon.types';
import type { NestedPage } from '@/types/common.types';

export const userCouponApi = {
  // Lấy danh sách coupon của user (phân trang)
  getList(userId: number, page = 0, size = 20) {
    return apiClient.get<NestedPage<UserCouponResponse>>('/user-coupons', {
      params: { userId, page, size, sort: 'receivedAt,desc' },
    });
  },

  // Đổi coupon từ mã code (pointsToRedeem == 0 mới cho phép)
  redeem(userId: number, couponCode: string) {
    return apiClient.post<UserCouponResponse>('/user-coupons/redeem', {
      userId,
      couponCode,
    } as RedeemCouponRequest);
  },

  // Sử dụng coupon (tiêu hao 1 lượt)
  use(userCouponId: number, userId: number) {
    return apiClient.post<UserCouponResponse>('/user-coupons/use', {
      userCouponId,
      userId,
    } as UseUserCouponRequest);
  },

  // Khôi phục lượt dùng (khi huỷ booking/payment)
  restore(userCouponId: number, userId: number) {
    return apiClient.post<UserCouponResponse>('/user-coupons/restore', {
      userCouponId,
      userId,
    } as RestoreUserCouponRequest);
  },

  // Thu hồi coupon (admin)
  revoke(userCouponId: number, userId: number) {
    return apiClient.post<UserCouponResponse>('/user-coupons/revoke', {
      userCouponId,
      userId,
    } as RevokeUserCouponRequest);
  },
};