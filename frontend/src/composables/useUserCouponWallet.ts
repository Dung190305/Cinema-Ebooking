// composables/useUserCouponWallet.ts
import { ref, computed } from 'vue'
import { userCouponApi } from '@/api/user-coupon.api'
import { couponApi } from '@/api/coupon.api'         
import { useAuthStore } from '@/stores/auth.store'
import type { UserCouponResponse, UserCouponStatus } from '@/types/user-coupon.types'

export function useUserCouponWallet() {
  const auth = useAuthStore()

  // ─── State ────────────────────────────────────────────────────────────────
  const coupons = ref<UserCouponResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const page = ref(0)
  const totalPages = ref(0)
  const pageSize = 10

  const activeFilter = ref<UserCouponStatus | 'ALL'>('ALL')

  // ─── Computed ─────────────────────────────────────────────────────────────
  const hasMore = computed(() => page.value < totalPages.value - 1)

  const filteredCoupons = computed(() => {
    if (activeFilter.value === 'ALL') return coupons.value
    return coupons.value.filter((c) => c.status === activeFilter.value)
  })

  const activeCoupons = computed(() => coupons.value.filter((c) => c.status === 'ACTIVE'))
  const expiredCount = computed(
    () => coupons.value.filter((c) => c.status === 'EXPIRED' || c.status === 'REVOKED').length,
  )

  // ─── Actions (giữ nguyên) ────────────────────────────────────────────────
  async function fetchWallet(reset = false) {
    if (!auth.user) return
    if (reset) {
      page.value = 0
      coupons.value = []
    }
    loading.value = true
    error.value = null
    try {
      const res = await userCouponApi.getList(auth.user.id, page.value, pageSize)
      coupons.value = reset ? res.content : [...coupons.value, ...res.content]
      totalPages.value = res.totalPages
    } catch (e: any) {
      error.value = e?.message ?? 'Không thể tải danh sách coupon'
    } finally {
      loading.value = false
    }
  }

  async function loadMore() {
    if (!hasMore.value || loading.value) return
    page.value++
    await fetchWallet()
  }

  function setFilter(filter: UserCouponStatus | 'ALL') {
    activeFilter.value = filter
  }

  async function fetchAllCoupons() {
    if (!auth.user) return
    loading.value = true
    error.value = null
    try {
      const res = await userCouponApi.getList(auth.user.id, 0, 999) // pageSize lớn
      coupons.value = res.content ?? []
      totalPages.value = res.totalPages
    } catch (e: any) {
      error.value = e?.message ?? 'Không thể tải danh sách coupon'
    } finally {
      loading.value = false
    }
  }

  // Đổi coupon bằng mã (pointsToRedeem == 0)
  async function redeemCoupon(couponCode: string): Promise<UserCouponResponse | null> {
    if (!auth.user) {
      error.value = 'Chưa đăng nhập'
      return null
    }
    try {
      // Kiểm tra coupon tồn tại và pointsToRedeem == 0
      const allCoupons = await couponApi.getList(0, 100)
      const found = allCoupons.content?.find(
        (c) => c.code.toLowerCase() === couponCode.toLowerCase() && c.status === 'ACTIVE'
      )
      if (!found) {
        error.value = 'Mã không hợp lệ hoặc chưa kích hoạt'
        return null
      }
      if (found.pointsToRedeem > 0) {
        error.value = 'Mã này cần điểm đổi thưởng, không thể tự động nhận'
        return null
      }

      const redeemed = await userCouponApi.redeem(auth.user.id, couponCode)
      // Tải lại danh sách
      await fetchAllCoupons()
      return redeemed
    } catch (err: any) {
      error.value = err?.message || 'Đổi coupon thất bại'
      return null
    }
  }

  // Sử dụng coupon (đánh dấu đã dùng)
  async function useCoupon(userCouponId: number): Promise<boolean> {
    if (!auth.user) return false
    try {
      await userCouponApi.use(userCouponId, auth.user.id)
      // Cập nhật trạng thái trong list
      const idx = coupons.value.findIndex((uc) => uc.id === userCouponId)
      if (idx !== -1) {
        coupons.value[idx].status = 'USED'
        coupons.value[idx].usedAt = new Date().toISOString()
        coupons.value[idx].usageRemain -= 1
      }
      return true
    } catch (err: any) {
      error.value = err?.message || 'Sử dụng coupon thất bại'
      return false
    }
  }

  // Khôi phục lượt dùng (khi huỷ booking)
  async function restoreCoupon(userCouponId: number): Promise<boolean> {
    if (!auth.user) return false
    try {
      await userCouponApi.restore(userCouponId, auth.user.id)
      await fetchAllCoupons() // reload lại toàn bộ
      return true
    } catch {
      return false
    }
  }

  // Preview (5 coupon đang active) – giữ nguyên
  const previews = ref<UserCouponResponse[]>([])
  const loadingPreview = ref(false)
  const fetchedPreview = ref(false)

  async function fetchPreview() {
    if (!auth.user || fetchedPreview.value) return
    loadingPreview.value = true
    try {
      const res = await userCouponApi.getActiveList(auth.user.id, 0, 5)
      previews.value = res.content
      fetchedPreview.value = true
    } catch {
      // silent
    } finally {
      loadingPreview.value = false
    }
  }

  function resetPreview() {
    previews.value = []
    fetchedPreview.value = false
  }

  return {
    // State
    coupons,
    loading,
    error,
    hasMore,
    activeFilter,
    filteredCoupons,
    activeCoupons,
    expiredCount,

    fetchWallet,
    loadMore,
    setFilter,
    fetchAllCoupons,
    redeemCoupon,
    useCoupon,
    restoreCoupon,
    // Preview
    previews,
    loadingPreview,
    fetchPreview,
    resetPreview,
  }
}