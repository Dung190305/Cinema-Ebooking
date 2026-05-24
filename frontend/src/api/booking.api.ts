import apiClient from '@/api/axios'
import type { NestedPage } from '@/types/common.types'
import type {
  BookingDetailResponse,
  BookingListItemResponse,
  BookingStatus,
} from '@/types/booking.types'

export const bookingApi = {
  getAdminList: (params: {
    movieId?: number | ''
    status?: BookingStatus | ''
    fromDate?: string
    toDate?: string
    page?: number
    size?: number
    sort?: string
  }) =>
    apiClient.get<NestedPage<BookingListItemResponse>>('/bookings/admin/all', {
      params: {
        movieId: params.movieId || undefined,
        status: params.status || undefined,
        fromDate: params.fromDate || undefined,
        toDate: params.toDate || undefined,
        page: params.page ?? 0,
        size: params.size ?? 10,
        sort: params.sort ?? 'createdAt,desc',
      },
    }) as Promise<NestedPage<BookingListItemResponse>>,

  getListByUser: (params: {
    userId: number
    status?: BookingStatus | ''
    page?: number
    size?: number
    sort?: string
  }) =>
    apiClient.get<NestedPage<BookingListItemResponse>>('/bookings', {
      params: {
        userId: params.userId,
        status: params.status || undefined,
        page: params.page ?? 0,
        size: params.size ?? 10,
        sort: params.sort ?? 'createdAt,desc',
      },
    }) as Promise<NestedPage<BookingListItemResponse>>,

  getById: (id: number) =>
    apiClient.get<BookingDetailResponse>(`/bookings/${id}`) as Promise<BookingDetailResponse>,

  cancel: (id: number) => apiClient.post(`/bookings/${id}/cancel`),
}
