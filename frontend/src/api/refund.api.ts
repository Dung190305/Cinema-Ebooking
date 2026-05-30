import apiClient from '@/api/axios'
import type {
  CreateRefundRequest,
  ProcessRefundRequest,
  RefundCalculationResponse,
  RefundPageResponse,
  RefundResponse,
} from '@/types/refund.types'

export const refundApi = {
  getAdminList: (params?: { page?: number; size?: number; sort?: string }) =>
    apiClient.get<RefundPageResponse>('/admin/refunds', {
      params: {
        page: params?.page ?? 0,
        size: params?.size ?? 10,
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
}
