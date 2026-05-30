import apiClient from './axios'
import type {
  CinemaPerformanceResponse,
  ComboSalesReportResponse,
  GoldenHourResponse,
  MoviePerformanceResponse,
  PaymentMethodReportResponse,
  PromotionEffectivenessResponse,
  RefundReportResponse,
  ReportFilterParams,
  ReportGroupBy,
  RetentionReportResponse,
  RevenueOverviewResponse,
  RevenueTrendPointResponse,
  RoomPerformanceResponse,
} from '@/types/report.types'

interface RevenueTrendParams extends ReportFilterParams {
  groupBy?: ReportGroupBy
}

function cleanParams(params?: ReportFilterParams | RevenueTrendParams) {
  const queryParams: Record<string, string | number> = {}

  if (!params) return queryParams

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      queryParams[key] = value as string | number
    }
  })

  return queryParams
}

export const reportApi = {
  getOverview: (params?: ReportFilterParams) =>
    apiClient.get<RevenueOverviewResponse>('/admin/reports/overview', {
      params: cleanParams(params),
    }) as unknown as Promise<RevenueOverviewResponse>,

  getRevenueTrend: (params?: RevenueTrendParams) =>
    apiClient.get<RevenueTrendPointResponse[]>('/admin/reports/revenue-trend', {
      params: cleanParams(params),
    }) as unknown as Promise<RevenueTrendPointResponse[]>,

  getMoviePerformance: (params?: ReportFilterParams) =>
    apiClient.get<MoviePerformanceResponse[]>('/admin/reports/movie-performance', {
      params: cleanParams(params),
    }) as unknown as Promise<MoviePerformanceResponse[]>,

  getComboSales: (params?: ReportFilterParams) =>
    apiClient.get<ComboSalesReportResponse[]>('/admin/reports/combo-sales', {
      params: cleanParams(params),
    }) as unknown as Promise<ComboSalesReportResponse[]>,

  getPaymentMethods: (params?: ReportFilterParams) =>
    apiClient.get<PaymentMethodReportResponse[]>('/admin/reports/payment-methods', {
      params: cleanParams(params),
    }) as unknown as Promise<PaymentMethodReportResponse[]>,

  getCinemaPerformance: (params?: ReportFilterParams) =>
    apiClient.get<CinemaPerformanceResponse[]>('/admin/reports/cinema-performance', {
      params: cleanParams(params),
    }) as unknown as Promise<CinemaPerformanceResponse[]>,

  getRoomPerformance: (params?: ReportFilterParams) =>
    apiClient.get<RoomPerformanceResponse[]>('/admin/reports/room-performance', {
      params: cleanParams(params),
    }) as unknown as Promise<RoomPerformanceResponse[]>,

  getGoldenHours: (params?: ReportFilterParams) =>
    apiClient.get<GoldenHourResponse[]>('/admin/reports/golden-hours', {
      params: cleanParams(params),
    }) as unknown as Promise<GoldenHourResponse[]>,

  getRetention: (params?: ReportFilterParams) =>
    apiClient.get<RetentionReportResponse>('/admin/reports/retention', {
      params: cleanParams(params),
    }) as unknown as Promise<RetentionReportResponse>,

  getPromotionEffectiveness: (params?: ReportFilterParams) =>
    apiClient.get<PromotionEffectivenessResponse[]>('/admin/reports/promotion-effectiveness', {
      params: cleanParams(params),
    }) as unknown as Promise<PromotionEffectivenessResponse[]>,

  getRefundReport: (params?: ReportFilterParams) =>
    apiClient.get<RefundReportResponse>('/admin/reports/refunds', {
      params: cleanParams(params),
    }) as unknown as Promise<RefundReportResponse>,
}
