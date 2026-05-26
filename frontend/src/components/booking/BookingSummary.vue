<script setup lang="ts">
import { inject, computed, ref, watch } from 'vue'
import { formatTimeVN, formatDateVN, formatDateHeaderVN } from '@/utils/dateFormat'
import AgeRatingTag from '@/components/movie/AgeRatingTag.vue'
import { roomApi } from '@/api/room.api'
import { getLanguageLabel } from '@/constants/languages'
import BaseButton from '@/components/ui/button/BaseButton.vue'

const props = defineProps<{
    currentStep?: number
}>()

function chunkArray<T>(arr: T[], size: number): T[][] {
    const result: T[][] = []
    for (let i = 0; i < arr.length; i += size) result.push(arr.slice(i, i + size))
    return result
}

const booking = inject<any>('booking')!
const movie = computed(() => {
    const m = booking.selectedMovie.value
    if (m?.title) return m

    // Fallback từ showtime
    const st = booking.selectedShowtime.value
    if (st?.movie?.title) return st.movie
    if (st?.title) {
        return {
            title: st.title,
            posterUrl: (st as any).posterUrl || '',
            ageRating: (st as any).ageRating || 'P'
        }
    }
    return null
})
const cinema = computed(() => booking.selectedCinema.value)
const showtime = computed(() => booking.selectedShowtime.value)
const seats = computed(() => booking.selectedSeats.value)
const combos = computed(() => booking.selectedCombos.value)
const coupon = computed(() => booking.appliedCoupon.value)

// ── Totals ────────────────────────────────────────────────────────────────────
const seatTotal = computed(() => seats.value.reduce((s: number, i: any) => s + i.price, 0))
const comboTotal = computed(() => combos.value.reduce((s: number, i: any) => s + i.totalPrice, 0))
const discount = computed(() => coupon.value?.discountValue ?? 0)
const total = computed(() => Math.max(0, seatTotal.value + comboTotal.value - discount.value))

defineEmits<{
    (e: 'prev'): void
    (e: 'next'): void
}>()

// ── Language / subtitle ───────────────────────────────────────────────────────
const languageLine = computed(() => {
    const st = showtime.value
    if (!st) return null
    const lang = st.audioLanguage ?? null
    const sub = st.subtitleLanguage ?? null
    if (!lang && !sub) return null
    if (lang && sub) return `${getLanguageLabel(lang)} · Phụ đề: ${getLanguageLabel(sub)}`
    if (lang) return getLanguageLabel(lang)
    return `Phụ đề: ${getLanguageLabel(sub)}`
})

// ── Room name ─────────────────────────────────────────────────────────────────
const roomNames = ref<Record<number, string>>({})

async function loadRoomName(roomId: number) {
    if (roomNames.value[roomId]) return
    try {
        const response = await roomApi.getById(roomId)
        const room = (response as any).data ?? response
        roomNames.value[roomId] = room.name ?? 'Unknown'
    } catch {
        roomNames.value[roomId] = 'Unknown'
    }
}

watch(
    () => showtime.value?.roomId,
    (id) => { if (id && !roomNames.value[id]) loadRoomName(id) },
    { immediate: true }
)

const seatGroups = computed(() => {
    const map = new Map<number, {
        seatTypeId: number
        seatsInfo: { label: string; row: number; col: number }[]
        subtotal: number
        typeName: string
        isCouple: boolean
    }>()

    for (const seat of seats.value) {
        const typeId = seat.seatTypeId
        const typeName = typeId === 2 ? 'Ghế VIP' : typeId === 3 ? 'Ghế đôi' : 'Ghế thường'

        if (!map.has(typeId)) {
            map.set(typeId, { seatTypeId: typeId, seatsInfo: [], subtotal: 0, typeName, isCouple: typeId === 3 })
        }
        const group = map.get(typeId)!
        group.seatsInfo.push({ label: seat.seatNumber, row: seat.rowIndex, col: seat.colIndex })
        group.subtotal += seat.price
    }

    for (const group of map.values()) {
        group.seatsInfo.sort((a, b) => a.row !== b.row ? a.row - b.row : b.col - a.col)
    }

    return Array.from(map.values()).map(group => {
        const labels = group.seatsInfo.map(i => i.label)
        return {
            seatType: group.typeName,
            subtotal: group.subtotal,
            isCouple: group.isCouple,
            // Ghế đôi: đếm theo cặp; ghế thường: đếm theo ghế
            displayCount: group.isCouple ? labels.length / 2 : labels.length,
            unit: group.isCouple ? 'cặp' : 'ghế',
            // Ghế đôi: ghép thành ["A1 & A2", "B3 & B4"]; ghế thường: ["A1", "A2"]
            displayLabels: group.isCouple
                ? chunkArray(labels, 2).map(pair => pair.join(' & '))
                : labels,
        }
    })
})

