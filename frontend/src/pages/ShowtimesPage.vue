<template>
  <div class="container mx-auto px-16 py-8">
    <h1 class="text-display font-bold text-text-primary mb-6">Lịch chiếu phim</h1>

    <ShowtimeFilters :cities="cities" :cinemas="filteredCinemas" :selectedCity="selectedCity"
      :selectedCinemaId="selectedCinemaId" :selectedDate="selectedDate" @update:selectedCity="selectedCity = $event"
      @update:selectedCinemaId="selectedCinemaId = $event" @update:selectedDate="selectedDate = $event" class="mb-8" />

    <div class="space-y-8">
      <template v-if="loading">
        <Skeleton :blocks="showtimeListSkeleton(5)" />
      </template>

      <template v-else-if="groupedShowtimes.length === 0">
        <div class="text-center py-12 text-text-secondary">
          Không có suất chiếu nào.
        </div>
      </template>

      <template v-else>
        <div v-for="group in groupedShowtimes" :key="group.date" class="space-y-4">
          <h2 class="text-title font-semibold text-text-primary border-b border-border-default pb-2">
            {{ formatDateHeader(group.date) }}
          </h2>
          <ShowtimeCard v-for="(g, idx) in group.groups" :key="idx" :showtimes="g.showtimes"
            :movie="getMovieById(g.showtimes[0].movieId)" :cinema="getCinemaById(g.showtimes[0].cinemaId)"
            :format="getFormatById(g.showtimes[0].formatId)" @book="handleBook" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ShowtimeFilters from '@/components/showtime/ShowtimeFilters.vue'
import ShowtimeCard from '@/components/showtime/ShowtimeCard.vue'
import Skeleton from '@/components/ui/skeleton/Skeleton.vue'
import { useShowtimes } from '@/composables/useShowtimes'
import { useAuthStore } from '@/stores/auth.store'
import { showtimeApi } from '@/api/showtime.api'
import { showtimeListSkeleton } from '@/skeletons/showtime.skeleton'
import type { ShowtimeFormatResponse } from '@/types/showtime'
import type { CreateBookingRequest } from '@/types/booking.types'
import type { ShowtimeResponse } from '@/types/showtime'
import { formatDateHeaderVN, getDateKeyVN } from '@/utils/dateFormat'

const {
  showtimes,
  loading,
  cities,
  filteredCinemas,
  selectedCity,
  selectedCinemaId,
  selectedDate,
  getCinemaById,
  getMovieById,
} = useShowtimes()

const authStore = useAuthStore()
const router = useRouter()
const formats = ref<ShowtimeFormatResponse[]>([])

const groupedShowtimes = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const dateMap = new Map<string, Map<string, ShowtimeResponse[]>>()

  showtimes.value.forEach(showtime => {
    const startInstant = showtime.startTime as string
    const startDateVN = new Date(startInstant)

    if (isNaN(startDateVN.getTime())) {
      console.error('Invalid startTime:', startInstant)
      return
    }

    // So sánh theo múi giờ VN
    if (startDateVN < today) return

    const dateKey = getDateKeyVN(startInstant)   // ← Dùng hàm này
    const groupKey = [
      showtime.movieId,
      showtime.cinemaId,
      showtime.formatId,
      showtime.audioLanguage,
      showtime.subtitleLanguage,
    ].join('|')

    if (!dateMap.has(dateKey)) dateMap.set(dateKey, new Map())
    const dayMap = dateMap.get(dateKey)!
    if (!dayMap.has(groupKey)) dayMap.set(groupKey, [])
    dayMap.get(groupKey)!.push(showtime)
  })

  return Array.from(dateMap.keys())
    .sort()
    .map(date => ({
      date,
      groups: Array.from(dateMap.get(date)!.values()).map(list => ({
        showtimes: list.sort(
          (a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
        ),
      })),
    }))
})

const formatDateHeader = (dateStr: string) => formatDateHeaderVN(dateStr)

const fetchFormats = async () => {
  try {
    const res = await showtimeApi.getFormats?.()
    if (res) formats.value = res.data
  } catch (error) {
    console.error('Failed to fetch formats', error)
  }
}

const getFormatById = (id: number) => formats.value.find(f => f.id === id)

const handleBook = (showtimeId: number) => {
  if (!authStore.user) {
    alert('Vui lòng đăng nhập để đặt vé')
    return
  }
  const bookingRequest: CreateBookingRequest = {
    userId: authStore.user.id,
    showtimeId,
    showTimeSeatIds: [],
    couponCode: null,
    combos: []
  }
  sessionStorage.setItem('tempBooking', JSON.stringify(bookingRequest))
  router.push({ name: 'booking-seats', params: { showtimeId } })
}

onMounted(() => {
  fetchFormats()
})
</script>