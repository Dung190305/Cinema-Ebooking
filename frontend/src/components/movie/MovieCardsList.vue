<template>
    <div class="w-full max-w-full mx-auto py-8">
        <AppCarousel
            :items="carouselItems"
            :slidesPerView="slidesPerView"
            :gap="32"
            :infinite="false"
            :paddingLeft="paddingLeft"
            :paddingRight="paddingRight"
            :edgeButtons="true"
        >
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
import type { MovieResponse } from '@/types/movie'

const props = defineProps<{
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
        const count = props.skeletonCount || 4
        return Array.from({ length: count }, (_, i) => ({
            _skeleton: true,
            _id: i,
        }))
    }

    return props.movies
})

const windowWidth = ref(window.innerWidth)

const onResize = () => {
    windowWidth.value = window.innerWidth
}

onMounted(() => window.addEventListener('resize', onResize))
onBeforeUnmount(() => window.removeEventListener('resize', onResize))

const SECTION_PADDING = 64

const containerWidth = computed(() =>
    Math.max(0, windowWidth.value - SECTION_PADDING * 2)
)

const slidesPerView = computed(() => {
    if (containerWidth.value >= 1200) return 4
    if (containerWidth.value >= 900) return 3
    return 2
})

const paddingLeft = computed(() => Math.min(64, containerWidth.value * 0.05))
const paddingRight = computed(() => Math.max(32, containerWidth.value * 0.06))
</script>