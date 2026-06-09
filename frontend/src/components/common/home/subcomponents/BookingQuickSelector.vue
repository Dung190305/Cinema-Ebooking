<template>
    <section class="w-full flex items-center justify-center z-40">
        <div class="flex items-center max-w-280 w-fit h-18 gap-0 bg-bg-surface rounded-sm relative">

            <!-- Step 1: Chọn Phim -->
            <div class="relative" ref="movieDropdownRef">
                <div class="step-trigger w-56 lg:w-64 group cursor-pointer" @click="toggleMovieDropdown">
                    <div class="flex w-full justify-between items-center gap-2 min-w-0 px-3">
                        <div class="flex gap-2 items-center min-w-0">
                            <div class="step-badge step-badge--active">1</div>
                            <span
                                class="text-caption text-text-primary truncate font-medium group-hover:text-accent transition-colors">
                                {{ quick.selectedMovie.value?.title || 'Chọn Phim' }}
                            </span>
                        </div>
                        <BaseIcon :icon="isMovieDropdownOpen ? ChevronUp : ChevronDown" :size="12"
                            class="shrink-0 text-text-secondary group-hover:text-accent transition-colors pointer-events-none" />
                    </div>
                </div>

                <Transition name="dropdown">
                    <div v-if="isMovieDropdownOpen" class="dropdown-panel w-72">
                        <div v-if="quick.loadingMovies.value" class="p-4 text-center text-text-secondary text-sm">
                            Đang tải phim...
                        </div>
                        <template v-else>
                            <div v-if="quick.availableMovies.value.length === 0"
                                class="p-4 text-center text-text-tertiary text-sm">
                                Không có phim đang chiếu
                            </div>
                            <div v-for="movie in quick.availableMovies.value" :key="movie.id" class="dropdown-item"
                                :class="{ 'dropdown-item--selected': quick.selectedMovie.value?.id === movie.id }"
                                @click="() => selectMovie(movie)">
                                <span class="truncate">{{ movie.title }}</span>
                                <BaseIcon v-if="quick.selectedMovie.value?.id === movie.id" :icon="Check" :size="14"
                                    class="shrink-0 text-accent" />
                            </div>
                        </template>
                    </div>
                </Transition>
            </div>

            <!-- Divider -->
            <div class="w-px h-7 bg-white/10 shrink-0" />

            <!-- Step 2: Chọn Rạp -->
            <div class="relative" ref="cinemaDropdownRef">
                <div class="step-trigger w-48 lg:w-56 group"
                    :class="quick.selectedMovie.value ? 'cursor-pointer' : 'opacity-55 pointer-events-none'"
                    @click="toggleCinemaDropdown">
                    <div class="flex w-full justify-between items-center gap-2 min-w-0 px-3">
                        <div class="flex gap-2 items-center min-w-0">
                            <div class="step-badge"
                                :class="(isCinemaDropdownOpen || quick.selectedCinema.value) ? 'step-badge--active' : 'step-badge--inactive'">
                                2
                            </div>
                            <span class="text-caption truncate font-medium"
                                :class="quick.selectedMovie.value ? 'text-text-primary group-hover:text-accent transition-colors' : 'text-text-disabled'">
                                {{ quick.selectedCinema.value?.name || 'Chọn Rạp' }}
                            </span>
                        </div>
                        <BaseIcon :icon="isCinemaDropdownOpen ? ChevronUp : ChevronDown" :size="12"
                            class="shrink-0 transition-colors pointer-events-none"
                            :class="quick.selectedMovie.value ? 'text-text-secondary group-hover:text-accent' : 'text-text-disabled'" />
                    </div>
                </div>

                <Transition name="dropdown">
                    <div v-if="isCinemaDropdownOpen && quick.selectedMovie.value" class="dropdown-panel w-72">
                        <div v-if="quick.loadingCinemas.value" class="p-4 text-center text-text-secondary text-sm">
                            Đang tải rạp...
                        </div>
                        <template v-else>
                            <div v-if="quick.availableCinemas.value.length === 0"
                                class="p-4 text-center text-text-tertiary text-sm">
                                Không có rạp chiếu phim này
                            </div>
                            <div v-for="cinema in quick.availableCinemas.value" :key="cinema.id" class="dropdown-item"
                                :class="{ 'dropdown-item--selected': quick.selectedCinema.value?.id === cinema.id }"
                                @click="() => selectCinema(cinema)">
                                <div class="flex flex-col min-w-0">
                                    <span class="truncate font-medium">{{ cinema.name }}</span>
                                    <span class="text-xs text-text-tertiary truncate">{{ cinema.city }}</span>
                                </div>
                                <BaseIcon v-if="quick.selectedCinema.value?.id === cinema.id" :icon="Check" :size="14"
                                    class="shrink-0 text-accent" />
                            </div>
                        </template>
                    </div>
                </Transition>
            </div>

            <!-- Divider -->
            <div class="w-px h-7 bg-white/10 shrink-0" />

            <!-- Step 3: Chọn Ngày -->
            <div class="relative" ref="dateDropdownRef">
                <div class="step-trigger w-44 lg:w-52 group"
                    :class="quick.selectedCinema.value ? 'cursor-pointer' : 'opacity-55 pointer-events-none'"
                    @click="toggleDateDropdown">
                    <div class="flex w-full justify-between items-center gap-2 min-w-0 px-3">
                        <div class="flex gap-2 items-center min-w-0">
                            <div class="step-badge"
                                :class="(isDateDropdownOpen || quick.selectedDate.value) ? 'step-badge--active' : 'step-badge--inactive'">
                                3
                            </div>
                            <span class="text-caption truncate font-medium"
                                :class="quick.selectedCinema.value ? 'text-text-primary group-hover:text-accent transition-colors' : 'text-text-disabled'">
                                {{ selectedDateDisplay }}
                            </span>
                        </div>
                        <BaseIcon :icon="isDateDropdownOpen ? ChevronUp : ChevronDown" :size="12"
                            class="shrink-0 transition-colors pointer-events-none"
                            :class="quick.selectedCinema.value ? 'text-text-secondary group-hover:text-accent' : 'text-text-disabled'" />
                    </div>
                </div>

                <Transition name="dropdown">
                    <div v-if="isDateDropdownOpen && quick.selectedCinema.value" class="dropdown-panel w-56">
                        <div v-if="quick.loadingDates.value" class="p-4 text-center text-text-secondary text-sm">
                            Đang tải ngày...
                        </div>
                        <template v-else>
                            <div v-if="quick.availableDates.value.length === 0"
                                class="p-4 text-center text-text-tertiary text-sm">
                                Không có suất chiếu
                            </div>
                            <div v-for="date in quick.availableDates.value" :key="date" class="dropdown-item"
                                :class="{ 'dropdown-item--selected': quick.selectedDate.value === date }"
                                @click="() => selectDate(date)">
                                <span>{{ formatDateVN(date) }}</span>
                                <BaseIcon v-if="quick.selectedDate.value === date" :icon="Check" :size="14"
                                    class="shrink-0 text-accent" />
                            </div>
                        </template>
                    </div>
                </Transition>
            </div>

            <!-- Divider -->
            <div class="w-px h-7 bg-white/10 shrink-0" />

            <!-- Step 4: Chọn Suất -->
            <div class="relative" ref="showtimeDropdownRef">
                <div class="step-trigger w-56 lg:w-64 group"
                    :class="quick.selectedDate.value ? 'cursor-pointer' : 'opacity-55 pointer-events-none'"
                    @click="toggleShowtimeDropdown">
                    <div class="flex w-full justify-between items-center gap-2 min-w-0 px-3">
                        <div class="flex gap-2 items-center min-w-0">
                            <div class="step-badge"
                                :class="(isShowtimeDropdownOpen || quick.selectedShowtime.value) ? 'step-badge--active' : 'step-badge--inactive'">
                                4
                            </div>
                            <span class="text-caption truncate font-medium"
                                :class="quick.selectedDate.value ? 'text-text-primary group-hover:text-accent transition-colors' : 'text-text-disabled'">
                                {{ selectedShowtimeDisplay }}
                            </span>
                        </div>
                        <BaseIcon :icon="isShowtimeDropdownOpen ? ChevronUp : ChevronDown" :size="12"
                            class="shrink-0 transition-colors pointer-events-none"
                            :class="quick.selectedDate.value ? 'text-text-secondary group-hover:text-accent' : 'text-text-disabled'" />
                    </div>
                </div>

                <Transition name="dropdown">
                    <div v-if="isShowtimeDropdownOpen && quick.selectedDate.value" class="dropdown-panel w-64">
                        <div v-if="quick.loadingShowtimes.value" class="p-4 text-center text-text-secondary text-sm">
                            Đang tải suất chiếu...
                        </div>
                        <template v-else>
                            <div v-if="quick.availableShowtimes.value.length === 0"
                                class="p-4 text-center text-text-tertiary text-sm">
                                Không có suất chiếu
                            </div>
                            <div v-for="st in quick.availableShowtimes.value" :key="st.id" class="dropdown-item"
                                :class="{ 'dropdown-item--selected': quick.selectedShowtime.value?.id === st.id }"
                                @click="() => selectShowtime(st)">
                                <div class="flex flex-col min-w-0">
                                    <span class="font-medium">
                                        {{ formatTimeVN(st.startTime) }} - {{ formatTimeVN(st.endTime) }}
                                    </span>
                                </div>
                                <BaseIcon v-if="quick.selectedShowtime.value?.id === st.id" :icon="Check" :size="14"
                                    class="shrink-0 text-accent" />
                            </div>
                        </template>
                    </div>
                </Transition>
            </div>

            <!-- Nút Đặt vé ngay -->
            <div class="shrink-0 self-stretch flex">
                <button
                    class="text-text-on-accent bg-accent px-10 rounded-r-md py-2 text-body font-medium transition-colors disabled:text-text-secondary disabled:bg-accent/50 hover:bg-accent/90 disabled:pointer-events-none"
                    :disabled="!quick.selectedShowtime.value" @click="bookNow">
                    Đặt vé ngay
                </button>
            </div>
        </div>
    </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { ChevronDown, ChevronUp, Check } from 'lucide-vue-next'
