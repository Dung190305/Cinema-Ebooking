// src/composables/useMovieList.ts
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { movieApi } from '@/api/movie.api'
import type { MovieResponse } from '@/types/movie.types'

export function useMovieList() {
  const route = useRoute()
  const router = useRouter()

  const movies = ref<MovieResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const MAX_SIZE = 50

  // Lấy trạng thái từ URL query, mặc định NOW_SHOWING
  const currentStatus = computed<'NOW_SHOWING' | 'COMING_SOON'>(() => {
    const status = route.query.status as string
    return status === 'COMING_SOON' ? 'COMING_SOON' : 'NOW_SHOWING'
  })

  const fetchMovies = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await movieApi.getList({
        page: 0,             // luôn lấy từ đầu
        size: MAX_SIZE,      // lấy toàn bộ
        status: currentStatus.value,
        sort: 'releaseDate,desc'
      })
      movies.value = response.content
    } catch (err) {
      error.value = 'Không thể tải danh sách phim'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  // Khi status thay đổi → tự động fetch
  watch(currentStatus, () => {
    fetchMovies()
  }, { immediate: true })

  // Hàm chuyển tab (cập nhật URL, không cần page)
  const setStatus = (status: 'NOW_SHOWING' | 'COMING_SOON') => {
    router.replace({ query: { status } })
  }

  return {
    movies,
    loading,
    error,
    currentStatus,
    setStatus,
    fetchMovies
  }
}