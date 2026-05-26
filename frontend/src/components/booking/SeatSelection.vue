<template>
    <div>
        <!-- Login Prompt Modal -->
        <div v-if="showLoginPrompt"
            class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
            <div class="bg-bg-surface border border-border-default rounded-2xl p-8 max-w-sm w-full mx-4 shadow-2xl">
                <div class="text-center mb-6">
                    <div class="text-5xl mb-3">🔐</div>
                    <h3 class="text-title mb-2">Đăng nhập để tiếp tục</h3>
                    <p class="text-text-secondary text-body">Bạn cần đăng nhập để chọn ghế và đặt vé.</p>
                </div>
                <div class="flex flex-col gap-3">
                    <button
                        class="w-full py-3 bg-accent text-text-on-accent rounded-xl font-semibold hover:brightness-110 transition"
                        @click="goToLogin">
                        Đăng nhập ngay
                    </button>
                    <button
                        class="w-full py-3 border border-border-default text-text-secondary rounded-xl font-medium hover:border-accent transition"
                        @click="showLoginPrompt = false">
                        Để sau
                    </button>
                </div>
            </div>
        </div>

        <!-- Danh sách tất cả suất chiếu trong ngày -->
        <div class="mb-6 bg-bg-surface border border-border-default rounded-xl px-6 py-4">
            <p class="text-body font-bold text-text-primary mb-3">Đổi suất chiếu</p>
            <div v-if="allShowtimes.length > 0" class="flex flex-wrap gap-2">
                <ShowtimePill v-for="st in allShowtimes" :key="st.id" :showtime-id="st.id"
                    :format-name="getFormatBadge(st)" :start-time="st.startTime" :end-time="st.endTime" size="md"
                    :active="isCurrentShowtime(st.id)" @book="(id) => changeShowtime(st)" />
            </div>

            <div v-else-if="!loadingOtherShowtimes" class="text-caption text-text-tertiary text-center py-4">
                Không có suất chiếu nào trong ngày này
            </div>
        </div>

        <!-- Loading ban đầu -->
        <div v-if="loading" class="flex flex-col items-center justify-center py-20 gap-3">
            <svg class="animate-spin h-8 w-8 text-accent" xmlns="http://www.w3.org/2000/svg" fill="none"
                viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
            </svg>
            <p class="text-text-tertiary text-caption">Đang tải sơ đồ ghế…</p>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="flex flex-col items-center justify-center py-20 gap-3">
            <p class="text-red-400 text-body">{{ error }}</p>
            <button
                class="px-4 py-2 border border-accent text-accent rounded-lg text-body hover:bg-accent/10 transition"
                @click="retry">
                Thử lại
            </button>
        </div>

        <!-- No showtime selected -->
        <div v-else-if="!booking.selectedShowtime.value"
            class="flex items-center justify-center py-20 text-text-tertiary text-caption">
            Chưa chọn suất chiếu
        </div>

        <!-- Seat Grid -->
        <div v-else-if="adapted"
            class="relative bg-bg-surface border border-border-default rounded-xl p-4 lg:p-6 overflow-x-auto">

            <!-- Loading overlay khi fetch lại sơ đồ ghế -->
            <div v-if="isRefetching"
                class="absolute inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-10 rounded-xl transition-opacity duration-200">
                <div class="flex flex-col items-center gap-3 text-center">
                    <svg class="animate-spin h-9 w-9 text-accent" xmlns="http://www.w3.org/2000/svg" fill="none"
                        viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                    </svg>
                    <p class="text-text-primary text-body">Đang cập nhật sơ đồ ghế...</p>
                </div>
            </div>

            <SeatGrid :layout="adapted.grid" :config="webSeatGridConfig" :selected-ids="selectedIds"
                :booked-ids="adapted.bookedIds" :locked-ids="adapted.lockedIds" :show-booking-legend="true"
                @seat-click="onSeatClick" @couple-click="onCoupleClick" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, inject, watch, computed } from 'vue'
import { showtimeApi } from '@/api/showtime.api'
import { useAuthStore } from '@/stores/auth.store'
import { useUIStore } from '@/stores/ui.store'
import SeatGrid from '@/components/seat/SeatGrid.vue'
import { webSeatGridConfig } from '@/components/seat/seatGridConfig'
import ShowtimePill from '@/components/showtime/ShowtimePill.vue'
import { formatTimeVN, formatDateHeaderVN, getDateKeyVN } from '@/utils/dateFormat'
import { useShowtimes } from '@/composables/useShowtimes'
import type { ShowtimeSeatLayoutResponse, ShowtimeSeatResponse } from '@/types/showtime-seat'
import type { RoomLayoutResponse, SeatResponse } from '@/types/seat'

const emit = defineEmits(['next', 'prev'])
const booking = inject<any>('booking')!
const authStore = useAuthStore()
const uiStore = useUIStore()

// ── Auth gate ─────────────────────────────────────────────────────────────────
const showLoginPrompt = ref(false)

