// combo.api.ts
import apiClient from '@/api/axios'
import type {
  ComboResponse,
  CreateComboRequest,
  UpdateComboRequest,
} from '@/types/combo.types'
import type { NestedPage } from '@/types/common.types'

export const comboApi = {
  // GET /api/v1/combos?page=0&size=10
  getList: (page = 0, size = 10) =>
    apiClient.get<NestedPage<ComboResponse>>(`/combos`, {
      params: { page, size, sort: 'id,desc' },
    }),

  // GET /api/v1/combos/{id}
  getById: (id: number) => apiClient.get<ComboResponse>(`/combos/${id}`),

  // POST /api/v1/combos
  create: (body: CreateComboRequest) =>
    apiClient.post<ComboResponse>(`/combos`, body),

  // PUT /api/v1/combos/{id}
  update: (id: number, body: UpdateComboRequest) =>
    apiClient.put<ComboResponse>(`/combos/${id}`, body),

  // DELETE /api/v1/combos/{id}
  delete: (id: number) => apiClient.delete(`/combos/${id}`),
}