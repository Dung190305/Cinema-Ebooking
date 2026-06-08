<template>
  <div class="w-full max-w-360 mx-auto px-4 lg:px-8 xl:px-16 py-8">
    <!-- Premium Segment Switcher -->
    <div class="flex justify-center mb-10">
      <div
        ref="tabsContainer"
        class="relative inline-flex items-center rounded-full p-1.5 backdrop-blur-sm border border-white/10"
        style="background: rgba(255, 255, 255, 0.05); box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1)"
      >
        <!-- Đang chiếu -->
        <button
          ref="nowShowingTabRef"
          @click="setStatus('NOW_SHOWING')"
          class="relative z-10 flex items-center gap-2 px-6 py-2.5 text-sm font-medium transition-colors duration-300 rounded-full"
          :class="
            currentStatus === 'NOW_SHOWING'
              ? 'text-text-on-accent'
              : 'text-text-secondary hover:text-text-primary'
          "
        >
          <PlayIcon
            class="w-4 h-4 transition-transform"
            :class="{ 'scale-110': currentStatus === 'NOW_SHOWING' }"
          />
          <span>Đang chiếu</span>
        </button>

        <!-- Sắp chiếu -->
        <button
          ref="comingSoonTabRef"
          @click="setStatus('COMING_SOON')"
          class="relative z-10 flex items-center gap-2 px-6 py-2.5 text-sm font-medium transition-colors duration-300 rounded-full"
          :class="
            currentStatus === 'COMING_SOON'
              ? 'text-text-on-accent'
              : 'text-text-secondary hover:text-text-primary'
          "
        >
          <ClockIcon
            class="w-4 h-4 transition-transform"
            :class="{ 'scale-110': currentStatus === 'COMING_SOON' }"
          />
          <span>Sắp chiếu</span>
        </button>

        <!-- Phim dành cho bạn -->
        <button
          ref="recommendedTabRef"
          @click="setStatus('RECOMMENDED')"
          class="relative z-10 flex items-center gap-2 px-6 py-2.5 text-sm font-medium transition-colors duration-300 rounded-full"
          :class="
            currentStatus === 'RECOMMENDED'
              ? 'text-text-on-accent'
              : 'text-text-secondary hover:text-text-primary'
          "
        >
          <SparklesIcon
            class="w-4 h-4 transition-transform"
            :class="{ 'scale-110': currentStatus === 'RECOMMENDED' }"
          />
          <span>Phim dành cho bạn</span>
        </button>

        <!-- Sliding Indicator -->
        <div
          class="indicator absolute top-1.5 bottom-1.5 rounded-full bg-accent shadow-lg shadow-accent/40 transition-all duration-500 ease-[cubic-bezier(0.16,1,0.3,1)]"
          :style="indicatorStyle"
        />
      </div>
    </div>

    <!-- Tiêu đề phụ -->
    <div class="mb-8 text-center">
      <h1 class="text-2xl font-bold text-text-primary">
        {{ pageTitle }}
      </h1>
      <p v-if="currentStatus === 'RECOMMENDED'" class="mt-2 text-sm text-text-secondary">
        Danh sách phim được AI gợi ý dựa trên lịch sử đặt vé và sở thích của bạn.
      </p>
    </div>

    <!-- Skeleton Loading Grid -->
    <div v-if="loading" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
      <div v-for="i in 12" :key="'skeleton-' + i" class="flex flex-col items-start gap-2">
        <div class="w-full aspect-2/3 rounded-lg bg-bg-surface animate-pulse" />
        <div class="w-3/4 h-4 rounded bg-bg-surface animate-pulse" />
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="text-center text-text-secondary py-12">
      <p>{{ error }}</p>

      <BaseButton variant="secondary" size="sm" class="mt-4" @click="fetchMovies">
        Thử lại
      </BaseButton>
    </div>

    <!-- Movie Grid -->
    <template v-else>
      <div v-if="movies.length" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        <MovieCard v-for="movie in movies" :key="movie.id" :movie="movie" @book="handleBook" />
      </div>

      <div v-else class="text-center text-text-secondary py-12">
        <p v-if="currentStatus === 'RECOMMENDED'">Chưa có phim gợi ý phù hợp.</p>
        <p v-else>Không có phim nào.</p>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { Play, Clock, Sparkles } from 'lucide-vue-next'
import { useMovieList } from '@/composables/useMovieList'
import MovieCard from '@/components/movie/MovieCard.vue'
import BaseButton from '@/components/ui/button/BaseButton.vue'

const PlayIcon = Play
const ClockIcon = Clock
const SparklesIcon = Sparkles

const { movies, loading, error, currentStatus, setStatus, fetchMovies } = useMovieList()

const pageTitle = computed(() => {
  if (currentStatus.value === 'RECOMMENDED') {
    return 'Phim dành cho bạn'
  }

  if (currentStatus.value === 'COMING_SOON') {
    return 'Phim sắp chiếu'
  }

  return 'Phim đang chiếu'
})

const handleBook = (movieId: number) => {
  console.log('Đặt vé phim ID:', movieId)
}

// ==========================
// Premium Indicator Logic
// ==========================
const tabsContainer = ref<HTMLElement | null>(null)
const nowShowingTabRef = ref<HTMLElement | null>(null)
const comingSoonTabRef = ref<HTMLElement | null>(null)
const recommendedTabRef = ref<HTMLElement | null>(null)

const indicatorStyle = ref<Record<string, string>>({
  left: '0px',
  width: '0px',
})

function getTabRect(tabEl: HTMLElement | null, containerEl: HTMLElement | null) {
  if (!tabEl || !containerEl) {
    return {
      left: 0,
      width: 0,
    }
  }

  const tabRect = tabEl.getBoundingClientRect()
  const containerRect = containerEl.getBoundingClientRect()

  return {
    left: tabRect.left - containerRect.left,
    width: tabRect.width,
  }
}

function updateIndicator() {
  let activeTab: HTMLElement | null = nowShowingTabRef.value

  if (currentStatus.value === 'COMING_SOON') {
    activeTab = comingSoonTabRef.value
  }

  if (currentStatus.value === 'RECOMMENDED') {
    activeTab = recommendedTabRef.value
  }

  const rect = getTabRect(activeTab, tabsContainer.value)

  indicatorStyle.value = {
    left: `${rect.left}px`,
    width: `${rect.width}px`,
  }
}

watch(currentStatus, () => {
  nextTick(updateIndicator)
})

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  nextTick(updateIndicator)

  if (tabsContainer.value) {
    resizeObserver = new ResizeObserver(() => {
      updateIndicator()
    })

    resizeObserver.observe(tabsContainer.value)
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
})
</script>
