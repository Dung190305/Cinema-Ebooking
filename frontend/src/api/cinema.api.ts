// Dùng lại apiClient đã có — interceptor lo token + unwrap ApiResponse + map lỗi
import apiClient from '@/api/axios'
import type {
  CinemaResponse,
  CreateCinemaRequest,
  UpdateCinemaRequest,
} from '@/cinema/types/cinema'
import type { NestedPage } from '@/types/common.types'
export const cinemaApi = {

  // GET /api/v1/cinemas?page=0&size=8
  getList: (page = 0, size = 8) =>
    apiClient.get<NestedPage<CinemaResponse>>(`/cinemas`, { params: { page, size, sort: 'id,desc' } }),

  // GET /api/v1/cinemas?page=0&size=100 (for filter dropdown)
  getAll: (size = 100) =>
    apiClient.get<NestedPage<CinemaResponse>>(`/cinemas`, { params: { page: 0, size } }),

  // GET /api/v1/cinemas/{id}
  getById: (id: number) =>
    apiClient.get<CinemaResponse>(`/cinemas/${id}`),

  // POST /api/v1/cinemas
  create: (body: CreateCinemaRequest) =>
    apiClient.post<CinemaResponse>(`/cinemas`, body),

  // PUT /api/v1/cinemas/{id}
  update: (id: number, body: UpdateCinemaRequest) =>
    apiClient.put<CinemaResponse>(`/cinemas/${id}`, body),

  // DELETE /api/v1/cinemas/{id}  → 204
  delete: (id: number) =>
    apiClient.delete(`/cinemas/${id}`),
}