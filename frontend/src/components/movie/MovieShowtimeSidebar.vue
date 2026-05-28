<template>
    <aside class="max-w-full p-3 space-y-3 bg-surface-dark text-text-primary rounded-xl
               xl:max-w-100 xl:p-5 xl:space-y-5">
        <!-- Header -->
        <h3 class="text-base font-bold flex items-center gap-2 xl:text-lg">
            <BaseIcon :icon="Clock" :size="24" :stroke-width="1.2" class="text-accent" />
            Lịch chiếu
        </h3>
        <template v-if="movieStatus === 'NOW_SHOWING'">
            <!-- Date Quick Selector -->
            <div class="space-y-2">
                <div class="flex items-center gap-2">
                    <!-- Prev -->
                    <button :disabled="weekOffset === 0" class="p-1 rounded-md transition-opacity"
                        :class="weekOffset === 0 ? 'opacity-20 cursor-not-allowed' : 'hover:text-accent'"
                        @click="prevWeek">
                        <BaseIcon :icon="ChevronLeft" :size="18" />
                    </button>

                    <!-- 7 date chips – responsive columns -->
                    <div class="grid grid-cols-3 gap-1 flex-1 md:grid-cols-4 xl:grid-cols-5">
                        <button v-for="d in dateOptions" :key="d.date" @click="selectDate(d.date)" :class="[
                            'py-2 px-0.5 rounded-lg text-xs font-medium transition-all outline outline-transparent text-center',
                            isSelectedDate(d.date)
                                ? 'bg-accent text-text-on-accent shadow-lg shadow-accent/25'
                                : 'hover:outline-accent text-text-primary'
                        ]">
                            <div class="leading-tight">{{ d.label }}</div>
                            <div class="text-text-secondary"
                                :class="isSelectedDate(d.date) ? 'text-text-on-accent/70' : ''">
                                {{ d.dateStr }}
                            </div>
                        </button>
                    </div>

                    <!-- Next -->
                    <button :disabled="weekOffset === maxWeekOffset" class="p-1 rounded-md transition-opacity"
                        :class="weekOffset === maxWeekOffset ? 'opacity-20 cursor-not-allowed' : 'hover:text-accent'"
                        @click="nextWeek">
                        <BaseIcon :icon="ChevronRight" :size="18" />
                    </button>
                </div>

                <!-- Indicator tuần hiện tại -->
                <p class="text-center text-xs text-text-secondary">
                    {{ weekOffset === 0 ? 'Tuần này' : `${weekOffset * 5 + 1}–${Math.min((weekOffset + 1) * 5, 30)} ngày
                    tới` }}
                </p>
            </div>

            <!-- Bộ lọc Thành phố & Rạp -->
            <div class="grid grid-cols-1 gap-3 xl:grid-cols-2">
                <!-- Thành phố -->
                <div>
                    <label class="block text-caption text-text-secondary mb-1">Thành phố</label>
                    <BaseDropdown :items="cityItems" trigger-type="click"
                        trigger-class="w-full p-2 border border-border-default ..." :full-width="true"
                        :show-chevron="true">
                        <template #trigger="{ isOpen }">
                            <div class="flex items-center justify-between w-full">
                                <span>{{ selectedCity || 'Tất cả' }}</span>
                                <BaseIcon :icon="isOpen ? ChevronUp : ChevronDown" :size="14" :scale="1.2"
                                    :stroke-width="1.5" />
                            </div>
                        </template>
                        <template #dropdown="{ close }">
                            <div v-for="item in cityItems" :key="item.value"
                                class="block px-4 py-2 text-sm text-text-primary hover:bg-accent hover:text-white cursor-pointer transition-colors first:rounded-t-md last:rounded-b-md"
                                @click="selectedCity = item.value; close()">
                                {{ item.label }}
                            </div>
                        </template>
                    </BaseDropdown>
                </div>

                <!-- Rạp -->
                <div>
                    <label class="block text-caption text-text-secondary mb-1">Rạp</label>
                    <BaseDropdown :items="cinemaItems" trigger-type="click"
                        trigger-class="w-full p-2 border border-border-default ..." :full-width="true"
                        :show-chevron="true">
                        <template #trigger="{ isOpen }">
                            <div class="flex items-center justify-between w-full">
                                <span>{{ selectedCinemaName || 'Tất cả rạp' }}</span>
                                <BaseIcon :icon="isOpen ? ChevronUp : ChevronDown" :size="14" :scale="1.2"
                                    :stroke-width="1.5" />
                            </div>
                        </template>
                        <template #dropdown="{ close }">
                            <div v-for="item in cinemaItems" :key="item.value"
                                class="block px-4 py-2 text-sm text-text-primary hover:bg-accent hover:text-white cursor-pointer transition-colors first:rounded-t-md last:rounded-b-md"
                                @click="selectedCinemaId = item.value; close()">
                                {{ item.label }}
                            </div>
                        </template>
                    </BaseDropdown>
                </div>
            </div>

            <!-- Loading / Error / Empty -->
            <div v-if="loading" class="py-10 text-center text-text-secondary">
                <div
                    class="inline-block w-6 h-6 border-2 border-accent border-t-transparent rounded-full animate-spin mb-2">
                </div>
                <p class="text-sm">Đang tải suất chiếu...</p>
            </div>
            <div v-else-if="error" class="py-6 text-center text-red-400 text-sm">
                {{ error }}
            </div>
            <div v-else-if="groupedShowtimes.length === 0" class="py-8 text-center text-text-secondary text-sm">
                Chưa có suất chiếu cho khu vực này.
            </div>

            <!-- Danh sách suất chiếu nhóm theo rạp -->
            <div v-else class="space-y-3 overflow-y-auto pr-1 custom-scrollbar xl:space-y-5">
                <div v-for="group in groupedShowtimes" :key="group.cinemaId" class="space-y-2">
                    <h4 class="font-semibold text-sm flex items-center gap-2 xl:text-base">
                        {{ group.cinemaName }}
                    </h4>
                    <div class="flex flex-wrap gap-2">
                        <ShowtimePill v-for="st in group.showtimes" :key="st.id" :showtime-id="st.id"
                            :format-name="st.formatName" :start-time="st.startTime" :end-time="st.endTime"
                            @book="handleBook" />
                    </div>
                </div>
            </div>
        </template>

        <template v-else-if="movieStatus === 'COMING_SOON'">
            <ComingSoonPanel :movie-id="movieId" />
        </template>
    </aside>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseDropdown from '@/components/ui/dropdown/BaseDropdown.vue'
