<template>
    <div class="min-h-screen bg-surface-dark text-text-primary">
        <!-- Trailer -->
        <MovieTrailer :trailer-url="displayTrailerUrl" :title="movie?.title || 'Phim mẫu'" />

        <!-- Main Content -->
        <div v-if="movie" class="px-4 pb-8 xl:px-16 xl:pb-16 relative z-10">
            <div class="max-w-328 grid grid-cols-1 lg:grid-cols-12 gap-4 xl:gap-8">

                <!-- Hàng 1, Cột 1: Poster -->
                <div class="lg:col-span-3">
                    <MovieCard :movie="movie" hide-overlay hide-badges no-redirect hide-title
                        class="max-w-none cursor-default shadow-2xl shadow-black/60 -translate-y-4 xl:-translate-y-10" />
                </div>

                <!-- Hàng 1, Cột 2: Movie Information -->
                <div class="lg:col-span-5 space-y-2 py-2 xl:space-y-4 xl:py-4">
                    <h1 class="text-2xl font-bold xl:text-display">{{ movie.title }}</h1>
                    <div class="flex gap-2 items-center">
                        <AgeRatingTag :rating="movie.ageRating" />
                        <div v-if="movie.rating !== null"
                            class="flex items-center gap-1.5 text-yellow-400 bg-overlay-light-20 px-3 py-1 rounded-full">
                            <svg class="w-5 h-5 fill-current" viewBox="0 0 20 20">
                                <path
                                    d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.957a1 1 0 00.95.69h4.162c.969 0 1.371 1.24.588 1.81l-3.37 2.448a1 1 0 00-.364 1.118l1.287 3.957c.3.921-.755 1.688-1.54 1.118l-3.37-2.448a1 1 0 00-1.175 0l-3.37 2.448c-.784.57-1.838-.197-1.539-1.118l1.287-3.957a1 1 0 00-.364-1.118L2.063 9.384c-.783-.57-.38-1.81.588-1.81h4.162a1 1 0 00.95-.69l1.286-3.957z" />
                            </svg>
                            <span class="font-semibold text-base xl:text-lg">
                                {{ movie.rating.toFixed(1) }}
                            </span>
                        </div>
                    </div>

                    <div class="flex flex-wrap items-center gap-x-6 gap-y-2 text-body text-text-primary">
                        <!-- Thời lượng -->
                        <span class="flex items-center gap-1.5 font-medium">
                            <BaseIcon :icon="Clock" :size="16" :stroke-width="1.5" class="text-accent" />
                            {{ movie.duration }} phút
                        </span>

                        <!-- Ngày ra mắt -->
                        <span v-if="movie.releaseDate" class="flex items-center gap-1.5">
                            <BaseIcon :icon="Calendar" :size="16" :stroke-width="1.5" class="text-accent" />
                            {{ formatDateVN(movie.releaseDate) }}
                        </span>
                    </div>

                    <!-- Genres -->
                    <div class="flex text-body text-text-secondary gap-3">
                        <h3 class="mt-1">Thể loại:</h3>
                        <div v-if="movie.genres?.length" class="flex flex-wrap gap-2">
                            <GenrePill v-for="genre in movie.genres" :key="genre.id" :genre="genre" />
                        </div>
                    </div>

                    <!-- Director -->
                    <div class="pt-2 xl:pt-4 border-t border-border-default">
                        <p class="text-body text-text-secondary">
                            <span class="font-medium text-text-primary">Đạo diễn: </span>
                            {{ directorStr }}
                        </p>
                    </div>

                    <!-- Cast -->
                    <div class="pt-2 xl:pt-4 border-t border-border-default">
                        <p class="text-body text-text-secondary">
                            <span class="font-medium text-text-primary">Diễn viên: </span>
                            {{ castStr }}
                        </p>
                    </div>
                </div>

                <!-- Hàng 1+2, Cột 3: Showtime Sidebar -->
                <div class="lg:col-span-4 lg:row-span-2">
                    <MovieShowtimeSidebar v-if="movie" :movie-status="movie.status" :movie-id="movie.id"
                        @book="handleBookShowtime" />
                </div>

                <!-- Hàng 2, Cột 1-2: Mô tả phim -->
                <div class="lg:col-span-8 flex flex-col gap-3">
                    <div class="flex gap-2 items-center">
                        <div class="w-1 h-6 bg-accent"></div>
                        <h3 class="text-lg font-bold xl:text-title">Nội dung phim</h3>
                    </div>

                    <p class="text-body text-text-secondary leading-relaxed">
                        {{ movie.description || 'Chưa có mô tả chi tiết cho phim này.' }}
                    </p>

                    <div class="mt-4 xl:mt-8">
                        <HotMoviesCarousel />
                    </div>
                </div>
            </div>
        </div>

        <!-- Loading / Error -->
        <div v-else class="max-w-7xl mx-auto px-4 py-32 text-center text-text-secondary">
            <p v-if="loading">Đang tải thông tin phim...</p>
            <p v-else-if="error">{{ error }}</p>
            <p v-else>Không tìm thấy phim.</p>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import MovieTrailer from '@/components/movie/MovieTrailer.vue'
import AgeRatingTag from '@/components/movie/AgeRatingTag.vue'
import MovieCard from '@/components/movie/MovieCard.vue'
import MovieShowtimeSidebar from '@/components/movie/MovieShowtimeSidebar.vue'
import { useMovieDetail } from '@/composables/useMovieDetail'
import type { MovieResponse } from '@/types/movie.types'
import { formatDateVN } from '@/utils/dateFormat'
import GenrePill from '@/components/movie/GenrePill.vue'
import { Clock, Calendar } from 'lucide-vue-next'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import HotMoviesCarousel from '@/components/movie/HotMoviesCarousel.vue'

const route = useRoute()
const { movie, loading, error, fetchMovie } = useMovieDetail()

watch(
    () => route.params.id,
    async (newId) => {
        if (!newId) return

        // Chỉ dùng state nếu id khớp — tránh dùng state cũ
        if (route.state?.movie && route.state.movie.id === Number(newId)) {
            movie.value = route.state.movie as MovieResponse
            return
        }

        movie.value = null  // reset để tránh hiển thị data cũ trong lúc load
        await fetchMovie(Number(newId))
    },
    { immediate: true }
)

const handleBookShowtime = (showtimeId: number) => {
    console.log('Đặt vé suất chiếu ID:', showtimeId)
}

const displayTrailerUrl = computed(() => {
    return movie.value?.trailerUrl || ''
})

const directorStr = computed(() => {
    if (movie.value?.director) return movie.value.director
    if (movie.value?.directors?.length) return movie.value.directors.map((d: any) => d.name).join(', ')
    return 'Đang cập nhật'
})

const castStr = computed(() => {
    if (movie.value?.actors) return movie.value.actors
    if (movie.value?.cast?.length) return movie.value.cast.slice(0, 6).map((a: any) => a.name).join(', ')
    return 'Đang cập nhật'
})
</script>