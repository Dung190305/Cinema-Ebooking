import apiClient from '@/api/axios'
import type {
  ShowtimeResponse,
  CreateShowtimeRequest,
  UpdateShowtimeRequest,
  ShowtimeFormatResponse,
  ShowtimeCancelResult,
} from '@/types/showtime'
import type { NestedPage } from '@/types/common.types'
import type { ShowtimeSeatLayoutResponse } from '@/types/showtime-seat'

export const showtimeApi = {
  getPublicShowtimes: (params: {
    cinemaId?: number
    movieId?: number
    roomId?: number
    date?: string
    city?: string
    page?: number
    size?: number
    sort?: string         
    status?: string       
  }) =>
    apiClient.get<NestedPage<ShowtimeResponse>>('/showtimes', { params }),
  
  getPublicById: (id: number) =>
    apiClient.get<ShowtimeResponse>(`/showtimes/${id}`),

  getById: (id: number) =>
    apiClient.get<ShowtimeResponse>(`/admin/showtimes/${id}`),

  getSeatMap: (id: number) =>
    apiClient.get<ShowtimeSeatLayoutResponse>(`/showtimes/${id}/seat-layout`),

  create: (body: CreateShowtimeRequest) =>
    apiClient.post<ShowtimeResponse>('/admin/showtimes', body, { timeout: 60000 }),

  update: (id: number, body: UpdateShowtimeRequest) =>
    apiClient.put<ShowtimeResponse>(`/admin/showtimes/${id}`, body),

  cancel: (id: number) =>
    apiClient.patch<ShowtimeCancelResult>(`/admin/showtimes/${id}/cancel`),
}
