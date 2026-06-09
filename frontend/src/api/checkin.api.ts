import apiClient from '@/api/axios'
import type { BookingDetailResponse } from '@/types/booking.types'

export interface TicketCheckInResult {
  ticketId: number
  ticketCode: string
  seatName: string
  seatType: string
  status: string
  price: number
  checkedInAt: string | null
  success: boolean
  message: string
}

export interface BatchCheckInResponse {
  bookingCode: string
  totalTickets: number
  successCount: number
  failCount: number
  tickets: TicketCheckInResult[]
}

export const checkinApi = {
  lookup: (bookingCode: string) =>
    apiClient.get<BookingDetailResponse>(`/checkin/lookup`, {
      params: { code: bookingCode },
    }) as Promise<BookingDetailResponse>,

  checkIn: (bookingCode: string) =>
    apiClient.post<BatchCheckInResponse>(`/checkin/${bookingCode}`) as Promise<BatchCheckInResponse>,
}
