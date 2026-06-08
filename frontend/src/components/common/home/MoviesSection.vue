<template>
  <section class="py-4">
    <div class="flex items-center justify-between px-16">
      <div class="flex gap-10 items-center">
        <!-- Tiêu đề -->
        <div class="flex gap-2 items-center">
          <div class="w-1 h-6 bg-accent"></div>
          <h1 class="text-title text-text-primary">Phim</h1>
        </div>

        <!-- Tabs -->
        <div ref="tabsContainer" class="relative flex gap-6 items-center">
          <!-- Đang chiếu -->
          <button
            ref="nowShowingTabRef"
            class="flex flex-col items-center gap-1 text-body cursor-pointer transition-colors duration-200"
            :class="
              currentStatus === 'NOW_SHOWING'
                ? 'text-accent'
                : 'text-text-secondary hover:text-accent'
            "
            @click="switchTab('NOW_SHOWING')"
          >
            <h2>Đang chiếu</h2>
          </button>

          <!-- Sắp chiếu -->
          <button
            ref="comingSoonTabRef"
            class="flex flex-col items-center gap-1 text-body cursor-pointer transition-colors duration-200"
            :class="
              currentStatus === 'COMING_SOON'
                ? 'text-accent'
                : 'text-text-secondary hover:text-accent'
            "
            @click="switchTab('COMING_SOON')"
          >
            <h2>Sắp chiếu</h2>
          </button>

          <!-- Phim dành cho bạn -->
          <button
            ref="recommendedTabRef"
            class="flex flex-col items-center gap-1 text-body cursor-pointer transition-colors duration-200"
            :class="
              currentStatus === 'RECOMMENDED'
                ? 'text-accent'
                : 'text-text-secondary hover:text-accent'
            "
            @click="switchTab('RECOMMENDED')"
          >
            <h2>Phim dành cho bạn</h2>
          </button>

          <!-- Gạch chân active tab -->
          <div
            class="absolute -bottom-1 h-0.5 w-7 bg-accent rounded-full transition-all duration-400 ease-[cubic-bezier(0.16,1,0.3,1)]"
            :style="indicatorStyle"
          />
        </div>
      </div>

      <!-- Nút xem thêm -->
      <BaseButton variant="secondary" size="md" :disabled="loading" @click="goToMoviesPage">
        <div class="flex gap-1 items-center">
          <h4 class="text-caption">Xem thêm</h4>
          <BaseIcon :icon="ArrowRight" :size="16" />
        </div>
      </BaseButton>
    </div>

    <!-- Error State -->
    <div v-if="error" class="flex flex-col items-center gap-4 py-10 text-text-secondary">
      <p>{{ error }}</p>

      <BaseButton variant="secondary" size="sm" @click="retry"> Thử lại </BaseButton>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="!loading && !error && !movies.length"
      class="text-center py-10 text-text-secondary"
    >
      Không có phim để hiển thị.
    </div>

    <!-- Movie List -->
    <MovieCardsList v-else :movies="movies" :loading="loading" :skeletonCount="4" @book="onBook" />
  </section>
</template>

<script lang="ts" setup>
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from 'lucide-vue-next'

import { useMovieSection } from '@/composables/useMovieSection'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import MovieCardsList from '@/components/movie/MovieCardsList.vue'

const router = useRouter()

const { movies, loading, error, currentStatus, switchTab, onBook, retry } = useMovieSection()

const goToMoviesPage = () => {
  router.push({
    path: '/movies',
    query: {
      status: currentStatus.value,
    },
  })
}

// ================== Indicator logic ==================
const tabsContainer = ref<HTMLElement | null>(null)
const nowShowingTabRef = ref<HTMLElement | null>(null)
const comingSoonTabRef = ref<HTMLElement | null>(null)
const recommendedTabRef = ref<HTMLElement | null>(null)

const indicatorStyle = ref<Record<string, string>>({
  left: '0px',
})

function getTabRect(el: HTMLElement | null, container: HTMLElement | null) {
  if (!el || !container) {
    return {
      left: 0,
      width: 0,
    }
  }

  const tabRect = el.getBoundingClientRect()
  const containerRect = container.getBoundingClientRect()

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
    left: `${rect.left + rect.width / 2 - 14}px`,
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
