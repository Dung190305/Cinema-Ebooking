<template>
    <div class="w-full max-w-full mx-auto py-8">
        <AppCarousel :items="movies" :slidesPerView="slidesPerView" :gap="32" :infinite="false"
            :paddingLeft="paddingLeft" :paddingRight="paddingRight" :edgeButtons="true">
            <template #default="{ item }">
                <div class="w-full">
                    <MovieCardSkeleton v-if="item._skeleton" />
                    <MovieCard v-else :movie="item" @book="handleBook" />
                </div>
            </template>
        </AppCarousel>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import AppCarousel from '@/components/common/AppCarousel.vue'
import MovieCard from '@/components/movie/MovieCard.vue'
import MovieCardSkeleton from '@/components/movie/MovieCardSkeleton.vue'
import type { MovieResponse } from '@/types/movie.types'

defineProps<{
    movies: MovieResponse[]
    loading?: boolean
    skeletonCount?: number
}>()

const emit = defineEmits<{
    book: [id: number]
}>()

const handleBook = (id: number) => {
    emit('book', id)
}

const carouselItems = computed(() => {
    if (props.loading) {
        const count = props.skeletonCount || 4 // mặc định 4 skeleton
        return Array.from({ length: count }, (_, i) => ({ _skeleton: true, _id: i }))
    }
    return props.movies
})

// ── Kích thước cửa sổ ──
const windowWidth = ref(window.innerWidth)
const onResize = () => {
    windowWidth.value = window.innerWidth
}
onMounted(() => window.addEventListener('resize', onResize))
onBeforeUnmount(() => window.removeEventListener('resize', onResize))

// Container thực tế của carousel (trừ padding MovieSection px-16)
const SECTION_PADDING = 64 // px-16 mỗi bên
const containerWidth = computed(() => Math.max(0, windowWidth.value - SECTION_PADDING * 2))

// Số phim hiển thị dựa trên chiều rộng container
const slidesPerView = computed(() => {
    if (containerWidth.value >= 1200) return 4
    if (containerWidth.value >= 900) return 3
    return 2 // rất nhỏ (dưới 900px)
})

// Padding trái – bám sát thiết kế: tối đa 64px, nhỏ hơn nếu màn hẹp
const paddingLeft = computed(() => Math.min(64, containerWidth.value * 0.05))

// Peek bên phải – luôn có để thấy một phần card tiếp theo
const paddingRight = computed(() => Math.max(32, containerWidth.value * 0.06))
</script>