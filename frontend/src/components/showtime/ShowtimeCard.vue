<template>
    <div
        class="flex flex-col sm:flex-row gap-4 p-4 rounded-lg bg-bg-surface shadow-md hover:shadow-lg transition-shadow">
        <!-- Poster -->
        <div class="w-full sm:w-40 shrink-0 mx-auto sm:mx-0">
            <MovieCard v-if="movie" :movie="movie" :hideOverlay="true" />
        </div>

        <!-- Thông tin suất chiếu -->
        <div v-if="showtimes.length" class="flex flex-col gap-2 flex-1 min-w-0">
            <!-- Địa điểm (thành phố) -->
            <div class="flex items-center gap-2 text-caption text-text-secondary">
                <BaseIcon :icon="MapPin" :size="16" />
                <span>{{ cinema?.city || 'Đang cập nhật' }}</span>
            </div>

            <!-- Tên rạp -->
            <div class="flex items-center gap-2 text-body font-medium text-text-primary">
                <span>{{ cinema?.name || 'Đang cập nhật' }}</span>
            </div>

            <!-- Định dạng (2D, 3D,...) -->
            <div class="flex items-center gap-2 text-caption text-text-secondary">
                <BaseIcon :icon="Film" :size="16" />
                <span>{{ formatName }}</span>
            </div>

            <!-- Ngôn ngữ thanh thoại -->
            <div class="flex items-center gap-2 text-caption text-text-secondary">
                <BaseIcon :icon="Mic" :size="16" />
                <span>Ngôn ngữ: {{ audioLanguageLabel }}</span>
            </div>

            <!-- Phụ đề -->
            <div class="flex items-center gap-2 text-caption text-text-secondary">
                <BaseIcon :icon="Captions" :size="16" />
                <span>Phụ đề: {{ subtitleLanguageLabel }}</span>
            </div>

            <!-- Các giờ chiếu (dạng pill) -->
            <div class="flex items-start gap-4 mt-1">
                <BaseIcon :icon="Clock" :size="16" class="text-text-secondary mt-2 shrink-0" />
                <div class="flex flex-wrap gap-4">
                    <button v-for="st in showtimes" :key="st.id" class="
                        px-3.5 py-1.5 rounded-full text-caption font-medium
                        border border-accent/30 bg-accent/5 text-accent
                        hover:bg-accent hover:text-text-on-accent hover:border-accent
                        active:scale-95
                        transition-all duration-200
                        cursor-pointer
                        focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent
                        shadow-sm hover:shadow-md
                    " @click="handleBook(st.id)">
                        {{ formatTime(st.startTime) }} - {{ formatTime(st.endTime) }}
                    </button>
                </div>
            </div>
        </div>

        <!-- Fallback khi không có suất chiếu nào -->
        <div v-else class="flex-1 text-center text-text-secondary italic py-4">
            Không có suất chiếu
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import MovieCard from '@/components/movie/MovieCard.vue'
import { MapPin, Clock, Film, Mic, Captions } from 'lucide-vue-next'
import type { ShowtimeResponse } from '@/types/showtime'
import type { MovieResponse } from '@/types/movie'
import type { CinemaResponse } from '@/types/cinema'
import type { ShowtimeFormatResponse } from '@/types/showtime'
import { useAuthStore } from '@/stores/auth.store'
import { formatTimeVN } from '@/utils/dateFormat'
import { getLanguageLabel } from '@/constants/languages'

const props = defineProps<{
    showtimes: ShowtimeResponse[]
    movie: MovieResponse | undefined
    cinema: CinemaResponse | undefined
    format: ShowtimeFormatResponse | undefined
}>()

const emit = defineEmits<{
    book: [showtimeId: number]
}>()

const authStore = useAuthStore()

const formatName = computed(() => props.format?.name || '2D')
const firstShowtime = computed(() => props.showtimes[0])

const audioLanguageLabel = computed(() =>
    firstShowtime.value ? getLanguageLabel(firstShowtime.value.audioLanguage) : '--'
)
const subtitleLanguageLabel = computed(() =>
    firstShowtime.value ? getLanguageLabel(firstShowtime.value.subtitleLanguage) : '--'
)

const formatTime = (startTime: string | Date) => {
    return formatTimeVN(startTime)
}

const handleBook = (showtimeId: number) => {
    if (!authStore.user) {
        alert('Vui lòng đăng nhập để đặt vé')
        return
    }
    emit('book', showtimeId)
}
</script>