import { useQuickBooking } from '@/composables/useQuickBooking'
import { formatDateVN, formatTimeVN } from '@/utils/dateFormat'

const router = useRouter()
const quick = useQuickBooking()

const isMovieDropdownOpen = ref(false)
const isCinemaDropdownOpen = ref(false)
const isDateDropdownOpen = ref(false)
const isShowtimeDropdownOpen = ref(false)

const movieDropdownRef = ref<HTMLElement | null>(null)
const cinemaDropdownRef = ref<HTMLElement | null>(null)
const dateDropdownRef = ref<HTMLElement | null>(null)
const showtimeDropdownRef = ref<HTMLElement | null>(null)

async function toggleMovieDropdown() {
    closeOthers('movie')
    if (!isMovieDropdownOpen.value) {
        await quick.loadMovies()
        await quick.loadAllCinemas()
    }
    isMovieDropdownOpen.value = !isMovieDropdownOpen.value
}

async function toggleCinemaDropdown() {
    if (!quick.selectedMovie.value) return
    closeOthers('cinema')
    if (!isCinemaDropdownOpen.value && quick.availableCinemas.value.length === 0) {
        await quick.loadShowtimesForMovie(quick.selectedMovie.value.id)
    }
    isCinemaDropdownOpen.value = !isCinemaDropdownOpen.value
}

