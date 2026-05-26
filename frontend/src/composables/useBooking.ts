
import { ref, computed } from 'vue'
import { bookingApi } from '@/api/booking.api'
import type { CreateBookingRequest } from '@/api/booking.api'
import type { MovieResponse } from '@/types/movie.types'
import type { CinemaResponse } from '@/cinema/types/cinema'
import type { ShowtimeResponse } from '@/types/showtime'
import type { ShowtimeSeatResponse } from '@/types/showtime-seat'
import type {
  BookingComboInfo,
  BookingCouponInfo,
  BookingDetailResponse,
} from '@/types/booking.types'

export type BookingState = ReturnType<typeof useBooking>

export function useBooking() {

  // ── Step navigation ───────────────────────────────────────────────────────

  const currentStep = ref(1)

  function goToStep(step: number) {
    // Clear stale state khi navigate ngược
    if (step < currentStep.value) {
      if (step <= 2) {
        selectedShowtime.value = null
        selectedSeats.value    = []
      }
      if (step <= 1) {
        selectedCinema.value = null
        selectedMovie.value  = null
      }
    }
    currentStep.value = step
  }

  // ── Selection state ───────────────────────────────────────────────────────

  const selectedMovie    = ref<MovieResponse | null>(null)
  const selectedCinema   = ref<CinemaResponse | null>(null)
  const selectedShowtime = ref<ShowtimeResponse | null>(null)
  const selectedSeats = ref<ShowtimeSeatResponse[]>([])
  const selectedCombos   = ref<BookingComboInfo[]>([])
  const appliedCoupon    = ref<BookingCouponInfo | null>(null)

  // ── Computed totals ───────────────────────────────────────────────────────

  const seatTotal   = computed(() => selectedSeats.value.reduce((s, x) => s + x.price, 0))
  const comboTotal  = computed(() => selectedCombos.value.reduce((s, x) => s + x.totalPrice, 0))
  const discount    = computed(() => appliedCoupon.value?.discountValue ?? 0)
  const grandTotal = computed(() => Math.max(0, seatTotal.value + comboTotal.value - discount.value))
  
  const allSeatIds = computed(() =>
    selectedSeats.value.map(s => s.id)
  )

  // ── Create booking ────────────────────────────────────────────────────────

  const creating       = ref(false)
  const createError    = ref('')
  const createdBooking = ref<BookingDetailResponse | null>(null)

  /**
   * Gọi POST /api/v1/bookings
   * @param userId — lấy từ auth store của caller
   */
  async function createBooking(userId: number): Promise<BookingDetailResponse | null> {
    if (!selectedShowtime.value || !selectedSeats.value.length) return null

    creating.value    = true
    createError.value = ''

    // Build payload khớp 1:1 với CreateBookingRequest.java
    const payload: CreateBookingRequest = {
      userId,
      showtimeId:      selectedShowtime.value.id,
      showTimeSeatIds: allSeatIds.value,
      couponCode:      appliedCoupon.value?.code ?? null,       
      combos:          selectedCombos.value.map(c => ({
        comboId:  c.comboId,
        quantity: c.quantity,
      })),
    }
    console.log(payload)
    try {
      const result = await bookingApi.create(payload)
      createdBooking.value = result
      return result
    } catch (err: any) {
      createError.value = err?.message ?? 'Đặt vé thất bại. Vui lòng thử lại.'
      return null
    } finally {
      creating.value = false
    }
  }

  // ── Reset (dùng sau khi booking thành công hoặc thoát flow) ──────────────

  function reset() {
    currentStep.value    = 1
    selectedMovie.value  = null
    selectedCinema.value = null
    selectedShowtime.value = null
    selectedSeats.value  = []
    selectedCombos.value = []
    appliedCoupon.value  = null
    createdBooking.value = null
    createError.value    = ''
  }

  return {
    // Navigation
    currentStep,
    goToStep,

    // Selection
    selectedMovie,
    selectedCinema,
    selectedShowtime,
    selectedSeats,
    selectedCombos,
    appliedCoupon,

    // Totals (readonly computed)
    seatTotal,
    comboTotal,
    discount,
    grandTotal,

    // Create
    creating,
    createError,
    createdBooking,
    createBooking,
    reset,
  }
}