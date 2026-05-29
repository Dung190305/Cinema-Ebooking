<template>
  <div
    class="min-h-screen bg-surface-dark text-text-primary selection:bg-accent selection:text-white"
  >
    <MovieTrailer :trailer-url="displayTrailerUrl" :title="movie?.title || 'Phim mẫu'" />

    <div v-if="movie" class="px-4 pb-8 xl:px-16 xl:pb-16 relative z-10">
      <div class="max-w-328 grid grid-cols-1 lg:grid-cols-12 gap-4 xl:gap-8">
        <div class="lg:col-span-3">
          <MovieCard
            :movie="movie"
            hide-overlay
            hide-badges
            no-redirect
            hide-title
            class="max-w-none cursor-default shadow-2xl shadow-black/60 -translate-y-4 xl:-translate-y-10"
          />
        </div>

        <div class="lg:col-span-5 space-y-2 py-2 xl:space-y-4 xl:py-4">
          <h1 class="text-2xl font-bold xl:text-display">{{ movie.title }}</h1>

          <div class="flex gap-2 items-center">
            <AgeRatingTag :rating="movie.ageRating" />

            <button
              @click="isReviewOpen = true"
              class="flex items-center gap-2 bg-overlay-light-20 hover:bg-white/10 border border-white/5 px-3 py-1 rounded-full transition-all duration-200 group active:scale-95 text-yellow-400"
              title="Xem tất cả đánh giá"
            >
              <div class="flex items-center gap-1.5">
                <svg
                  class="w-5 h-5 fill-current group-hover:scale-110 transition-transform"
                  viewBox="0 0 20 20"
                >
                  <path
                    d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.957a1 1 0 00.95.69h4.162c.969 0 1.371 1.24.588 1.81l-3.37 2.448a1 1 0 00-.364 1.118l1.287 3.957c.3.921-.755 1.688-1.54 1.118l-3.37-2.448a1 1 0 00-1.175 0l-3.37 2.448c-.784.57-1.838-.197-1.539-1.118l1.287-3.957a1 1 0 00-.364-1.118L2.063 9.384c-.783-.57-.38-1.81.588-1.81h4.162a1 1 0 00.95-.69l1.286-3.957z"
                  />
                </svg>
                <span class="font-semibold text-base xl:text-lg">
                  {{ movie.rating !== null ? movie.rating.toFixed(1) : '0.0' }}
                </span>
              </div>
              <span
                class="text-xs xl:text-sm text-text-secondary border-l border-white/10 pl-2 group-hover:text-text-primary transition-colors downward-arrow"
              >
                Xem đánh giá
              </span>
            </button>
          </div>

          <div class="flex flex-wrap items-center gap-x-6 gap-y-2 text-body text-text-primary">
            <span class="flex items-center gap-1.5 font-medium">
              <BaseIcon :icon="Clock" :size="16" :stroke-width="1.5" class="text-accent" />
              {{ movie.duration }} phút
            </span>

            <span v-if="movie.releaseDate" class="flex items-center gap-1.5">
              <BaseIcon :icon="Calendar" :size="16" :stroke-width="1.5" class="text-accent" />
              {{ formatDateVN(movie.releaseDate) }}
            </span>
          </div>

          <div class="flex text-body text-text-secondary gap-3">
            <h3 class="mt-1">Thể loại:</h3>
            <div v-if="movie.genres?.length" class="flex flex-wrap gap-2">
              <GenrePill v-for="genre in movie.genres" :key="genre.id" :genre="genre" />
            </div>
          </div>

          <div class="pt-2 xl:pt-4 border-t border-border-default">
            <p class="text-body text-text-secondary">
              <span class="font-medium text-text-primary">Đạo diễn: </span>
              {{ directorStr }}
            </p>
          </div>

          <div class="pt-2 xl:pt-4 border-t border-border-default">
            <p class="text-body text-text-secondary">
              <span class="font-medium text-text-primary">Diễn viên: </span>
              {{ castStr }}
            </p>
          </div>
        </div>

        <div class="lg:col-span-4 lg:row-span-2">
          <MovieShowtimeSidebar
            v-if="movie"
            :movie-status="movie.status"
            :movie-id="movie.id"
            @book="handleBookShowtime"
          />
        </div>

        <div class="lg:col-span-8 flex flex-col gap-3">
          <div class="flex gap-2 items-center">
            <div class="w-1 h-6 bg-accent"></div>
            <h3 class="text-lg font-bold xl:text-title">Nội dung phim</h3>
          </div>

          <p class="text-body text-text-secondary leading-relaxed text-justify whitespace-pre-line">
            {{ movie.description || 'Chưa có mô tả chi tiết cho phim này.' }}
          </p>

          <div class="mt-4 xl:mt-8">
            <HotMoviesCarousel />
          </div>
        </div>
      </div>
    </div>

    <div v-else class="max-w-7xl mx-auto px-4 py-32 text-center text-text-secondary">
      <p v-if="loading">Đang tải thông tin phim...</p>
      <p v-else-if="error">{{ error }}</p>
      <p v-else>Không tìm thấy phim.</p>
    </div>

    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-300 ease-out"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition duration-200 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="isReviewOpen && movie"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 md:p-10"
        >
          <div
            class="absolute inset-0 bg-black/80 backdrop-blur-md"
            @click="isReviewOpen = false"
          ></div>

          <Transition
            enter-active-class="transition duration-300 ease-out"
            enter-from-class="opacity-0 translate-y-4 sm:scale-95"
            enter-to-class="opacity-100 translate-y-0 sm:scale-100"
            leave-active-class="transition duration-200 ease-in"
            leave-from-class="opacity-100 translate-y-0 sm:scale-100"
            leave-to-class="opacity-0 translate-y-4 sm:scale-95"
          >
            <div
              class="relative w-full max-w-3xl bg-surface-dark border border-border-default rounded-2xl shadow-2xl flex flex-col max-h-[85vh] overflow-hidden z-10"
            >
              <div
                class="flex items-center justify-between px-6 py-4 border-b border-border-default"
              >
                <div>
                  <h2 class="text-xl font-bold tracking-wide text-amber-400">
                    Đánh giá & Bình luận
                  </h2>
                  <p class="text-xs font-bold text-zinc-400 mt-0.5">{{ movie.title }}</p>
                </div>
                <button
                  @click="isReviewOpen = false"
                  class="p-1.5 rounded-lg bg-overlay-light-10 hover:bg-overlay-light-20 text-text-secondary hover:text-text-primary transition-colors active:scale-95"
                >
                  <svg
                    class="w-5 h-5"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <div class="flex-1 overflow-y-auto p-6 custom-scrollbar">
                <MovieReviewSection :movie-id="movie.id" class="w-full" @request-login="handleRequestLogin" />
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, ref } from 'vue' // Đã thêm ref ở đây
import { useRoute } from 'vue-router'
import MovieTrailer from '@/components/movie/MovieTrailer.vue'
import AgeRatingTag from '@/components/movie/AgeRatingTag.vue'
import MovieCard from '@/components/movie/MovieCard.vue'
import MovieShowtimeSidebar from '@/components/movie/MovieShowtimeSidebar.vue'
import MovieReviewSection from '@/components/movie/MovieReviewSection.vue' // Import lại vào đây
import { useMovieDetail } from '@/composables/useMovieDetail'
import { formatDateVN } from '@/utils/dateFormat'
import GenrePill from '@/components/movie/GenrePill.vue'
import { Clock, Calendar } from 'lucide-vue-next'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import HotMoviesCarousel from '@/components/movie/HotMoviesCarousel.vue'
import { useUIStore } from '@/stores/ui.store'

