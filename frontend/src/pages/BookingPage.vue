<script setup lang="ts">
import { provide, watch, onMounted, onUnmounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBooking } from '@/composables/useBooking'
import { useSeatLock } from '@/composables/useSeatLock'
import { useAuthStore } from '@/stores/auth.store'
import BookingLayout from '@/layouts/BookingLayout.vue'
import ProgressSteps from '@/components/booking/ProgressSteps.vue'
import BookingSummary from '@/components/booking/BookingSummary.vue'

import CinemaSelection from '@/components/booking/CinemaSelection.vue'
import MovieShowtimeSelection from '@/components/booking/MovieShowtimeSelection.vue'
import SeatSelection from '@/components/booking/SeatSelection.vue'
import ComboSelection from '@/components/booking/ComboSelection.vue'
import PaymentCoupon from '@/components/booking/PaymentCoupon.vue'

import { movieApi } from '@/api/movie.api'
import { showtimeApi } from '@/api/showtime.api'

const STORAGE_KEY = 'booking_state'
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const booking = useBooking()
provide('booking', booking)

const seatLock = useSeatLock(() => {
    // Xoá toàn bộ trạng thái booking và quay về bước 1
    resetBookingState()
})
provide('seatLock', seatLock)

// Refs cho các step component
const cinemaRef = ref<InstanceType<typeof CinemaSelection> | null>(null)
const movieShowtimeRef = ref<InstanceType<typeof MovieShowtimeSelection> | null>(null)
const seatRef = ref<InstanceType<typeof SeatSelection> | null>(null)
const comboRef = ref<InstanceType<typeof ComboSelection> | null>(null)
const paymentRef = ref<InstanceType<typeof PaymentCoupon> | null>(null)

function resetBookingState() {
    booking.reset()
    seatLock.reset()
    sessionStorage.removeItem(STORAGE_KEY)
}

/** Khôi phục state từ sessionStorage */
function restoreBookingState() {
    try {
        const saved = sessionStorage.getItem(STORAGE_KEY)
        if (!saved) return
        const state = JSON.parse(saved)

        booking.selectedCinema.value = state.selectedCinema ?? null
        booking.selectedMovie.value = state.selectedMovie ?? null
        booking.selectedShowtime.value = state.selectedShowtime ?? null
        booking.selectedSeats.value = state.selectedSeats ?? []
        booking.selectedCombos.value = state.selectedCombos ?? []
        booking.appliedCoupon.value = state.appliedCoupon ?? null
        if (state.currentStep && state.currentStep >= 1 && state.currentStep <= 6) {
            booking.currentStep.value = state.currentStep
        }
    } catch {
        // Dữ liệu lỗi -> bỏ qua, giữ trạng thái mặc định
    }
}

onMounted(() => {
    const forceReset = sessionStorage.getItem('booking_force_reset') === 'true'
    const showtimeId = route.query.showtimeId ? Number(route.query.showtimeId) : null

    if (forceReset) {
        // Reset hoàn toàn khi bấm nút "Đặt vé ngay"
        resetBookingState()
        sessionStorage.removeItem('booking_force_reset')
    } else if (showtimeId && !isNaN(showtimeId)) {
        // Deep link từ showtime -> khởi tạo mới từ suất chiếu
        resetBookingState()
        initializeFromShowtime(showtimeId)
        window.history.replaceState({}, '', '/bookings')
    } else {
        // Reload hoặc quay lại -> khôi phục state cũ (nếu có)
        restoreBookingState()
    }
})

onUnmounted(() => {
    const userId = auth.user?.id
    const showtimeId = booking.selectedShowtime.value?.id
    const currentStep = booking.currentStep.value

    if (userId && showtimeId && currentStep >= 4 && currentStep <= 5) {
        seatLock.releaseOnBeforeUnload(userId, showtimeId)
    }
})

