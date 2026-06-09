import apiClient from '@/api/axios'
import type {
  CreateRefundRequest,
  ProcessRefundRequest,
  RefundCalculationResponse,
  RefundPageResponse,
  RefundResponse,
} from '@/types/refund.types'

export const refundApi = {
  getAdminList: (params?: { status?: string; page?: number; size?: number; sort?: string }) =>
    apiClient.get<RefundPageResponse>('/admin/refunds', {
      params: {
        status: params?.status || undefined,
        page: params?.page ?? 0,
        size: params?.size ?? 20,
        sort: params?.sort ?? 'requestedAt,desc',
      },
    }) as Promise<RefundPageResponse>,

  getById: (id: number) =>
    apiClient.get<RefundResponse>(`/admin/refunds/${id}`) as Promise<RefundResponse>,

  calculate: (bookingId: number) =>
    apiClient.get<RefundCalculationResponse>('/refunds/calculate', {
      params: { bookingId },
    }) as Promise<RefundCalculationResponse>,

  create: (body: CreateRefundRequest) =>
    apiClient.post<RefundResponse>('/refunds', body) as Promise<RefundResponse>,

  cancel: (id: number) =>
    apiClient.post<RefundResponse>(`/refunds/${id}/cancel`) as Promise<RefundResponse>,

  approve: (id: number, body?: ProcessRefundRequest) =>
    apiClient.post<RefundResponse>(
      `/admin/refunds/${id}/approve`,
      body ?? {},
    ) as Promise<RefundResponse>,

  reject: (id: number, body?: ProcessRefundRequest) =>
    apiClient.post<RefundResponse>(
      `/admin/refunds/${id}/reject`,
      body ?? {},
    ) as Promise<RefundResponse>,

  complete: (id: number, body?: ProcessRefundRequest) =>
    apiClient.post<RefundResponse>(
      `/admin/refunds/${id}/complete`,
      body ?? {},
    ) as Promise<RefundResponse>,
  
  
    /**
   * User yêu cầu hủy booking CONFIRMED + tạo refund request.
   * Endpoint: POST /bookings/{id}/request-refund
   */
  requestRefundAndCancel: (bookingId: number, reason?: string) =>
    apiClient.post<RefundResponse>(`/bookings/${bookingId}/request-refund`, {
      reason: reason ?? null,
    }) as Promise<RefundResponse>,
 
  /**
   * Xem chi tiết refund theo bookingId.
   */
  getByBookingId: (bookingId: number) =>
    apiClient.get<RefundResponse>(`/refunds/by-booking/${bookingId}`) as Promise<RefundResponse>,
 
  /**
   * Tính số tiền dự kiến hoàn trước khi user xác nhận hủy.
   */
  calculateRefundAmount: (bookingId: number) =>
    apiClient.get<RefundCalculationResponse>(`/refunds/calculate`, {
      params: { bookingId },
    }) as Promise<RefundCalculationResponse>,
  
  getMyRefunds: () =>
    apiClient.get<RefundResponse[]>('/refunds/me') as Promise<RefundResponse[]>,
}

export interface RefundCalculationResponse {
  bookingId: number
  originalAmount: number
  refundAmount: number
  refundPercentage: number
  message: string
}
 
