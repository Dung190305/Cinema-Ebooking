import { ref, readonly } from 'vue'
import { dashboardApi } from '@/api/dashboard.api'
import type { DashboardSummaryDto } from '@/types/dashboard.types'

export function useDashboard() {
  const data = ref<DashboardSummaryDto | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  async function fetchDashboard(cinemaId?: number | null) {
    isLoading.value = true
    error.value = null
    try {
      data.value = await dashboardApi.getSummary(cinemaId)
    } catch (err: any) {
      error.value = err?.message ?? 'Failed to load dashboard data'
    } finally {
      isLoading.value = false
    }
  }

  return {
    data: readonly(data),
    isLoading: readonly(isLoading),
    error: readonly(error),
    fetchDashboard,
  }
}
