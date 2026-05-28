<template>
    <div class="w-full">
        <h2 class="text-2xl font-bold text-text-primary mb-4">🔥 Phim đang hot</h2>

        <!-- Loading skeleton -->
        <div v-if="loading" class="flex gap-4 overflow-hidden">
            <MovieCardSkeleton v-for="n in skeletonCount" :key="n" class="shrink-0"
                :style="{ width: skeletonWidth + 'px' }" />
        </div>

        <!-- Carousel khi có dữ liệu -->
        <Carousel v-else-if="movies.length" :items="movies" :slides-per-view="slidesPerView" :gap="16" :peek="0"
            :infinite="true" :autoplay="true" :interval="4000" :dots="false" :edge-buttons="false" :simple-nav="true">
            <template #default="{ item }">
                <div class="cursor-pointer transition-transform hover:scale-105" @click="goToDetail(item.id)">
                    <MovieCard :movie="item" :hide-overlay="false" :hide-badges="false" :hide-title="false"
                        class="w-full" />
                </div>
            </template>
        </Carousel>

        <!-- Fallback -->
        <p v-else class="text-text-secondary text-center py-8">
            Chưa có phim hot nào.
        </p>
    </div>
</template>
<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Carousel from '@/components/common/AppCarousel.vue'
import MovieCard from '@/components/movie/MovieCard.vue'
import MovieCardSkeleton from '@/components/movie/MovieCardSkeleton.vue'
import { useHotMovies } from '@/composables/useHotMovies'

const router = useRouter()
const { movies, loading, fetchMovies } = useHotMovies()

// Responsive slidesPerView dựa trên màn hình
const slidesPerView = ref(4)
const skeletonCount = computed(() => slidesPerView.value + 1)
const skeletonWidth = ref(200)

const updateSlidesPerView = () => {
    const width = window.innerWidth
    if (width < 640) {
        slidesPerView.value = 2
        skeletonWidth.value = width - 64
    } else if (width < 1024) {
        slidesPerView.value = 3
        skeletonWidth.value = (width - 64 - 16) / 2
    } else {
        // Desktop: hiển thị 3 card trong không gian 8 cột (~66% màn hình)
        slidesPerView.value = 4
        skeletonWidth.value = (width * 0.66 - 32) / 3 // ước lượng
    }
}

onMounted(() => {
    updateSlidesPerView()
    window.addEventListener('resize', updateSlidesPerView)
    fetchMovies()
})

onUnmounted(() => {
    window.removeEventListener('resize', updateSlidesPerView)
})

const goToDetail = (id: number) => {
    router.push({ name: 'movie-detail', params: { id } })
}
</script>
