// src/api/review.api.ts
import apiClient from './axios'
import type { ReviewResponse, CreateReviewRequest, UpdateReviewRequest, MyReviewResponse, TicketCheckResponse } from '@/types/review.types'
import type { NestedPage } from '@/types/common.types'

interface GetReviewsParams {
  page?: number
  size?: number
  sort?: string
}

export const reviewApi = {
  getByMovieId: (movieId: number, params?: GetReviewsParams) => {
    const queryParams: Record<string, any> = {}
    if (params?.page !== undefined) queryParams.page = params.page
    if (params?.size !== undefined) queryParams.size = params.size
    if (params?.sort) queryParams.sort = params.sort

    return apiClient.get<NestedPage<ReviewResponse>>(`/reviews/movies/${movieId}`, {
      params: queryParams,
    })
  },

  getByUserId: (userId: number, params?: GetReviewsParams) => {
    const queryParams: Record<string, any> = {}
    if (params?.page !== undefined) queryParams.page = params.page
    if (params?.size !== undefined) queryParams.size = params.size
    if (params?.sort) queryParams.sort = params.sort

    return apiClient.get<NestedPage<ReviewResponse>>(`/reviews/users/${userId}`, {
      params: queryParams,
    })
  },

  getById: (id: number) =>
    apiClient.get<ReviewResponse>(`/reviews/${id}`),

  create: (body: CreateReviewRequest) =>
    apiClient.post<ReviewResponse>('/reviews', body),

  update: (id: number, body: UpdateReviewRequest) =>
    apiClient.put<ReviewResponse>(`/reviews/${id}`, body),

  delete: (id: number, userId: number) =>
    apiClient.delete(`/reviews/${id}`, { params: { userId } }),

  getMyReview: (movieId: number, userId: number) =>
    apiClient.get<MyReviewResponse>(`/reviews/movies/${movieId}/my-review`, {
      params: { userId },
    }),

  checkTicket: (movieId: number, userId: number) =>
    apiClient.get<TicketCheckResponse>(`/reviews/movies/${movieId}/check-ticket`, {
      params: { userId },
    }),
}
