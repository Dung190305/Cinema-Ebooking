// src/composables/useHotMovies.ts
import { ref, onMounted } from 'vue'
import { movieApi } from '@/api/movie.api'
import type { MovieResponse } from '@/types/movie.types'

export function useHotMovies() {
  const movies = ref<MovieResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const fetchMovies = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await movieApi.getList({
        page: 0,
        size: 12,                 // lấy 12 phim hot nhất
        status: 'NOW_SHOWING',    // chỉ phim đang chiếu
        sort: 'rating,desc'       // sắp xếp theo đánh giá cao nhất
      })
      movies.value = response.content
    } catch (err) {
      error.value = 'Không thể tải danh sách phim hot'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  return {
    movies,
    loading,
    error,
    fetchMovies
  }
}