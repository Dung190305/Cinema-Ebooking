// src/api/user-admin.api.ts
import apiClient from './axios'
import type { UserDetailResponse, UserResponse } from '@/types/user.types'

export const userAdminApi = {
  /**
   * GET /api/v1/admin
   * List users with optional search + filter + pagination.
   * Axios interceptor unwraps ApiResponse{data} → returns data directly.
   */
  getList: (params?: {
    page?: number
    size?: number
    sort?: string
    fullName?: string
    email?: string
    role?: string
    status?: string
  }): Promise<{
    content: UserResponse[]
    totalElements: number
    totalPages: number
    size: number
    number: number
  }> => {
    const queryParams: Record<string, unknown> = {}
    if (params?.page !== undefined) queryParams.page = params.page
    if (params?.size !== undefined) queryParams.size = params.size
    if (params?.sort) queryParams.sort = params.sort
    if (params?.fullName) queryParams.fullName = params.fullName
    if (params?.email) queryParams.email = params.email
    if (params?.role) queryParams.role = params.role
    if (params?.status) queryParams.status = params.status

    return apiClient.get('/admin', { params: queryParams })
  },

  /**
   * GET /api/v1/admin/{id}
   * Get enriched user detail with loyalty + booking summary.
   * Axios interceptor unwraps ApiResponse{data} → returns data directly.
   */
  getById: (id: number): Promise<UserDetailResponse> =>
    apiClient.get<UserDetailResponse>(`/admin/${id}`) as unknown as Promise<UserDetailResponse>,

  /**
   * PUT /api/v1/admin/{id}/activate
   * Activate a user account.
   */
  activate: (id: number): Promise<void> =>
    apiClient.put(`/admin/${id}/activate`),

  /**
   * PUT /api/v1/admin/{id}/deactivate
   * Deactivate a user account.
   */
  deactivate: (id: number): Promise<void> =>
    apiClient.put(`/admin/${id}/deactivate`),
}