const route = useRoute()
const { movie, loading, error, fetchMovie } = useMovieDetail()
const ui = useUIStore()

// State quản lý việc đóng/mở Modal đánh giá
const isReviewOpen = ref(false)

const handleRequestLogin = () => {
  isReviewOpen.value = false
  ui.openLoginModal()
}

watch(
  () => route.params.id,
  async (newId) => {
    if (!newId) return

    const idString = Array.isArray(newId) ? newId[0] : newId
    const targetId = Number(idString)

    // SỬA LỖI 2: Lấy history state chuẩn từ window.history để tránh lỗi TS của Vue Router
    const historyState = window.history.state as { movie?: any } | null

    // Chỉ dùng state nếu id khớp — tránh dùng state cũ
    if (historyState?.movie && historyState.movie.id === targetId) {
      movie.value = historyState.movie
      return
    }

    movie.value = null // reset để tránh hiển thị data cũ trong lúc load
    isReviewOpen.value = false // Tự động đóng popup khi chuyển phim
    await fetchMovie(targetId)
  },
  { immediate: true },
)

const handleBookShowtime = (showtimeId: number) => {
  console.log('Đặt vé suất chiếu ID:', showtimeId)
}

const displayTrailerUrl = computed(() => {
  return movie.value?.trailerUrl || ''
})

const directorStr = computed(() => {
  if (movie.value?.director) return movie.value.director
  if (movie.value?.directors?.length)
    return movie.value.directors.map((d: any) => d.name).join(', ')
  return 'Đang cập nhật'
})

const castStr = computed(() => {
  if (movie.value?.actors) return movie.value.actors
  if (movie.value?.cast?.length)
    return movie.value.cast
      .slice(0, 6)
      .map((a: any) => a.name)
      .join(', ')
  return 'Đang cập nhật'
})
</script>

<style scoped>
/* Custom thanh cuộn tinh tế cho danh sách đánh giá bên trong Popup */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 9999px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