const logicalSeatCount = computed(() =>
    seats.value.reduce((count, seat) => {
        // Ghế đôi được count 0.5 mỗi ghế → 2 ghế = 1 cặp
        return count + (seat.seatTypeId === 3 ? 0.5 : 1)
    }, 0)
)

// ── Format badge ──────────────────────────────────────────────────────────────
const formatBadge = computed(() => {
    const st = showtime.value
    if (!st) return null
    if (st.formatName) return st.formatName
    const map: Record<number, string> = { 1: '2D', 2: '3D', 3: 'IMAX' }
    return map[st.formatId] ?? '2D'
})

// Disable next button based on step
const isNextDisabled = computed(() => {
    const step = props.currentStep || booking.currentStep?.value || 1
    if (step === 1) return false // handled separately
    if (step === 2) return !booking.selectedShowtime.value
    if (step === 3) return booking.selectedSeats.value.length === 0
    // Add more for other steps if needed
    return false
})

function showtimeLabel(iso: string): string {
    if (!iso) return ''
    const time = formatTimeVN(iso)
    const dateHeader = formatDateHeaderVN(iso)
    return `Suất: ${time} - ${dateHeader}`
}
</script>

<template>
    <div class="bg-bg-surface p-6 border border-border-default rounded-2xl overflow-hidden w-full">

        <div class="space-y-5">

            <!-- ① Phim — có dữ liệu -->
            <div v-if="movie" class="flex gap-4">
                <div class="w-25 h-38 rounded-xl bg-bg-elevated overflow-hidden shrink-0">
                    <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title"
                        class="w-full h-full object-cover" />
                    <div v-else class="w-full h-full flex items-center justify-center text-text-tertiary text-2xl">
                        🎬
                    </div>
                </div>

                <div class="min-w-0 flex-1 flex flex-col gap-1">
                    <p class="text-body font-semibold leading-tight line-clamp-2">
                        {{ movie.title }}
                    </p>

                    <!-- Rạp · Phòng -->
                    <p v-if="cinema" class="text-caption text-text-secondary">
                        {{ cinema.name }}
                        <template v-if="showtime">
                            · {{ roomNames[showtime.roomId] || '…' }}
                        </template>
                    </p>

                    <!-- Suất chiếu -->
                    <p v-if="showtime" class="text-caption text-text-secondary">
                        {{ showtimeLabel(showtime.startTime) }}
                    </p>

                    <!-- Ngôn ngữ · Phụ đề (cùng 1 hàng) -->
                    <p v-if="languageLine" class="text-caption text-text-tertiary">
                        {{ languageLine }}
                    </p>

                    <!-- Badges: age + format -->
                    <div v-if="showtime" class="flex items-center gap-2 mt-1">
                        <AgeRatingTag :rating="movie.ageRating" size="sm" />
                        <span v-if="formatBadge" class="px-2 h-6 text-xs bg-bg-elevated border border-border-subtle rounded
                                   flex items-center font-bold text-text-secondary">
                            {{ formatBadge }}
                        </span>
                    </div>
                </div>
            </div>

            <!-- ① Phim — chưa chọn (placeholder) -->
            <div v-else class="flex gap-4">
                <!-- Placeholder poster -->
                <div class="w-20 h-28 rounded-xl bg-bg-elevated border border-dashed border-border-default
                            shrink-0 flex items-center justify-center">
                    <span class="text-2xl opacity-40">🎬</span>
                </div>

                <div class="min-w-0 flex-1 flex flex-col justify-center gap-2">
                    <p class="text-body font-semibold text-text-tertiary">Vui lòng chọn phim</p>
                    <!-- Skeleton lines -->
                    <div class="h-3 w-3/4 rounded-full bg-bg-elevated animate-pulse" />
                    <div class="h-3 w-1/2 rounded-full bg-bg-elevated animate-pulse" />
                </div>
            </div>

            <!-- ── Divider + ghế / combo / coupon ── -->
            <template v-if="seats.length || combos.length || coupon">
                <hr class="border-border-subtle" />

                <div class="space-y-0 text-sm">

                    <!-- ② Ghế — gom nhóm theo loại -->
                    <template v-if="seatGroups.length">

                        <!-- Tiêu đề section ghế -->
                        <div class="flex justify-between items-center pb-3">
                            <span class="text-text-secondary font-medium">
                                Ghế ({{ logicalSeatCount }})
                            </span>
                            <span class="font-semibold">{{ seatTotal.toLocaleString() }}đ</span>
                        </div>

                        <!-- Từng nhóm loại ghế -->
                        <div v-for="(group, gi) in seatGroups" :key="group.seatType">
                            <hr v-if="gi > 0" class="border-border-subtle my-2.5" />

                            <div class="flex items-start justify-between gap-3 py-0.5">
                                <div class="min-w-0">
                                    <!-- x1 cặp / x2 ghế thường -->
                                    <span class="font-medium text-text-primary">
                                        x{{ group.displayCount }}
                                        <span class="text-text-secondary">{{ group.seatType }}</span>
                                    </span>
                                    <!-- Ghế thường: "A1, A2" | Ghế đôi: "A1 & A2" mỗi cặp 1 dòng -->
                                    <p class="text-caption text-text-primary mt-0.5 leading-relaxed">
                                        <template v-if="group.isCouple">
                                            <span v-for="(pair, pi) in group.displayLabels" :key="pi"
                                                class="inline-flex items-center gap-1 mr-2">
                                                {{ pair }} ,
                                            </span>
                                        </template>
                                        <template v-else>
                                            {{ group.displayLabels.join(', ') }}
                                        </template>
                                    </p>
                                </div>

                                <span class="text-accent font-semibold shrink-0 tabular-nums">
                                    {{ group.subtotal.toLocaleString() }}đ
                                </span>
                            </div>
                        </div>
                    </template>

                    <!-- Divider trước combo nếu có ghế -->
                    <hr v-if="seatGroups.length && (combos.length || coupon)" class="border-border-subtle my-3.5" />

                    <!-- ③ Combo -->
                    <div v-if="combos.length" class="space-y-2">
                        <div v-for="combo in combos" :key="combo.comboId" class="flex justify-between items-center">
                            <span class="text-text-secondary truncate">
                                {{ combo.comboName }}
                                <span class="text-text-tertiary ml-1">×{{ combo.quantity }}</span>
                            </span>
                            <span class="font-medium tabular-nums shrink-0 ml-3">
                                {{ combo.totalPrice.toLocaleString() }}đ
                            </span>
                        </div>
                    </div>

                    <!-- ④ Coupon -->
                    <div v-if="coupon" :class="['flex justify-between items-center text-green-500',
                        combos.length ? 'mt-2' : '']">
                        <span class="flex items-center gap-1.5">
                            <span>🎟</span>
                            <span class="font-medium">{{ coupon.code }}</span>
                        </span>
                        <span class="font-semibold tabular-nums shrink-0 ml-3">
                            −{{ coupon.discountValue.toLocaleString() }}đ
                        </span>
                    </div>
                </div>
            </template>

            <!-- Placeholder ghế khi chưa chọn (chỉ hiện nếu có phim đã chọn) -->
            <template v-else-if="movie">
                <hr class="border-border-subtle" />
                <div class="space-y-2">
                    <div class="h-3 w-full rounded-full bg-bg-elevated animate-pulse opacity-60" />
                    <div class="h-3 w-4/5 rounded-full bg-bg-elevated animate-pulse opacity-40" />
                </div>
            </template>

            <!-- ── Total ── -->
            <hr class="border-border-subtle" />
            <div class="flex justify-between items-center text-lg">
                <span class="text-text-secondary">Tổng cộng</span>
                <span class="font-bold text-accent tabular-nums">
                    {{ total.toLocaleString() }}đ
                </span>
            </div>

        </div>
        <div v-if="props.currentStep !== 1" class="flex gap-4 mt-6">
            <BaseButton variant="secondary" size="lg" class="flex-1" @click="$emit('prev')">
                Quay lại
            </BaseButton>
            <BaseButton variant="primary" size="lg" class="flex-1" @click="$emit('next')" :disabled="isNextDisabled">
                Tiếp tục
            </BaseButton>
        </div>
    </div>
</template>