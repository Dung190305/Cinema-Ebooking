export interface MetricCardDto {
  value: number
  changePercent: number
  label: string
  suffix: string | null
  prefix: string | null
}

export interface HourlyRevenueDto {
  hour: number
  revenue: number
}

export interface TopMovieDto {
  movieTitle: string
  ticketCount: number
  revenue: number
  rank: number
}

export interface RevenueStructureDto {
  ticketRevenue: number
  fnbRevenue: number
  totalRevenue: number
  ticketPercent: number
  fnbPercent: number
}

export interface CinemaStatusDto {
  cinemaId: number
  cinemaName: string
  status: string
  maintenanceNote: string | null
}

export interface UpcomingShowtimeDto {
  showtimeId: number
  movieTitle: string
  screenRoom: string
  cinemaBranch: string
  startTime: string
  bookedSeats: number
  totalSeats: number
  bookedRatio: string
}

export interface DashboardSummaryDto {
  todayRevenue: MetricCardDto
  ticketsSold: MetricCardDto
  occupancyRate: MetricCardDto
  newUsers: MetricCardDto
  hourlyRevenue: HourlyRevenueDto[]
  topMovies: TopMovieDto[]
  revenueStructure: RevenueStructureDto
  cinemaStatuses: CinemaStatusDto[]
  upcomingShowtimes: UpcomingShowtimeDto[]
}
