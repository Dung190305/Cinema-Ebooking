import { ref, onMounted } from 'vue'
import { movieApi } from '@/api/movie.api'
import { recommendationApi } from '@/api/recommendation.api'
import type { MovieResponse, MovieStatus } from '@/types/movie.types'
import type { RecommendationMovieResponse } from '@/types/recommendation.types'

type MovieTab = MovieStatus | 'RECOMMENDED'

export function useMovieSection() {
  const movies = ref<MovieResponse[]>([])
  const loading = ref(true)
  const error = ref<string | null>(null)
  const currentStatus = ref<MovieTab>('NOW_SHOWING')
  const recommendationLimit = ref(8)

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

  async function fetchMovies() {
    loading.value = true
    error.value = null

    try {
      if (currentStatus.value === 'RECOMMENDED') {
        const response = await recommendationApi.getMyRecommendations(recommendationLimit.value)

        movies.value = response.map(mapRecommendationToMovie)
        return
      }

      const response = await movieApi.getList({
        page: 0,
        size: 8,
        status: currentStatus.value,
      })

      movies.value = response.content
    } catch (err) {
      console.error('Lỗi khi tải danh sách phim:', err)

      if (currentStatus.value === 'RECOMMENDED') {
        error.value = 'Không thể tải phim dành cho bạn. Vui lòng đăng nhập hoặc thử lại.'
      } else {
        error.value = 'Không thể tải phim. Vui lòng thử lại.'
      }

      movies.value = []
    } finally {
      loading.value = false
    }
  }

  function switchTab(status: MovieTab) {
    if (currentStatus.value === status) return

    currentStatus.value = status

    if (status === 'RECOMMENDED') {
      recommendationLimit.value = 8
    }

    fetchMovies()
  }

  function onBook(id: number) {
    console.log('Đặt vé cho phim ID:', id)
  }

  onMounted(() => {
    fetchMovies()
  })

  return {
    movies,
    loading,
    error,
    currentStatus,
    switchTab,
    onBook,
    retry: fetchMovies,
  }
}
