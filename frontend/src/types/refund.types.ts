import type { NestedPage } from '@/types/common.types'

export type RefundStatus = 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'COMPLETED' | 'CANCELLED'

export interface CreateRefundRequest {
  bookingId: number
  reason?: string | null
}

export interface ProcessRefundRequest {
  adminNote?: string | null
}

export interface RefundCalculationResponse {
  bookingId: number
  originalAmount: number
  refundAmount: number
  refundPercentage: number
  message: string
}

export interface RefundResponse {
  id: number
  bookingId: number
  originalAmount: number
  refundAmount: number
  refundPercentage: number
  status: RefundStatus
  requestedAt: string
  processedAt: string | null
  reason: string | null
  adminNote: string | null
}

export type RefundPageResponse = NestedPage<RefundResponse>
