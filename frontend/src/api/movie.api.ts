// src/api/movie.api.ts
import apiClient from './axios'
import type { MovieResponse, CreateMovieRequest, UpdateMovieRequest } from '@/types/movie.types'
import type { NestedPage } from '@/types/common.types';

interface GetMovieListParams {
  page?: number
  size?: number
  sort?: string               // ví dụ: 'releaseDate,desc' hoặc 'rating,asc'
  status?: string             // 'NOW_SHOWING' | 'COMING_SOON'
  ageRating?: string
}

export const movieApi = {
  getList: (params?: GetMovieListParams) => {
    // Tạo object query params, bỏ qua các giá trị undefined
    const queryParams: Record<string, any> = {}
    if (params?.page !== undefined) queryParams.page = params.page
    if (params?.size !== undefined) queryParams.size = params.size
    if (params?.sort) queryParams.sort = params.sort
    if (params?.status) queryParams.status = params.status
    if (params?.ageRating) queryParams.ageRating = params.ageRating

    // Nếu không truyền sort, backend sẽ dùng mặc định releaseDate,desc (đã cấu hình ở @PageableDefault)
    return apiClient.get<NestedPage<MovieResponse>>('/movies', { params: queryParams })
  },

  getById: (id: number) =>
    apiClient.get<MovieResponse>(`/movies/${id}`),

  create: (body: CreateMovieRequest) =>
    apiClient.post<MovieResponse>('/movies', body),

  update: (id: number, body: UpdateMovieRequest) =>
    apiClient.put<MovieResponse>(`/movies/${id}`, body),

  delete: (id: number) =>
    apiClient.delete(`/movies/${id}`),
}