function requireLogin() {
    if (!authStore.isLoggedIn) {
        showLoginPrompt.value = true
        return false
    }
    return true
}

function goToLogin() {
    showLoginPrompt.value = false
    uiStore.openLoginModal()
}

// ── Raw state ────────────────────────────────────────────────────────────────
const rawLayout = ref<ShowtimeSeatLayoutResponse | null>(null)
const loading = ref(false)           // Loading ban đầu
const isRefetching = ref(false)      // Loading khi fetch lại (overlay)
const error = ref('')

const fullSeatMap = ref<Record<number, ShowtimeSeatResponse>>({})
const couplePairMap = ref<Record<number, number>>({})

const TYPE_NAME_TO_ID: Record<string, number> = {
    STANDARD: 1, NORMAL: 1, ECONOMY: 1,
    VIP: 2,
    COUPLE: 3, DOUBLE: 3,
}

interface AdaptedLayout {
    grid: RoomLayoutResponse
    bookedIds: number[]
    lockedIds: number[]
    priceMap: Record<number, number>
    nameMap: Record<number, string>
    typeMap: Record<number, number>
}

function adaptLayout(raw: ShowtimeSeatLayoutResponse): AdaptedLayout {
    const flatSeats: ShowtimeSeatResponse[] = []
    for (const row of raw.rows ?? []) {
        for (const seat of row ?? []) {
            if (seat != null) flatSeats.push(seat)
        }
    }

    const maxRow = raw.totalRows ?? raw.rows?.length ?? 0
    const maxCol = raw.totalCols ?? (raw.rows?.[0]?.length ?? 0)

    const rows: (SeatResponse | null)[][] = Array.from({ length: maxRow }, () => new Array(maxCol).fill(null))

    const bookedIds: number[] = []
    const lockedIds: number[] = []
    const priceMap: Record<number, number> = {}
    const nameMap: Record<number, string> = {}
    const typeMap: Record<number, number> = {}
    const newPairMap: Record<number, number> = {}
    const newFullSeatMap: Record<number, ShowtimeSeatResponse> = {}

    for (const s of flatSeats) {
        const r = s.rowIndex - 1
        const c = s.colIndex - 1
        if (r < 0 || r >= maxRow || c < 0 || c >= maxCol) continue

        const seatTypeId = s.seatTypeId ?? TYPE_NAME_TO_ID[String(s.seatTypeId ?? '').toUpperCase()] ?? 1

        rows[r][c] = {
            id: s.id,
            rowIndex: s.rowIndex,
            colIndex: s.colIndex,
            seatTypeId,
            status: s.status,
            label: s.seatNumber,
        } as SeatResponse

        if (s.status === 'BOOKED') bookedIds.push(s.id)
        if (s.status === 'LOCKED') lockedIds.push(s.id)

        priceMap[s.id] = s.price
        nameMap[s.id] = s.seatNumber
        typeMap[s.id] = seatTypeId
        newFullSeatMap[s.id] = s
    }

    const groupByCoupleId: Record<number, ShowtimeSeatResponse[]> = {}
    for (const s of flatSeats) {
        if (s.seatTypeId === 3 && s.coupleGroupId != null) {
            const gid = s.coupleGroupId
            if (!groupByCoupleId[gid]) groupByCoupleId[gid] = []
            groupByCoupleId[gid].push(s)
        }
    }
    for (const group of Object.values(groupByCoupleId)) {
        if (group.length === 2) {
            newPairMap[group[0].id] = group[1].id
            newPairMap[group[1].id] = group[0].id
        }
    }

    // Fallback couple detection
    if (Object.keys(newPairMap).length === 0) {
        for (let r = 0; r < maxRow; r++) {
            for (let c = 0; c < maxCol - 1; c++) {
                const left = rows[r][c]
                const right = rows[r][c + 1]
                if (left && right && left.seatTypeId === 3 && right.seatTypeId === 3) {
                    if (!newPairMap[left.id] && !newPairMap[right.id]) {
                        newPairMap[left.id] = right.id
                        newPairMap[right.id] = left.id
                    }
                }
            }
        }
    }

    couplePairMap.value = newPairMap
    fullSeatMap.value = newFullSeatMap

    return {
        grid: { rows: rows as SeatResponse[][], totalCols: maxCol } as RoomLayoutResponse,
        bookedIds, lockedIds, priceMap, nameMap, typeMap
    }
}

const adapted = computed<AdaptedLayout | null>(() => rawLayout.value ? adaptLayout(rawLayout.value) : null)

// Watch showtime change → load seat map
watch(() => booking.selectedShowtime.value, async (st) => {
    if (!st) return
    loading.value = true
    error.value = ''
    rawLayout.value = null
    booking.selectedSeats.value = []
    try {
        rawLayout.value = await showtimeApi.getSeatMap(st.id)
    } catch {
        error.value = 'Không thể tải sơ đồ ghế. Vui lòng thử lại.'
    } finally {
        loading.value = false
    }
}, { immediate: true })

