<script setup lang="ts">
import { provide, watch, onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useBooking } from '@/composables/useBooking'
import BookingLayout from '@/layouts/BookingLayout.vue'
import ProgressSteps from '@/components/booking/ProgressSteps.vue'
import BookingSummary from '@/components/booking/BookingSummary.vue'

import CinemaSelection from '@/components/booking/CinemaSelection.vue'
import MovieShowtimeSelection from '@/components/booking/MovieShowtimeSelection.vue'
import SeatSelection from '@/components/booking/SeatSelection.vue'
import ComboSelection from '@/components/booking/ComboSelection.vue'
import PaymentCoupon from '@/components/booking/PaymentCoupon.vue'

import { showtimeApi } from '@/api/showtime.api'
import { movieApi } from '@/api/movie.api'

const STORAGE_KEY = 'booking_state'
const route = useRoute()

const booking = useBooking()
if (!booking.currentStep.value) {
    booking.currentStep.value = 1
}
provide('booking', booking)

const debugInfo = computed(() => ({
    currentStep: booking.currentStep.value,
    hasCinema: !!booking.selectedCinema.value,
    hasShowtime: !!booking.selectedShowtime.value,
    hasSeats: booking.selectedSeats.value.length
}))

// Refs cho các step component
const cinemaRef = ref<InstanceType<typeof CinemaSelection> | null>(null)
const movieShowtimeRef = ref<InstanceType<typeof MovieShowtimeSelection> | null>(null)
const seatRef = ref<InstanceType<typeof SeatSelection> | null>(null)
const comboRef = ref<InstanceType<typeof ComboSelection> | null>(null)
const paymentRef = ref<InstanceType<typeof PaymentCoupon> | null>(null)

const initializeFromShowtime = async (showtimeId: number) => {
    try {
        booking.reset()

        const showtimeRaw = await showtimeApi.getPublicById(showtimeId)
        if (!showtimeRaw) {
            alert('Không tìm thấy suất chiếu')
            return
        }

        // === FIX: Xử lý cả 2 trường hợp (nested movie hoặc chỉ movieId) ===
        let movieData = null
        let cinemaData = null

        // Trường hợp 1: API trả về nested object
        if (showtimeRaw.movie) {
            movieData = showtimeRaw.movie
        }
        // Trường hợp 2: Chỉ có movieId → fetch riêng movie
        else if (showtimeRaw.movieId) {
            try {
                const movieRes = await movieApi.getById(showtimeRaw.movieId)
                movieData = movieRes  // hoặc movieRes.data nếu dùng axios response wrapper
            } catch (e) {
                console.warn('Không tải được thông tin phim chi tiết, dùng fallback')
                movieData = {
                    id: showtimeRaw.movieId,
                    title: `Phim #${showtimeRaw.movieId}`,
                    posterUrl: '',
                    ageRating: 'P' as const,
                }
            }
        }

        // Xử lý cinema tương tự
        if (showtimeRaw.cinema) {
            cinemaData = showtimeRaw.cinema
        } else if (showtimeRaw.cinemaId) {
            // Nếu cần cinema chi tiết thì fetch, hiện tại chỉ cần name nên có thể để sau
            cinemaData = { id: showtimeRaw.cinemaId, name: 'Rạp' }
        }

        // Gán vào booking
        booking.selectedShowtime.value = showtimeRaw
        booking.selectedMovie.value = movieData
        booking.selectedCinema.value = cinemaData

        // Đảm bảo roomId có để load tên phòng
        if (showtimeRaw.roomId) {
            (booking.selectedShowtime.value as any).roomId = showtimeRaw.roomId
        }

        booking.currentStep.value = 3

        console.log('✅ Loaded from showtimeId:', {
            showtime: showtimeRaw,
            movie: movieData
        })

    } catch (err) {
        console.error('Failed to load showtime:', err)
        alert('Không thể tải thông tin suất chiếu.')
    }
}

// Hàm xử lý nút "Tiếp tục" từ BookingSummary
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

// Hàm xử lý nút "Quay lại"
function handlePrev() {
    if (booking.currentStep.value > 1) {
        booking.goToStep(booking.currentStep.value - 1)
    }
}

onMounted(async () => {
    // Ưu tiên xử lý từ URL query (từ Sidebar hoặc ShowtimeCard)
    const showtimeId = route.query.showtimeId ? Number(route.query.showtimeId) : null

    if (showtimeId && !isNaN(showtimeId)) {
        await initializeFromShowtime(showtimeId)

        // Xóa query param để URL sạch
        window.history.replaceState({}, '', '/bookings')
    }
    // Nếu không có query thì khôi phục từ sessionStorage
    else {
        const saved = sessionStorage.getItem(STORAGE_KEY)
        if (saved) {
            try {
                const state = JSON.parse(saved)
                if (state.currentStep) booking.currentStep.value = state.currentStep
                if (state.selectedCinema) booking.selectedCinema.value = state.selectedCinema
                if (state.selectedMovie) booking.selectedMovie.value = state.selectedMovie
                if (state.selectedShowtime) booking.selectedShowtime.value = state.selectedShowtime
                if (state.selectedSeats) booking.selectedSeats.value = state.selectedSeats
                if (state.selectedCombos) booking.selectedCombos.value = state.selectedCombos
                if (state.appliedCoupon) booking.appliedCoupon.value = state.appliedCoupon
            } catch (e) {
                console.error(e)
                sessionStorage.removeItem(STORAGE_KEY)
            }
        }
    }
})

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
</script>

<template>
    <BookingLayout>
        <template #progress>
            <ProgressSteps :current-step="booking.currentStep.value" @update:step="booking.goToStep" />
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