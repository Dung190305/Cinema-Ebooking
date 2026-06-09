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

  // Refund amount fields
  originalAmount: number
  refundAmount: number
  refundPercentage: number

  // Status & timestamps
  status: RefundStatus
  requestedAt: string
  processedAt: string | null

  // Reason
  reason: string | null
  adminNote: string | null

  // Booking details (populated via JOIN FETCH — no extra API call needed)
  bookingCode: string | null
  movieTitle: string | null
  cinemaName: string | null
  roomName: string | null
  userId: number | null
  showtimeStartTime: string | null
}

export type RefundPageResponse = NestedPage<RefundResponse>
