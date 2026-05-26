import apiClient from '@/api/axios'
import type { DashboardSummaryDto } from '@/types/dashboard.types'

export const dashboardApi = {
  getSummary: (cinemaId?: number | null) =>
    apiClient.get<DashboardSummaryDto>('/admin/analytics/dashboard', {
      params: cinemaId ? { cinemaId } : {},
    }) as Promise<DashboardSummaryDto>,
}
