// composables/useBookingDetail.ts
import { ref, computed, watch } from 'vue'
import type { BookingDetailResponse } from '@/types/booking.types'
import { bookingApi } from '@/api/booking.api'
import { refundApi } from '@/api/refund.api'
import type { RefundCalculationResponse } from '@/api/refund.api'

export function useBookingDetail(bookingId: () => number | null) {
  // ── Core state ─────────────────────────────────────────────────────────────
  const loading = ref(false)
  const loadingQR = ref(false)
  const detail = ref<BookingDetailResponse | null>(null)
  const qrBase64 = ref<string | null>(null)
  const error = ref<string | null>(null)

  // ── Cancel/Refund state ────────────────────────────────────────────────────
  const showCancelModal = ref(false)
  const cancelLoading = ref(false)
  const cancelError = ref<string | null>(null)
  const cancelSuccess = ref(false)
  const refundCalc = ref<RefundCalculationResponse | null>(null)
  const refundCalcLoading = ref(false)

  // ── Computed ───────────────────────────────────────────────────────────────
  const hasDiscount = computed(() =>
    detail.value &&
    ((detail.value.tierDiscountAmount ?? 0) > 0 ||
      (detail.value.couponDiscountAmount ?? 0) > 0),
  )

  // Suất chiếu đã diễn ra (showtimeStartTime < now)
  const isShowtimePassed = computed(() => {
    if (!detail.value?.showtimeStartTime) return false
    return new Date(detail.value.showtimeStartTime).getTime() < Date.now()
  })

  // CONFIRMED + suất chiếu chưa diễn ra → mới được hủy
  const canRequestRefund = computed(
    () => detail.value?.status === 'CONFIRMED' && !isShowtimePassed.value,
  )

  // ── Fetch helpers ──────────────────────────────────────────────────────────
  async function fetchDetail(id: number) {
    loading.value = true
    error.value = null
    detail.value = null
    qrBase64.value = null

    try {
      detail.value = await bookingApi.getById(id)
      if (detail.value.status === 'CONFIRMED') {
        fetchQR(id)
      }
    } catch {
      error.value = 'Không thể tải thông tin đơn hàng. Vui lòng thử lại.'
    } finally {
      loading.value = false
    }
  }

  async function fetchQR(id: number) {
    loadingQR.value = true
    try {
      const response = await bookingApi.getQRCode(id)
      qrBase64.value = response.base64Image
    } catch {
      // QR không critical — silent fail
    } finally {
      loadingQR.value = false
    }
  }

  function retry() {
    const id = bookingId()
    if (id !== null) fetchDetail(id)
  }

  // ── Cancel/Refund actions ──────────────────────────────────────────────────
  async function openCancelModal() {
    showCancelModal.value = true
    cancelError.value = null
    cancelSuccess.value = false
    refundCalc.value = null

    const id = bookingId()
    if (id) {
      refundCalcLoading.value = true
      try {
        refundCalc.value = await refundApi.calculateRefundAmount(id)
      } catch {
        // Non-blocking
      } finally {
        refundCalcLoading.value = false
      }
    }
  }

  function closeCancelModal(): boolean {
    showCancelModal.value = false
    cancelError.value = null
    // Trả về true nếu vừa thành công → caller reload + emit
    const wasSuccess = cancelSuccess.value
    if (wasSuccess) {
      const id = bookingId()
      if (id) fetchDetail(id)
    }
    return wasSuccess
  }

  async function confirmCancel(): Promise<boolean> {
    const id = bookingId()
    if (!id) return false

    cancelLoading.value = true
    cancelError.value = null

    try {
      await refundApi.requestRefundAndCancel(id, 'Người dùng yêu cầu hủy vé')
      cancelSuccess.value = true
      return true
    } catch (err: any) {
      const message =
        err?.globalErrors?.[0] ||
        err?.message ||
        'Không thể gửi yêu cầu hủy. Vui lòng thử lại.'
      cancelError.value = message
      return false
    } finally {
      cancelLoading.value = false
    }
  }

  // ── Watch ──────────────────────────────────────────────────────────────────
  watch(
    bookingId,
    (id) => {
      if (id !== null) fetchDetail(id)
    },
    { immediate: true },
  )

  return {
    // state
    loading,
    loadingQR,
    detail,
    qrBase64,
    error,
    showCancelModal,
    cancelLoading,
    cancelError,
    cancelSuccess,
    refundCalc,
    refundCalcLoading,
    // computed
    hasDiscount,
    isShowtimePassed,
    canRequestRefund,
    // actions
    fetchDetail,
    retry,
    openCancelModal,
    closeCancelModal,
    confirmCancel,
  }
}