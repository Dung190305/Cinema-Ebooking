import { ref, computed } from 'vue'
import { couponApi } from '@/api/coupon.api'
import { userCouponApi } from '@/api/user-coupon.api'
import { useAuthStore } from '@/stores/auth.store'
import type { PublicCouponResponse } from '@/types/coupon.types'

export function useCouponBrowse() {
  const auth = useAuthStore()

  // ─── State ────────────────────────────────────────────────────────────────
  const coupons = ref<PublicCouponResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const page = ref(0)
  const totalPages = ref(0)
  const pageSize = 12

  // Redeem by code (manual input)
  const redeemCode = ref('')
  const redeemLoading = ref(false)
  const redeemError = ref<string | null>(null)
  const redeemSuccess = ref(false)

  // ─── Computed ─────────────────────────────────────────────────────────────
  const hasMore = computed(() => page.value < totalPages.value - 1)

  // ─── Actions ──────────────────────────────────────────────────────────────
  async function fetchCoupons(reset = false) {
    if (reset) {
      page.value = 0
      coupons.value = []
    }
    loading.value = true
    error.value = null
    try {
        const res = await couponApi.getPublicList(page.value, pageSize)
        console.log(res)
      coupons.value = reset
        ? res.content
        : [...coupons.value, ...res.content]
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
    await fetchCoupons()
  }

  async function redeemByCode() {
    if (!redeemCode.value.trim()) return
    if (!auth.user) {
      redeemError.value = 'Vui lòng đăng nhập để đổi coupon'
      return
    }
    redeemLoading.value = true
    redeemError.value = null
    redeemSuccess.value = false
    try {
      await userCouponApi.redeem(auth.user.id, redeemCode.value.trim().toUpperCase())
      redeemSuccess.value = true
      redeemCode.value = ''
      // Refresh public list để cập nhật remainingSlots
      await fetchCoupons(true)
    } catch (e: any) {
      const status = e?.response?.status
      if (status === 404) redeemError.value = 'Mã coupon không tồn tại'
      else if (status === 409) redeemError.value = 'Bạn đã sở hữu coupon này rồi'
      else if (status === 410) redeemError.value = 'Coupon đã hết lượt đổi'
      else redeemError.value = e?.message ?? 'Đổi coupon thất bại'
    } finally {
      redeemLoading.value = false
    }
  }

  async function redeemCoupon(coupon: PublicCouponResponse) {
    if (!auth.user) {
      redeemError.value = 'Vui lòng đăng nhập để đổi coupon'
      return
    }
    redeemLoading.value = true
    redeemError.value = null
    redeemSuccess.value = false
    try {
      await userCouponApi.redeem(auth.user.id, coupon.code)
      redeemSuccess.value = true
      await fetchCoupons(true)
    } catch (e: any) {
      const status = e?.response?.status
      if (status === 409) redeemError.value = `Bạn đã sở hữu coupon "${coupon.code}"`
      else if (status === 410) redeemError.value = 'Coupon đã hết lượt đổi'
      else redeemError.value = e?.message ?? 'Đổi coupon thất bại'
    } finally {
      redeemLoading.value = false
    }
  }

  function clearRedeemState() {
    redeemError.value = null
    redeemSuccess.value = false
  }

  return {
    coupons,
    loading,
    error,
    hasMore,
    redeemCode,
    redeemLoading,
    redeemError,
    redeemSuccess,
    fetchCoupons,
    loadMore,
    redeemByCode,
    redeemCoupon,
    clearRedeemState,
  }
}