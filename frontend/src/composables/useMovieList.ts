import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { movieApi } from '@/api/movie.api'
import { recommendationApi } from '@/api/recommendation.api'
import type { MovieResponse } from '@/types/movie.types'
import type { RecommendationMovieResponse } from '@/types/recommendation.types'

type MovieListStatus = 'NOW_SHOWING' | 'COMING_SOON' | 'RECOMMENDED'

export function useMovieList() {
  const route = useRoute()
  const router = useRouter()

  const movies = ref<MovieResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const MAX_SIZE = 50
  const RECOMMENDATION_LIMIT = 10

  const currentStatus = computed<MovieListStatus>(() => {
    const status = route.query.status as string

    if (status === 'COMING_SOON') return 'COMING_SOON'
    if (status === 'RECOMMENDED') return 'RECOMMENDED'

    return 'NOW_SHOWING'
  })

  function mapRecommendationToMovie(movie: RecommendationMovieResponse): MovieResponse {
    return {
      id: movie.id,
      title: movie.title,
      description: movie.description,
      duration: movie.duration,
      ageRating: movie.ageRating,
      releaseDate: movie.releaseDate,
      showingEndDate: movie.showingEndDate ?? null,
      status: movie.status,
      posterUrl: movie.posterUrl ?? '',
      bannerUrl: movie.bannerUrl ?? '',
      trailerUrl: '',
      director: movie.director ?? '',
      actors: movie.actors ?? '',
      genres: movie.genres ?? [],
      rating: movie.rating ?? null,
      ratingCount: movie.ratingCount ?? 0,
    }
  }

  const fetchMovies = async () => {
    loading.value = true
    error.value = null

    try {
      if (currentStatus.value === 'RECOMMENDED') {
        const response = await recommendationApi.getMyRecommendations(RECOMMENDATION_LIMIT)

        movies.value = response.map(mapRecommendationToMovie)
        return
      }

      const response = await movieApi.getList({
        page: 0,
        size: MAX_SIZE,
        status: currentStatus.value,
        sort: 'releaseDate,desc',
      })

      movies.value = response.content
    } catch (err) {
      if (currentStatus.value === 'RECOMMENDED') {
        error.value = 'Không thể tải phim dành cho bạn. Vui lòng đăng nhập hoặc thử lại.'
      } else {
        error.value = 'Không thể tải danh sách phim'
      }

      console.error(err)
      movies.value = []
    } finally {
      loading.value = false
    }
  }

  watch(
    currentStatus,
    () => {
      fetchMovies()
    },
    { immediate: true },
  )

  const setStatus = (status: MovieListStatus) => {
    router.replace({
      query: {
        status,
      },
    })
  }

  return {
    movies,
    loading,
    error,
    currentStatus,
    setStatus,
    fetchMovies,
  }
}