async function toggleDateDropdown() {
    if (!quick.selectedCinema.value) return
    closeOthers('date')
    if (!isDateDropdownOpen.value && quick.availableDates.value.length === 0) {
        await quick.loadShowtimesForMovieCinema(
            quick.selectedMovie.value!.id,
            quick.selectedCinema.value.id
        )
    }
    isDateDropdownOpen.value = !isDateDropdownOpen.value
}

async function toggleShowtimeDropdown() {
    if (!quick.selectedDate.value) return
    closeOthers('showtime')
    if (!isShowtimeDropdownOpen.value && quick.availableShowtimes.value.length === 0) {
        await quick.loadShowtimesForAll(
            quick.selectedMovie.value!.id,
            quick.selectedCinema.value!.id,
            quick.selectedDate.value
        )
    }
    isShowtimeDropdownOpen.value = !isShowtimeDropdownOpen.value
}

function closeOthers(except: string) {
    if (except !== 'movie') isMovieDropdownOpen.value = false
    if (except !== 'cinema') isCinemaDropdownOpen.value = false
    if (except !== 'date') isDateDropdownOpen.value = false
    if (except !== 'showtime') isShowtimeDropdownOpen.value = false
}

async function selectMovie(movie: any) {
    await quick.selectMovie(movie)
    isMovieDropdownOpen.value = false
}

