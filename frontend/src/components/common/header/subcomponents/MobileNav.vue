<template>
    <div class="flex flex-col h-full gap-4">

        <!-- Search bar -->
        <div class="px-1">
            <div class="flex items-center gap-2 bg-bg-elevated border border-border-default rounded-xl px-3 py-2.5">
                <BaseIcon :icon="Search" :size="16" class="text-text-secondary shrink-0" aria-hidden="true" />
                <input v-model="query" type="search" autocomplete="off" spellcheck="false"
                    placeholder="Tìm phim đang chiếu..."
                    class="flex-1 bg-transparent text-text-primary placeholder:text-text-secondary text-sm outline-none"
                    @focus="onFocus" @keydown.enter="handleSubmit" />
                <button v-if="query" @click="query = ''"
                    class="text-text-secondary hover:text-text-primary transition-colors">
                    <BaseIcon :icon="X" :size="14" />
                </button>
            </div>

            <!-- Dropdown results trong sidebar -->
            <div v-if="isOpen && dropdownItems.length > 0"
                class="mt-2 border border-border-default rounded-xl overflow-hidden bg-bg-surface">
                <button v-for="movie in dropdownItems" :key="movie.id"
                    class="w-full flex items-center gap-3 px-3 py-2 text-left hover:bg-bg-elevated transition-colors"
                    @click="handleSelect(movie)">
                    <div class="shrink-0 w-8 h-11 rounded-md overflow-hidden bg-bg-elevated">
                        <img v-if="movie.posterUrl"
                            :src="getTransformedUrl(movie.posterUrl, { width: 64, height: 88, crop: 'fill' })"
                            :alt="movie.title" class="w-full h-full object-cover" loading="lazy" />
                        <div v-else class="w-full h-full flex items-center justify-center">
                            <BaseIcon :icon="Film" :size="12" class="text-text-secondary" />
                        </div>
                    </div>
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-text-primary truncate">{{ movie.title }}</p>
                        <p class="text-xs text-text-secondary mt-0.5">{{ movie.duration }} phút · {{ movie.ageRating }}
                        </p>
                    </div>
                </button>
                <div class="px-3 py-1.5 border-t border-border-default">
                    <button class="text-xs text-accent hover:underline w-full text-left" @click="handleSubmit">
                        Xem tất cả kết quả →
                    </button>
                </div>
            </div>

            <div v-if="isOpen && query.trim() && dropdownItems.length === 0 && !isLoading"
                class="mt-2 px-3 py-3 text-center border border-border-default rounded-xl text-sm text-text-secondary">
                Không tìm thấy "{{ query }}"
            </div>
        </div>

        <!-- Nav items -->
        <div class="space-y-1">
            <MobileNavItem label="Phim" :items="movieMenuItems" @close="$emit('close')" />
            <MobileNavItem label="Lịch chiếu" :items="showtimeMenuItems" @close="$emit('close')" />
            <MobileNavItem label="Khuyến mãi" :items="promotionMenuItems" @close="$emit('close')" />
            <router-link to="/profile?tab=tickets"
                class="block px-4 py-3 text-text-primary hover:bg-accent/10 rounded-lg transition-colors"
                @click="$emit('close')">
                Vé của tôi
            </router-link>
        </div>

        <!-- Auth section ở cuối sidebar -->
        <div class="mt-auto pt-4 border-t border-border-default">
            <MobileSidebarAuth @close="$emit('close')" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { Search, X, Film } from 'lucide-vue-next'
import { useMovieSearch } from '@/composables/useMovieSearch'
import { useCloudinaryImage } from '@/composables/useCloudinaryImage'
import { useRouter } from 'vue-router'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import MobileNavItem from './MobileNavItem.vue'
import MobileSidebarAuth from './MobileSidebarAuth.vue'
import type { MovieResponse } from '@/types/movie.types'

const emit = defineEmits<{ close: [] }>()
const router = useRouter()
const { getTransformedUrl } = useCloudinaryImage()
const { query, isOpen, isLoading, dropdownItems, onFocus, selectMovie, submitSearch } = useMovieSearch()

function handleSelect(movie: MovieResponse) {
    selectMovie(movie)
    emit('close')
}

function handleSubmit() {
    submitSearch()
    emit('close')
}

const movieMenuItems = [
    { label: 'Phim đang chiếu', route: '/movies', query: { status: 'NOW_SHOWING' } },
    { label: 'Phim sắp chiếu', route: '/movies', query: { status: 'COMING_SOON' } },
    { label: 'Phim đề xuất', route: '/movies', query: { status: 'NOW_SHOWING' } },
]

const showtimeMenuItems = [
    { label: 'Suất chiếu hôm nay', route: '/showtimes', getQuery: () => ({ date: new Date().toISOString().split('T')[0] }) },
    { label: 'Suất chiếu theo rạp', route: '/showtimes' },
    { label: 'Tất cả suất chiếu', route: '/showtimes' },
]

const promotionMenuItems = [
    { label: 'Ưu đãi thành viên', route: '/promotions' },
    { label: 'Coupon', route: '/coupons' },
]
</script>