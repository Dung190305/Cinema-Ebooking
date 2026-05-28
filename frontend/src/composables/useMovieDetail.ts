// composables/useMovieDetail.ts
import { ref } from 'vue'
import { movieApi } from '@/api/movie.api'
import type { MovieResponse } from '@/types/movie.types'

export function useMovieDetail() {
  const movie = ref<MovieResponse | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchMovie(id: number) {
    loading.value = true
    error.value = null
    try {
        const data  = await movieApi.getById(id)
      movie.value = data
    } catch (err: any) {
      error.value = err?.response?.data?.message || 'Không thể tải thông tin phim.'
    } finally {
      loading.value = false
    }
  }

  return {
    movie,
    loading,
    error,
    fetchMovie
  }
}