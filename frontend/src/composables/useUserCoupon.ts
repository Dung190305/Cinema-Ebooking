import { ref, computed } from 'vue';
import { userCouponApi } from '@/api/user-coupon.api';
import { couponApi } from '@/api/coupon.api';
import type { UserCouponResponse } from '@/types/user-coupon.types';
import type { CouponResponse } from '@/types/coupon.types';
import { useAuthStore } from '@/stores/auth.store';

export interface UserCouponWithDetails extends UserCouponResponse {
  coupon: CouponResponse | null;
}

export function useUserCoupon() {
  const authStore = useAuthStore();
  const userId = computed(() => authStore.user?.id ?? 0);

  const userCoupons = ref<UserCouponWithDetails[]>([]);
  const isLoading = ref(false);
  const error = ref('');

  // Lấy danh sách user coupon + gắn thêm thông tin coupon (code, value, type...)
  async function fetchUserCoupons() {
    if (!userId.value) return;
    isLoading.value = true;
    error.value = '';

    try {
      const res = await userCouponApi.getList(userId.value);
      const userCouponList = res.content ?? [];

      // Lấy chi tiết coupon cho từng user coupon
      const enriched = await Promise.all(
        userCouponList.map(async (uc) => {
          try {
            const couponDetail = await couponApi.getById(uc.couponId);
            return { ...uc, coupon: couponDetail };
          } catch {
            return { ...uc, coupon: null };
          }
        })
      );
      userCoupons.value = enriched;
    } catch (err: any) {
      error.value = err?.message || 'Không thể tải danh sách coupon';
    } finally {
      isLoading.value = false;
    }
  }

  // Đổi coupon (chỉ khi pointsToRedeem == 0)
  async function redeemCoupon(couponCode: string): Promise<UserCouponWithDetails | null> {
    if (!userId.value) {
      error.value = 'Chưa đăng nhập';
      return null;
    }

    // Kiểm tra trước coupon có tồn tại và pointsToRedeem == 0 ?
    try {
      const allCoupons = await couponApi.getList(0, 100);
      const found = allCoupons.content?.find(
        (c) => c.code.toLowerCase() === couponCode.toLowerCase() && c.status === 'ACTIVE'
      );
      if (!found) {
        error.value = 'Mã không hợp lệ hoặc chưa kích hoạt';
        return null;
      }
      if (found.pointsToRedeem > 0) {
        error.value = 'Mã này cần điểm đổi thưởng, không thể tự động nhận';
        return null;
      }

      const redeemed = await userCouponApi.redeem(userId.value, couponCode);
      // Tải lại danh sách
      await fetchUserCoupons();
      const newItem = userCoupons.value.find((uc) => uc.id === redeemed.id);
      return newItem || null;
    } catch (err: any) {
      error.value = err?.message || 'Đổi coupon thất bại';
      return null;
    }
  }

  // Sử dụng coupon (đánh dấu đã dùng) – có thể gọi sau khi tạo booking thành công
  async function useCoupon(userCouponId: number): Promise<boolean> {
    if (!userId.value) return false;
    try {
      await userCouponApi.use(userCouponId, userId.value);
      // Cập nhật lại trạng thái trong list
      const idx = userCoupons.value.findIndex((uc) => uc.id === userCouponId);
      if (idx !== -1) {
        userCoupons.value[idx].status = 'USED';
        userCoupons.value[idx].usedAt = new Date().toISOString();
        userCoupons.value[idx].usageRemain -= 1;
      }
      return true;
    } catch (err: any) {
      error.value = err?.message || 'Sử dụng coupon thất bại';
      return false;
    }
  }

  // Khôi phục (khi huỷ thanh toán)
  async function restoreCoupon(userCouponId: number): Promise<boolean> {
    if (!userId.value) return false;
    try {
      await userCouponApi.restore(userCouponId, userId.value);
      await fetchUserCoupons();
      return true;
    } catch {
      return false;
    }
  }

  return {
    userCoupons,
    isLoading,
    error,
    fetchUserCoupons,
    redeemCoupon,
    useCoupon,
    restoreCoupon,
  };
}