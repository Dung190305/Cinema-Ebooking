// src/composables/useShowtimes.ts
import { ref, computed, watch, onMounted } from 'vue'
import { showtimeApi } from '@/api/showtime.api'
import { cinemaApi } from '@/api/cinema.api'
import { movieApi } from '@/api/movie.api'
import type { ShowtimeResponse } from '@/types/showtime'
import type { CinemaResponse } from '@/types/cinema'
import type { MovieResponse } from '@/types/movie'

export function useShowtimes() {
  const showtimes = ref<ShowtimeResponse[]>([])
  const cinemas = ref<CinemaResponse[]>([])
  const movies = ref<MovieResponse[]>([])
  const loading = ref(false)

  // Filters
  const selectedCity = ref<string>('')
  const selectedCinemaId = ref<number | null>(null)
  const selectedDate = ref<string>('')  // mặc định rỗng => lấy tất cả

  const cities = computed(() => {
    const citySet = new Set(cinemas.value.map(c => c.city))
    return Array.from(citySet).sort()
  })

  const filteredCinemas = computed(() => {
    if (!selectedCity.value) return cinemas.value
    return cinemas.value.filter(c => c.city === selectedCity.value)
  })

  const fetchMasterData = async () => {
    try {
      const [cinemasRes, moviesRes] = await Promise.all([
        cinemaApi.getList(0, 100),
        movieApi.getList({ size: 100 })
      ])
      cinemas.value = cinemasRes.content
      movies.value = moviesRes.content
    } catch (error) {
      console.error('Failed to fetch master data', error)
    }
  }

  const fetchShowtimes = async () => {
    loading.value = true
    try {
        const params: any = {
        page: 0,
        size: 200,
        sort: 'startTime,asc'
        }

        if (selectedCinemaId.value != null) {
        params.cinemaId = selectedCinemaId.value
        } else if (selectedCity.value) {
        params.city = selectedCity.value
        }

        // Nếu có selectedDate → gửi theo định dạng YYYY-MM-DD (backend sẽ xử lý theo VN)
        if (selectedDate.value) {
        params.date = selectedDate.value
        }

        const res = await showtimeApi.getPublicShowtimes(params)
        showtimes.value = res.content
    } catch (error) {
        console.error('Failed to fetch showtimes', error)
        showtimes.value = []
    } finally {
        loading.value = false
    }
    }

  // Reset cinema khi đổi thành phố
  watch(selectedCity, () => {
    selectedCinemaId.value = null
    fetchShowtimes()   // fetch lại khi đổi thành phố
  })

  // Fetch khi đổi rạp hoặc ngày
  watch([selectedCinemaId, selectedDate], () => {
    fetchShowtimes()
  })

  const getCinemaById = (id: number) => cinemas.value.find(c => c.id === id)
  const getMovieById = (id: number) => movies.value.find(m => m.id === id)

  onMounted(async () => {
    await fetchMasterData()
    await fetchShowtimes()
  })

  return {
    showtimes,
    loading,
    cities,
    filteredCinemas,
    selectedCity,
    selectedCinemaId,
    selectedDate,
    getCinemaById,
    getMovieById,
  }
}