import { useShowtimes } from '@/composables/useShowtimes'
import { formatTimeVN, getDateKeyVN } from '@/utils/dateFormat'
import type { ShowtimeResponse } from '@/types/showtime'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { ChevronLeft, ChevronRight, ChevronDown, ChevronUp, Clock } from 'lucide-vue-next'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import ShowtimePill from '@/components/showtime/ShowtimePill.vue'
import type { MovieStatus } from '@/types/movie'
import ComingSoonPanel from '@/components/movie/ComingSoonPanel.vue'

const props = defineProps<{
    movieId: number
    movieStatus: MovieStatus
}>()

const router = useRouter()

const {
    showtimes,
    loading,
    error,
    cities,
    filteredCinemas,
    selectedCity,
    selectedCinemaId,
    selectedDate,
    getCinemaById,
} = useShowtimes(props.movieId, { autoFetch: false })

onMounted(() => {
    if (!selectedDate.value) {
        const today = new Date()
        selectedDate.value = getDateKeyVN(today.toISOString())
    }
})

const MAX_DAYS = 30
const WEEK_SIZE = 5

const weekOffset = ref(0) // 0 = tuần hiện tại, 1 = tuần sau, ...
const today = new Date()

const maxWeekOffset = computed(() =>
    Math.floor((MAX_DAYS - 1) / WEEK_SIZE) // = 4
)

const dateOptions = computed(() => {
    const start = weekOffset.value * WEEK_SIZE
    const end = Math.min(start + WEEK_SIZE, MAX_DAYS)

    return Array.from({ length: end - start }, (_, i) => {
        const d = new Date(today)
        d.setDate(today.getDate() + start + i)
        const iso = d.toISOString()
        const dayIndex = start + i

        return {
            date: getDateKeyVN(iso),
            label: dayIndex === 0
                ? 'Hôm nay'
                : ['CN', 'T.Hai', 'T.Ba', 'T.Tư', 'T.Năm', 'T.Sáu', 'T.Bảy'][d.getDay()],
            dateStr: `${d.getDate()}/${d.getMonth() + 1}`,
        }
    })
})

// Tự động nhảy tuần nếu ngày được chọn không thuộc tuần hiện tại
const selectDate = (date: string) => {
    selectedDate.value = date
}

const prevWeek = () => { if (weekOffset.value > 0) weekOffset.value-- }
const nextWeek = () => { if (weekOffset.value < maxWeekOffset.value) weekOffset.value++ }
const isSelectedDate = (date: string) => selectedDate.value === date

const cityItems = computed(() => {
    const items = [{ label: 'Tất cả', value: '' }]
    cities.value.forEach(city => items.push({ label: city, value: city }))
    return items
})

const cinemaItems = computed(() => {
    const items = [{ label: 'Tất cả rạp', value: null as number | null }]
    filteredCinemas.value.forEach(cinema => items.push({ label: cinema.name, value: cinema.id }))
    return items
})

const selectedCinemaName = computed(() => {
    if (selectedCinemaId.value == null) return null
    const cinema = filteredCinemas.value.find(c => c.id === selectedCinemaId.value)
    return cinema?.name ?? null
})

const groupedShowtimes = computed(() => {
    const groups = new Map<number, {
        cinemaId: number
        cinemaName: string
        showtimes: Array<ShowtimeResponse & { formatName: string }>
    }>()

    showtimes.value.forEach(st => {
        // Lấy cinemaId một cách an toàn: ưu tiên nested object, sau đó đến trường cinemaId phẳng
        const cinemaId = st.cinema?.id ?? (st as any).cinemaId
        if (!cinemaId) return // bỏ qua nếu không xác định được rạp

        // Lấy tên rạp: ưu tiên nested object, không có thì dùng getCinemaById từ composable
        const cinemaName =
            st.cinema?.name ?? getCinemaById(cinemaId)?.name ?? `Rạp #${cinemaId}`

        if (!groups.has(cinemaId)) {
            groups.set(cinemaId, {
                cinemaId,
                cinemaName,
                showtimes: [],
            })
        }
        groups.get(cinemaId)!.showtimes.push({
            ...st,
            formatName: st.format?.name ?? '2D',
        })
    })

    return Array.from(groups.values())
})

const formatTime = (instant: string) => formatTimeVN(instant)

const handleBook = (showtimeId: number) => {
    router.push(`/bookings?showtimeId=${showtimeId}`)
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 2px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
}
</style>