const initializeFromShowtime = async (showtimeId: number) => {
    try {
        booking.reset()
        seatLock.reset()

        const showtimeRaw = await showtimeApi.getPublicById(showtimeId)
        if (!showtimeRaw) {
            alert('Không tìm thấy suất chiếu')
            return
        }

        let movieData = null
        let cinemaData = null

        if (showtimeRaw.movie) {
            movieData = showtimeRaw.movie
        } else if (showtimeRaw.movieId) {
            try {
                const movieRes = await movieApi.getById(showtimeRaw.movieId)
                movieData = movieRes
            } catch (e) {
                movieData = {
                    id: showtimeRaw.movieId,
                    title: `Phim #${showtimeRaw.movieId}`,
                    posterUrl: '',
                    ageRating: 'P' as const,
                }
            }
        }

        if (showtimeRaw.cinema) {
            cinemaData = showtimeRaw.cinema
        } else if (showtimeRaw.cinemaId) {
            cinemaData = { id: showtimeRaw.cinemaId, name: 'Rạp' }
        }

        booking.selectedShowtime.value = showtimeRaw
        booking.selectedMovie.value = movieData
        booking.selectedCinema.value = cinemaData

        if (showtimeRaw.roomId) {
            (booking.selectedShowtime.value as any).roomId = showtimeRaw.roomId
        }

        booking.currentStep.value = 3

    } catch (err) {
        console.error('Failed to load showtime:', err)
        alert('Không thể tải thông tin suất chiếu.')
    }
}

function handleNext() {
    const step = booking.currentStep.value
    let currentComponent = null
    switch (step) {
        case 1: currentComponent = cinemaRef.value; break
        case 2: currentComponent = movieShowtimeRef.value; break
        case 3: currentComponent = seatRef.value; break
        case 4: currentComponent = comboRef.value; break
        case 5: currentComponent = paymentRef.value; break
    }
    if (currentComponent && typeof currentComponent.next === 'function') {
        currentComponent.next()
    }
}

async function handlePrev() {
    const current = booking.currentStep.value
    const target = current - 1

    // Chỉ release lock khi quay về bước <=3 (rời khỏi vùng cần giữ ghế)
    if (target <= 3 && current >= 4) {
        const userId = auth.user?.id
        const showtimeId = booking.selectedShowtime.value?.id
        if (userId && showtimeId) {
            await seatLock.releaseLocks(userId, showtimeId)
        }
    }

    if (current > 1) {
        booking.goToStep(target)
    }
}
// Watch lưu state vào sessionStorage mỗi khi có thay đổi
watch(
    () => ({
        currentStep: booking.currentStep.value,
        selectedCinema: booking.selectedCinema.value,
        selectedMovie: booking.selectedMovie.value,
        selectedShowtime: booking.selectedShowtime.value,
        selectedSeats: booking.selectedSeats.value,
        selectedCombos: booking.selectedCombos.value,
        appliedCoupon: booking.appliedCoupon.value,
    }),
    (state) => {
        try {
            sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state))
        } catch { }
    },
    { deep: true }
)


watch(
    () => booking.currentStep.value,
    (step) => {
        if (step === 6) {
            sessionStorage.removeItem(STORAGE_KEY)
        }
    }
)

async function handleStepChange(step: number) {
    if (step > booking.currentStep.value) return

    const current = booking.currentStep.value
    // Nếu chuyển từ step >=4 xuống step <=3 thì release lock
    if (current >= 4 && step <= 3) {
        const userId = auth.user?.id
        const showtimeId = booking.selectedShowtime.value?.id
        if (userId && showtimeId) {
            await seatLock.releaseLocks(userId, showtimeId)
        }
    }

    booking.goToStep(step)
}
</script>

<template>
    <BookingLayout>
        <template #progress>
            <ProgressSteps :current-step="booking.currentStep.value" @update:step="handleStepChange" />
        </template>

        <template #main>
            <CinemaSelection ref="cinemaRef" v-if="booking.currentStep.value === 1" @next="booking.goToStep(2)" />
            <MovieShowtimeSelection ref="movieShowtimeRef" v-if="booking.currentStep.value === 2"
                @next="booking.goToStep(3)" @prev="booking.goToStep(1)" />
            <SeatSelection ref="seatRef" v-if="booking.currentStep.value === 3" @next="booking.goToStep(4)"
                @prev="booking.goToStep(2)" />
            <ComboSelection ref="comboRef" v-if="booking.currentStep.value === 4" @next="booking.goToStep(5)"
                @prev="booking.goToStep(3)" />
            <PaymentCoupon ref="paymentRef" v-if="booking.currentStep.value === 5" @prev="booking.goToStep(4)"
                @success="booking.goToStep(6)" />
        </template>

        <template #sidebar>
            <div class="lg:sticky lg:top-6">
                <BookingSummary :current-step="booking.currentStep.value" @prev="handlePrev" @next="handleNext" />
            </div>
        </template>
    </BookingLayout>
</template>