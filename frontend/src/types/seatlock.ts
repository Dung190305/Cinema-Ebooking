// src/types/seatlock.ts
export interface AcquireSeatLockRequest {
  userId: number
  showtimeId: number
  seatIds: number[]
}

export interface AcquireLockResponse {
  success: boolean
  lockedSeats: Array<{
    seatId: number
    seatNumber: string
    expiredAt: string 
  }>
  expiredAt: string
}

export interface ReleaseSeatLockRequest {
  userId: number
  showtimeId: number
}

export interface SeatLockState {
  isLocked: boolean
  lockedSeats: Array<{
    seatId: number
    seatNumber: string
    expiredAt: string
  }>
  expiredAt: string | null
  timeLeft: number // seconds
}