<!-- /home/workdir/attachments/MovieShowtimeSelection.vue -->
<script setup lang="ts">
import { inject, watch, computed, onMounted } from 'vue'
import { useShowtimes } from '@/composables/useShowtimes'
import CalendarPicker from '@/components/ui/calendar/CalendarPicker.vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { Calendar } from 'lucide-vue-next'
import ShowtimePill from '@/components/showtime/ShowtimePill.vue'

import type { BookingState } from '@/composables/useBooking'
import type { ShowtimeResponse } from '@/types/showtime'
import { formatTimeVN, formatDateHeaderVN, getDateKeyVN } from '@/utils/dateFormat'

const emit = defineEmits(['next', 'prev'])
const booking = inject<BookingState>('booking')!

const {
    showtimes,
    loading,
    selectedDate,
    selectedCinemaId,
    getMovieById,
    fetchShowtimes,
} = useShowtimes(undefined, { autoFetch: false })

onMounted(async () => {
    if (booking.selectedCinema.value) {
        selectedCinemaId.value = booking.selectedCinema.value.id
        await fetchShowtimes()
    }
})

watch(() => booking.selectedCinema.value, async (cinema) => {
    if (cinema) {
        selectedCinemaId.value = cinema.id
        await fetchShowtimes()
    }
})

const showtimesByMovie = computed(() => {
    const map = new Map<number, { movie: any; times: ShowtimeResponse[] }>()
    for (const st of showtimes.value) {
        const movieId = st.movieId
        if (!movieId) continue
        const movie = getMovieById(movieId)
        if (!map.has(movieId)) {
            map.set(movieId, {
                movie: movie ?? { id: movieId, title: `Phim #${movieId}`, posterUrl: '', duration: 0, ageRating: '', genres: [] },
                times: []
            })
        }
        map.get(movieId)!.times.push(st)
    }
    const groups = Array.from(map.values())
    for (const group of groups) group.times.sort((a, b) => (a.startTime ?? '').localeCompare(b.startTime ?? ''))
    return groups.sort((a, b) => (a.times[0]?.startTime ?? '').localeCompare(b.times[0]?.startTime ?? ''))
})

// Group by date for clearer display
const showtimesByDateAndMovie = computed(() => {
    const dateMap = new Map<string, any[]>()

    showtimesByMovie.value.forEach(group => {
        const dateGroups = new Map<string, ShowtimeResponse[]>()
        group.times.forEach(st => {
            if (!st.startTime) return
            const dateKey = getDateKeyVN(st.startTime)
            if (!dateGroups.has(dateKey)) dateGroups.set(dateKey, [])
            dateGroups.get(dateKey)!.push(st)
        })

        dateGroups.forEach((times, dateKey) => {
            if (!dateMap.has(dateKey)) dateMap.set(dateKey, [])
            dateMap.get(dateKey)!.push({
                ...group,
                times
            })
        })
    })

    // Sort dates
    return Array.from(dateMap.entries())
        .sort((a, b) => a[0].localeCompare(b[0]))
        .map(([dateKey, groups]) => ({
            dateKey,
            dateHeader: formatDateHeaderVN(dateKey),
            groups
        }))
})

function selectShowtime(st: ShowtimeResponse) {
    booking.selectedShowtime.value = st
    booking.selectedMovie.value = getMovieById(st.movieId) ?? null
    emit('next')
}

// Expose method next để validate
function validateAndNext() {
    if (!booking.selectedShowtime.value) {
        alert('Vui lòng chọn suất chiếu')
        return false
    }
    emit('next')
    return true
}
defineExpose({ next: validateAndNext })

// Helper functions
function getFormatBadge(st: ShowtimeResponse): string {
    const map: Record<number, string> = { 1: '2D', 2: '3D', 3: 'IMAX' }
    return map[st.formatId] ?? '2D'
}
function getFormatClass(st: ShowtimeResponse): string {
    const f = getFormatBadge(st)
    if (f === 'IMAX') return 'bg-yellow-500/15 text-yellow-400 border-yellow-500/30'
    if (f === '3D') return 'bg-blue-500/15 text-blue-400 border-blue-500/30'
    return 'bg-bg-elevated text-text-secondary border-border-default'
}
function ageRatingClass(rating: string): string {
    if (rating === 'T18') return 'bg-red-500/20 text-red-400'
    if (rating === 'T16') return 'bg-orange-500/20 text-orange-400'
    if (rating === 'T13') return 'bg-yellow-500/20 text-yellow-500'
    return 'bg-green-500/20 text-green-400'
}
function formatShowtimeVN(iso: string): string {
    return iso ? formatTimeVN(iso) : ''
}