// Selection logic
const selectedSeats = computed({
    get: () => booking.selectedSeats.value as ShowtimeSeatResponse[],
    set: (val) => { booking.selectedSeats.value = val }
})

const selectedIds = computed(() => selectedSeats.value.map(s => s.id))

function onSeatClick(seat: SeatResponse) {
    if (!requireLogin()) return
    const fullSeat = fullSeatMap.value[seat.id]
    if (!fullSeat) return

    // Ghế đôi (type 3) → tìm partner và xử lý theo cặp
    if (fullSeat.seatTypeId === 3) {
        const partnerId = couplePairMap.value[seat.id]
        if (partnerId) {
            const partnerGrid = adapted.value?.grid.rows
                .flat()
                .find(s => s?.id === partnerId) ?? null
            if (partnerGrid) {
                onCoupleClick(seat, partnerGrid)
                return
            }
        }
        // Không tìm được partner → vẫn cho chọn đơn (edge case)
    }

    // Ghế thường — toggle bình thường
    const idx = selectedSeats.value.findIndex(s => s.id === fullSeat.id)
    if (idx >= 0) {
        selectedSeats.value = selectedSeats.value.filter((_, i) => i !== idx)
    } else {
        selectedSeats.value = [...selectedSeats.value, fullSeat]
    }
}

function onCoupleClick(left: SeatResponse, right: SeatResponse) {
    if (!requireLogin()) return
    const fullLeft = fullSeatMap.value[left.id]
    const fullRight = fullSeatMap.value[right.id]
    if (!fullLeft || !fullRight) return

    const leftSelected = selectedSeats.value.some(s => s.id === fullLeft.id)
    const rightSelected = selectedSeats.value.some(s => s.id === fullRight.id)

    if (leftSelected && rightSelected) {
        selectedSeats.value = selectedSeats.value.filter(s => s.id !== fullLeft.id && s.id !== fullRight.id)
    } else {
        const toAdd = []
        if (!leftSelected) toAdd.push(fullLeft)
        if (!rightSelected) toAdd.push(fullRight)
        if (toAdd.length) selectedSeats.value = [...selectedSeats.value, ...toAdd]
    }
}

const totalPrice = computed(() => selectedSeats.value.reduce((sum, s) => sum + s.price, 0))
const seatCount = computed(() => selectedIds.value.length)
const coupleCount = computed(() =>
    selectedSeats.value.filter(s => s.seatTypeId === 3).length / 2
)
const standardCount = computed(() =>
    selectedSeats.value.filter(s => s.seatTypeId !== 3).length
)
async function retry() {
    const st = booking.selectedShowtime.value
    if (!st) return

    isRefetching.value = true
    error.value = ''

    try {
        rawLayout.value = await showtimeApi.getSeatMap(st.id)
    } catch {
        error.value = 'Không thể tải sơ đồ ghế. Vui lòng thử lại.'
    } finally {
        isRefetching.value = false
    }
}

function validateAndNext() {
    if (!requireLogin()) return false
    if (seatCount.value === 0) {
        alert('Vui lòng chọn ít nhất một ghế')
        return false
    }
    emit('next')
    return true
}

defineExpose({ next: validateAndNext })

// ── All Showtimes in Day ─────────────────────────────────────────────────────
const currentShowtime = computed(() => booking.selectedShowtime.value)

function getFormatBadge(st: any): string {
    if (!st) return ''
    if (st.formatName) return st.formatName
    const map: Record<number, string> = { 1: '2D', 2: '3D', 3: 'IMAX' }
    return map[st.formatId] ?? '2D'
}

function isCurrentShowtime(id: number): boolean {
    return currentShowtime.value?.id === id
}

// Other showtimes
const {
    showtimes: otherShowtimesRaw,
    loading: loadingOtherShowtimes,
    selectedCinemaId,
    selectedDate,
    fetchShowtimes: fetchOtherShowtimes
} = useShowtimes(undefined, { autoFetch: false })

const allShowtimes = computed(() => {
    const current = currentShowtime.value
    if (!current) return otherShowtimesRaw.value

    const hasCurrent = otherShowtimesRaw.value.some(st => st.id === current.id)
    if (hasCurrent) return otherShowtimesRaw.value

    return [current, ...otherShowtimesRaw.value]
})

watch([() => booking.selectedShowtime.value, () => booking.selectedCinema.value], async ([st, cinema]) => {
    if (st && cinema) {
        selectedCinemaId.value = cinema.id
        selectedDate.value = getDateKeyVN(st.startTime)
        await fetchOtherShowtimes()
    } else {
        selectedCinemaId.value = null
        selectedDate.value = ''
        otherShowtimesRaw.value = []
    }
}, { immediate: true })

function changeShowtime(newShowtime: any) {
    if (newShowtime.id === currentShowtime.value?.id) return
    booking.selectedShowtime.value = newShowtime
    booking.selectedSeats.value = []
}
</script>