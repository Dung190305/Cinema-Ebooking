// composables/useRefundHistory.ts
import { ref, computed } from 'vue'
import type { RefundResponse, RefundStatus } from '@/types/refund.types'
import { refundApi } from '@/api/refund.api'

export function useRefundHistory() {
  const loading = ref(false)
  const refunds = ref<RefundResponse[]>([])
  const error = ref<string | null>(null)
  const hasLoaded = ref(false)

  // Badge count: chỉ đếm REQUESTED + APPROVED (đang xử lý, cần user quan tâm)
  const pendingCount = computed(
    () =>
      refunds.value.filter(
        (r) => r.status === 'REQUESTED' || r.status === 'APPROVED',
      ).length,
  )

  async function fetchRefunds() {
    loading.value = true
    error.value = null
    try {
      refunds.value = await refundApi.getMyRefunds()
      hasLoaded.value = true
    } catch {
      error.value = 'Không thể tải lịch sử hoàn tiền.'
    } finally {
      loading.value = false
    }
  }

  function refresh() {
    fetchRefunds()
  }

  return {
    loading,
    refunds,
    error,
    hasLoaded,
    pendingCount,
    fetchRefunds,
    refresh,
  }
}