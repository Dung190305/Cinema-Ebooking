// Dùng lại apiClient đã có — interceptor lo token + unwrap ApiResponse + map lỗi
import apiClient from '@/api/axios'

import type { CouponResponse, CreateCouponRequest, UpdateCouponRequest, UpdateDraftCouponRequest } from '@/types/coupon.types'

import type { NestedPage } from '@/types/common.types'

export const couponApi = {
  // GET /api/v1/coupons?page=0&size=10
  getList: (page = 0, size = 10) =>
    apiClient.get<NestedPage<CouponResponse>>(`/coupons`, {
      params: { page, size, sort: 'id,desc' },
    }),

  // GET /api/v1/coupons/{id}
  getById: (id: number) => apiClient.get<CouponResponse>(`/coupons/${id}`),

  // POST /api/v1/coupons
  create: (body: CreateCouponRequest) => apiClient.post<CouponResponse>(`/coupons`, body),

  // PUT /api/v1/coupons/{id}
  update: (id: number, body: UpdateCouponRequest) =>
    apiClient.put<CouponResponse>(`/coupons/${id}`, body),

  // PUT /api/v1/coupons/{id}/draft
  updateDraft: (id: number, body: UpdateDraftCouponRequest) =>
    apiClient.put<CouponResponse>(`/coupons/${id}/draft`, body),

  // PATCH /api/v1/coupons/{id}/activate
  activate: (id: number) => apiClient.patch(`/coupons/${id}/activate`),

  // PATCH /api/v1/coupons/{id}/disable
  disable: (id: number) => apiClient.patch(`/coupons/${id}/disable`),

  // DELETE đã bị comment ở backend, không dùng nữa
}