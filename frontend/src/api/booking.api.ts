import apiClient from '@/api/axios'
import type { NestedPage } from '@/types/common.types'
import type {
  BookingDetailResponse,
  BookingListItemResponse,
  BookingStatus,
  CreateBookingRequest,
  CreateBookingResponse
} from '@/types/booking.types'

interface QRCodeResponse {
  base64Image: string
}

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

  getPendingBooking: (userId: number, showtimeId: number) =>
    apiClient.get('/bookings/pending', { params: { userId, showtimeId } }),

  create: (data: CreateBookingRequest) =>
    apiClient.post<CreateBookingResponse>('/bookings', data) as Promise<CreateBookingResponse>,

  cancel: (id: number) => apiClient.post(`/bookings/${id}/cancel`),

  getMyHistory: (params: {
    movieId?: number | '';
    status?: BookingStatus | '';
    fromDate?: string;
    toDate?: string;
    page?: number;
    size?: number;
    sort?: string;
  }) =>
    apiClient.get<NestedPage<BookingListItemResponse>>('/bookings/me', {
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
  
  
  getQRCode: (id: number) =>
    apiClient.get<QRCodeResponse>(`/bookings/${id}/qr-code`) as Promise<QRCodeResponse>,
}
