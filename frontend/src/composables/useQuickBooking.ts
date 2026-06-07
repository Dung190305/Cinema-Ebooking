// composables/useQuickBooking.ts
import { ref, computed } from 'vue'
import { movieApi } from '@/api/movie.api'
import { cinemaApi } from '@/api/cinema.api'
import { showtimeApi } from '@/api/showtime.api'
import type { MovieResponse } from '@/types/movie.types'
import type { CinemaResponse } from '@/types/cinema'
import type { ShowtimeResponse } from '@/types/showtime'
import { dateToISOString, getDateKeyVN } from '@/utils/dateFormat'

export function useQuickBooking() {
  const selectedMovie = ref<MovieResponse | null>(null)
  const selectedCinema = ref<CinemaResponse | null>(null)
  const selectedDate = ref<string | null>(null)      // YYYY-MM-DD (local date)
  const selectedShowtime = ref<ShowtimeResponse | null>(null)

  // Cached data
  const allMovies = ref<MovieResponse[]>([])
  const allCinemas = ref<CinemaResponse[]>([])

  // Showtime cache
  const showtimesByMovie = ref<ShowtimeResponse[]>([])
  const showtimesByMovieCinema = ref<ShowtimeResponse[]>([])
  const showtimesByAll = ref<ShowtimeResponse[]>([])

  // Loading states
  const loadingMovies = ref(false)
  const loadingCinemas = ref(false)
  const loadingDates = ref(false)
  const loadingShowtimes = ref(false)

  // ---------- Computed ----------
  const availableMovies = computed(() => allMovies.value)

  const availableCinemas = computed(() => {
    if (!selectedMovie.value) return []
    const cinemaIds = new Set(showtimesByMovie.value.map(st => st.cinemaId))
    return allCinemas.value.filter(cinema => cinemaIds.has(cinema.id))
  })

  const availableDates = computed(() => {
    if (!selectedMovie.value || !selectedCinema.value) return []
    const datesSet = new Set<string>()
    showtimesByMovieCinema.value.forEach(st => {
      // Get local date key from the showtime's startTime (ISO)
      const localDate = getDateKeyVN(st.startTime)
      datesSet.add(localDate)
    })
    return Array.from(datesSet).sort()
  })

  const availableShowtimes = computed(() => {
    if (!selectedMovie.value || !selectedCinema.value || !selectedDate.value) return []
    // Filter by selected local date
    return showtimesByAll.value.filter(st => getDateKeyVN(st.startTime) === selectedDate.value)
  })

  // ---------- Lazy data loaders ----------
  async function loadMovies() {
    if (allMovies.value.length) return
    loadingMovies.value = true
    try {
      const res = await movieApi.getList({ size: 100, status: 'NOW_SHOWING' })
      allMovies.value = res.content
    } catch (err) {
      console.error('Failed to load movies', err)
    } finally {
      loadingMovies.value = false
    }
  }

  async function loadAllCinemas() {
    if (allCinemas.value.length) return
    loadingCinemas.value = true
    try {
      const res = await cinemaApi.getAll(100)
      allCinemas.value = res.content
    } catch (err) {
      console.error('Failed to load cinemas', err)
    } finally {
      loadingCinemas.value = false
    }
  }

    async function loadShowtimesForMovie(movieId: number) {
    if (!movieId) return
    if (showtimesByMovie.value.length && showtimesByMovie.value[0]?.movieId === movieId) return
    loadingCinemas.value = true
    try {
        const res = await showtimeApi.getPublicShowtimes({
        movieId,
        size: 200,
        status: 'SCHEDULED', 
        })
        showtimesByMovie.value = res.content
    } catch (err) {
        console.error('Failed to load showtimes for movie', err)
        showtimesByMovie.value = []
    } finally {
        loadingCinemas.value = false
    }
    }

    async function loadShowtimesForMovieCinema(movieId: number, cinemaId: number) {
    if (!movieId || !cinemaId) return
    if (showtimesByMovieCinema.value.length &&
        showtimesByMovieCinema.value[0]?.movieId === movieId &&
        showtimesByMovieCinema.value[0]?.cinemaId === cinemaId) return
    loadingDates.value = true
    try {
        const res = await showtimeApi.getPublicShowtimes({
        movieId,
        cinemaId,
        size: 200,
        status: 'SCHEDULED',  // ← thêm filter
        })
        showtimesByMovieCinema.value = res.content
    } catch (err) {
        console.error('Failed to load showtimes for movie+cinema', err)
        showtimesByMovieCinema.value = []
    } finally {
        loadingDates.value = false
    }
    }

    async function loadShowtimesForAll(movieId: number, cinemaId: number, date: string) {
    if (!movieId || !cinemaId || !date) return
    loadingShowtimes.value = true
    try {
        const res = await showtimeApi.getPublicShowtimes({
        movieId,
        cinemaId,
        date: date,      // YYYY-MM-DD
        size: 200,
        status: 'SCHEDULED',  // ← thêm filter
        })
        showtimesByAll.value = res.content
    } catch (err) {
        console.error('Failed to load showtimes for all filters', err)
        showtimesByAll.value = []
    } finally {
        loadingShowtimes.value = false
    }
    }

  async function loadShowtimesForAll(movieId: number, cinemaId: number, date: string) {
    if (!movieId || !cinemaId || !date) return
    loadingShowtimes.value = true
    try {
      // Send date as YYYY-MM-DD (local) – backend should interpret correctly
      const res = await showtimeApi.getPublicShowtimes({
        movieId,
        cinemaId,
        date: date, // date is already YYYY-MM-DD from getDateKeyVN
        size: 200,
        status: 'SCHEDULED',  // ← thêm filter
      })
      showtimesByAll.value = res.content
    } catch (err) {
      console.error('Failed to load showtimes for all filters', err)
      showtimesByAll.value = []
    } finally {
      loadingShowtimes.value = false
    }
  }

  // ---------- Public selection handlers ----------
  async function selectMovie(movie: MovieResponse | null) {
    if (!movie) return
    selectedMovie.value = movie
    selectedCinema.value = null
    selectedDate.value = null
    selectedShowtime.value = null
    showtimesByMovie.value = []
    showtimesByMovieCinema.value = []
    showtimesByAll.value = []
    await loadShowtimesForMovie(movie.id)
  }

  async function selectCinema(cinema: CinemaResponse | null) {
    if (!cinema || !selectedMovie.value) return
    selectedCinema.value = cinema
    selectedDate.value = null
    selectedShowtime.value = null
    showtimesByMovieCinema.value = []
    showtimesByAll.value = []
    await loadShowtimesForMovieCinema(selectedMovie.value.id, cinema.id)
  }

  async function selectDate(date: string | null) {
    if (!date || !selectedMovie.value || !selectedCinema.value) return
    selectedDate.value = date
    selectedShowtime.value = null
    showtimesByAll.value = []
    await loadShowtimesForAll(selectedMovie.value.id, selectedCinema.value.id, date)
  }

  function selectShowtime(showtime: ShowtimeResponse | null) {
    selectedShowtime.value = showtime
  }

  function reset() {
    selectedMovie.value = null
    selectedCinema.value = null
    selectedDate.value = null
    selectedShowtime.value = null
    showtimesByMovie.value = []
    showtimesByMovieCinema.value = []
    showtimesByAll.value = []
  }

  return {
    selectedMovie,
    selectedCinema,
    selectedDate,
    selectedShowtime,
    loadingMovies,
    loadingCinemas,
    loadingDates,
    loadingShowtimes,
    availableMovies,
    availableCinemas,
    availableDates,
    availableShowtimes,
    loadMovies,
    loadAllCinemas,
    loadShowtimesForMovie,
    loadShowtimesForMovieCinema,
    loadShowtimesForAll,
    selectMovie,
    selectCinema,
    selectDate,
    selectShowtime,
    reset,
  }
}