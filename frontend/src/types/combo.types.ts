// combo.types.ts
// Enums matching backend enums

export type ComboStatus = 'ACTIVE' | 'INACTIVE'

export type ComboDisplayStatus = 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK'

// ─── Response DTO ─────────────────────────────────────────────────────────

export interface ComboResponse {
  id: number
  name: string
  description: string
  price: number           // BigDecimal → number
  originalPrice: number
  stock: number
  imageUrl: string
  status: ComboStatus
  displayStatus: ComboDisplayStatus
}

// ─── Create Request ──────────────────────────────────────────────────────

export interface CreateComboRequest {
  name: string
  description: string
  price: number
  originalPrice: number
  stock: number
  imageUrl: string
}

// ─── Update Request (full update except originalPrice) ───────────────────

export interface UpdateComboRequest {
  name: string
  description: string
  price: number
  stock: number
  imageUrl: string
  status: ComboStatus
}