const showtimesByMovieFiltered = computed(() => {
    if (!selectedDate.value) return showtimesByDateAndMovie.value

    const targetDateKey = getDateKeyVN(selectedDate.value)
    return showtimesByDateAndMovie.value.filter(dg => dg.dateKey === targetDateKey)
})
</script>

<template>
    <div>
        <!-- Header -->
        <div class="flex justify-between items-center mb-6">
            <div>
                <h2 class="text-title">Chọn phim & suất chiếu</h2>
                <p v-if="booking.selectedCinema.value" class="text-caption text-text-secondary mt-1">
                    {{ booking.selectedCinema.value.name }}
                </p>
            </div>
        </div>

        <!-- Calendar Picker -->
        <div class="mb-6">
            <p class="text-caption text-text-secondary mb-2">Chọn ngày chiếu</p>
            <CalendarPicker v-model="selectedDate" mode="date" :minDate="new Date()" class="w-full max-w-xs" />
        </div>

        <!-- Loading -->
        <div v-if="loading" class="flex flex-col items-center py-16">
            <svg class="animate-spin h-8 w-8 text-accent" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
            </svg>
            <p class="mt-4 text-text-tertiary">Đang tải suất chiếu...</p>
        </div>

        <!-- Danh sách phim grouped by date -->
        <div v-else class="space-y-8">
            <div v-for="dateGroup in showtimesByMovieFiltered" :key="dateGroup.dateKey" class="space-y-4">
                <!-- Date Header -->
                <div class="sticky top-0 z-10 py-2 pl-4 border-b">
                    <h3 class="text-lg font-semibold text-text-primary flex items-center gap-2">
                        <BaseIcon :icon="Calendar" :size="20" class="text-accent" />
                        {{ dateGroup.dateHeader }}
                    </h3>
                </div>

                <div v-for="group in dateGroup.groups" :key="group.movie.id"
                    class="bg-bg-surface border border-border-default rounded-2xl overflow-hidden">

                    <div class="flex gap-4 p-4">
                        <!-- Poster -->
                        <div class="w-24 shrink-0 relative rounded-xl overflow-hidden bg-bg-elevated">
                            <img v-if="group.movie.posterUrl" :src="group.movie.posterUrl" :alt="group.movie.title"
                                class="w-full h-full object-cover" />
                            <!-- Age Rating -->
                            <span v-if="group.movie.ageRating"
                                class="absolute top-2 left-2 text-[10px] font-bold px-2 py-0.5 rounded"
                                :class="ageRatingClass(group.movie.ageRating)">
                                {{ group.movie.ageRating }}
                            </span>
                        </div>

                        <!-- Info -->
                        <div class="flex-1">
                            <p class="font-semibold text-body leading-tight">{{ group.movie.title }}</p>
                            <div class="flex gap-2 text-caption text-text-tertiary mt-1">
                                <span v-if="group.movie.duration">{{ group.movie.duration }} phút</span>
                                <span v-if="group.movie.genres?.length">
                                    {{group.movie.genres.slice(0, 2).map((g: any) => g.name ?? g).join(' · ')}}
                                </span>
                            </div>

                            <div class="flex flex-wrap gap-2 mt-4">
                                <ShowtimePill v-for="st in group.times" :key="st.id" :showtime-id="st.id"
                                    :format-name="getFormatBadge(st)" :start-time="st.startTime" :end-time="st.endTime"
                                    size="md" :active="booking.selectedShowtime.value?.id === st.id"
                                    @book="(id) => selectShowtime(group.times.find(t => t.id === id)!)" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Empty -->
        <div v-if="!loading && showtimesByMovieFiltered.length === 0" class="text-center py-20 text-text-tertiary">
            <p class="text-4xl mb-3">🎬</p>
            <p>Không có suất chiếu nào trong ngày đã chọn</p>
            <p class="text-sm mt-1">Vui lòng chọn ngày khác</p>
        </div>
    </div>
</template>