// ─── Showtime Format ──────────────────────────────────────────────────────────
// Seeded: 2D (extraPrice=0), 3D (extraPrice=30000), IMAX (extraPrice=60000)
export type FormatName = '2D' | '3D' | 'IMAX'

export interface ShowtimeFormatResponse {
  id:         number
  name:       FormatName
  extraPrice: number
}

// ─── Showtime ─────────────────────────────────────────────────────────────────
export type ShowtimeStatus = 'SCHEDULED' | 'ONGOING' | 'FINISHED' | 'CANCELLED'

export interface ShowtimeResponse {
  id:               number
  movieId:          number
  roomId:           number
  formatId:         number
  cinemaId:         number
  roomLayoutId:     number
  startTime:        string   // ISO 8601: "2026-05-10T14:00:00"
  endTime:          string
  audioLanguage:    string
  subtitleLanguage: string
  status:           ShowtimeStatus
}

export interface CreateShowtimeRequest {
  movieId:          number
  roomId:           number
  formatId:         number
  startTime:        string
  endTime:          string
  audioLanguage:    string
  subtitleLanguage: string
}

export interface UpdateShowtimeRequest {
  audioLanguage:    string
  subtitleLanguage: string
}

export interface RefundError {
  bookingId: number
  bookingCode: string | null
  reason: string
}
 
export interface ShowtimeCancelResult {
  showtime: ShowtimeResponse
  refundErrors: RefundError[]
}