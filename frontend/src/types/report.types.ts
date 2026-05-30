export type ReportGroupBy = 'DAY' | 'MONTH' | 'YEAR'

export interface ReportFilterParams {
  fromDate?: string
  toDate?: string
  cinemaId?: number | ''
  movieId?: number | ''
}

export interface RevenueOverviewResponse {
  totalRevenue: number
  totalTicketRevenue: number
  totalComboRevenue: number

  totalBookings: number
  confirmedBookings: number
  pendingBookings: number
  cancelledBookings: number

  totalTicketsSold: number

  averageRevenuePerBooking: number
}

export interface RevenueTrendPointResponse {
  label: string

  revenue: number
  ticketRevenue: number
  comboRevenue: number

  bookingCount: number
  ticketCount: number
}

export interface MoviePerformanceResponse {
  movieId: number | null
  movieTitle: string

  bookingCount: number
  ticketSold: number

  revenue: number
  revenueShare: number
}

export interface ComboSalesReportResponse {
  comboId: number | null
  comboName: string

  quantitySold: number
  totalRevenue: number
}

export interface PaymentMethodReportResponse {
  method: string

  transactionCount: number
  totalAmount: number
  percentage: number
}

export interface CinemaPerformanceResponse {
  cinemaId: number | null
  cinemaName: string

  revenue: number
  bookingCount: number
  ticketSold: number

  occupancyRate: number
}

export interface RoomPerformanceResponse {
  roomId: number | null
  roomName: string
  cinemaName: string

  revenue: number
  bookingCount: number
  ticketSold: number
  showtimeCount: number

  occupancyRate: number
}

export interface GoldenHourResponse {
  dayOfWeek: string
  hour: number

  bookingCount: number
  ticketSold: number
  revenue: number
}

export interface RetentionReportResponse {
  totalCustomers: number
  returningCustomers: number
  oneTimeCustomers: number

  retentionRate: number
  averageBookingsPerCustomer: number
}

export interface PromotionEffectivenessResponse {
  couponCode: string

  usedCount: number
  totalDiscount: number
  revenueGenerated: number
  averageDiscountPerBooking: number
}