async function selectCinema(cinema: any) {
    await quick.selectCinema(cinema)
    isCinemaDropdownOpen.value = false
}

async function selectDate(date: string) {
    await quick.selectDate(date)
    isDateDropdownOpen.value = false
}

function selectShowtime(showtime: any) {
    quick.selectShowtime(showtime)
    isShowtimeDropdownOpen.value = false
}

function bookNow() {
    if (!quick.selectedShowtime.value) return
    router.push({ path: '/bookings', query: { showtimeId: quick.selectedShowtime.value.id } })
}

function handleClickOutside(event: MouseEvent) {
    const target = event.target as HTMLElement
    if (!movieDropdownRef.value?.contains(target) && isMovieDropdownOpen.value)
        isMovieDropdownOpen.value = false
    if (!cinemaDropdownRef.value?.contains(target) && isCinemaDropdownOpen.value)
        isCinemaDropdownOpen.value = false
    if (!dateDropdownRef.value?.contains(target) && isDateDropdownOpen.value)
        isDateDropdownOpen.value = false
    if (!showtimeDropdownRef.value?.contains(target) && isShowtimeDropdownOpen.value)
        isShowtimeDropdownOpen.value = false
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))

const selectedDateDisplay = computed(() =>
    quick.selectedDate.value ? formatDateVN(quick.selectedDate.value) : 'Chọn Ngày'
)

const selectedShowtimeDisplay = computed(() =>
    quick.selectedShowtime.value ? formatTimeVN(quick.selectedShowtime.value.startTime) + ' - ' + formatTimeVN(quick.selectedShowtime.value.endTime) : 'Chọn Suất'
)
</script>

<style scoped>
/* ── Step trigger base ── */
.step-trigger {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 12px;
    text-align: center;
    transition: background-color 0.15s ease;
}

/* ── Step number badge ── */
.step-badge {
    width: 22px;
    height: 22px;
    border-radius: 9999px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 700;
    flex-shrink: 0;
    transition: all 0.2s ease;
}

.step-badge--active {
    background-color: var(--color-accent);
    color: #fff;
}

.step-badge--inactive {
    border: 1.5px solid rgba(255, 255, 255, 0.2);
    color: rgba(255, 255, 255, 0.25);
    background: transparent;
}

/* ── Dropdown panel ── */
.dropdown-panel {
    position: absolute;
    bottom: calc(100% + 10px);
    left: 50%;
    transform: translateX(-50%);
    max-height: 320px;
    overflow-y: auto;
    background-color: var(--gray-800);
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.12);
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.6), 0 4px 16px rgba(0, 0, 0, 0.4);
    z-index: 50;
    padding: 6px;
    scrollbar-width: none;
}

.dropdown-panel::-webkit-scrollbar {
    display: none;
}

/* ── Dropdown items ── */
.dropdown-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 13px;
    font-weight: 500;
    color: rgba(255, 255, 255, 0.8);
    transition: background-color 0.12s ease, color 0.12s ease;
    margin-bottom: 2px;
}

.dropdown-item:last-child {
    margin-bottom: 0;
}

.dropdown-item:hover {
    background-color: rgba(255, 255, 255, 0.09);
    color: rgba(255, 255, 255, 0.95);
}

.dropdown-item--selected {
    background-color: rgba(201, 167, 78, 0.12);
    /* accent/12 */
    color: var(--color-accent);
}

.dropdown-item--selected:hover {
    background-color: rgba(201, 167, 78, 0.18);
}

/* ── Dropdown animation ── */
.dropdown-enter-active,
.dropdown-leave-active {
    transition: opacity 0.18s ease, transform 0.18s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
    opacity: 0;
    transform: translateX(-50%) translateY(-6px) scale(0.97);
}

.dropdown-enter-to,
.dropdown-leave-from {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
}
</style>