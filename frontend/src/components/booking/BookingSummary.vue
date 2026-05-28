<script setup lang="ts">
import { inject, computed, ref, watch } from 'vue'
import { formatTimeVN, formatDateVN, formatDateHeaderVN } from '@/utils/dateFormat'
import AgeRatingTag from '@/components/movie/AgeRatingTag.vue'
import { roomApi } from '@/api/room.api'
import { getLanguageLabel } from '@/constants/languages'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { Clock } from 'lucide-vue-next'

const seatLock = inject<ReturnType<typeof import('@/composables/useSeatLock').useSeatLock>>('seatLock')!

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

const seatTotal = computed(() => booking.seatTotal?.value ?? 0)
const total = computed(() => booking.grandTotal?.value ?? 0)

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
                ? chunkArray(labels, 2).map(pair => pair.join(' - '))
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

const isNextDisabled = computed(() => {
    const step = props.currentStep || booking.currentStep?.value || 1
    if (step === 2) return !booking.selectedShowtime.value
    if (step === 3) return booking.selectedSeats.value.length === 0
    if (step === 4) return false // combo là optional
    if (step === 5) return false // payment tự validate khi submit
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
    <!-- Lock timer bar – thêm ring, gradient nền tinh tế, hiệu ứng sắp hết giờ rõ hơn -->
    <div v-if="seatLock?.hasActiveLock && props.currentStep && props.currentStep >= 4" class="relative overflow-hidden bg-lỉnear-to-r from-accent/5 via-accent/10 to-accent/5 backdrop-blur-xl
           border border-accent/25 rounded-full px-5 py-2.5 flex items-center justify-between
           shadow-[0_4px_20px_-4px_rgba(var(--color-accent-rgb),0.2),0_0_0_1px_rgba(var(--color-accent-rgb),0.1)_inset]
           ring-1 ring-accent/10 mb-5 transition-all duration-500" :class="{
            'animate-pulse border-error/40 bg-lỉnear-to-r from-error/10 via-error/15 to-error/10 shadow-[0_4px_20px_-4px_rgba(var(--color-error-rgb),0.3),0_0_0_1px_rgba(var(--color-error-rgb),0.15)_inset] ring-error/15':
                seatLock.timeLeft < 60
        }">
        <div class="flex items-center gap-2 text-accent">
            <BaseIcon :icon="Clock" :size="18" class="text-current drop-shadow-sm" />
            <span class="text-xs font-semibold tracking-wide uppercase">Giữ ghế</span>
        </div>
        <div class="font-mono text-lg font-bold tabular-nums drop-shadow-sm"
            :class="seatLock.timeLeft < 60 ? 'text-error' : 'text-accent'">
            {{ seatLock.formattedTime }}
        </div>
    </div>

    <!-- Card chính – nhiều lớp shadow, ring, gradient viền, glow tinh tế -->
    <div
        class="relative bg-bg-surface mt-6 p-6 rounded-2xl overflow-hidden w-full
           shadow-[0_8px_30px_-12px_rgba(0,0,0,0.3),0_20px_60px_-20px_rgba(0,0,0,0.2),0_0_0_1px_rgba(255,255,255,0.05)_inset]
           ring-1 ring-white/5
           before:absolute before:inset-0 before:rounded-2xl before:bg-linear-to-b before:from-white/3 before:to-transparent before:pointer-events-none">
        <!-- Đường gradient top – rực rỡ hơn với glow -->
        <div class="absolute top-0 left-0 right-0 h-0.5 bg-lỉnear-to-r from-transparent via-accent/60 to-transparent
             shadow-[0_0_12px_rgba(var(--color-accent-rgb),0.4)]" />

        <div class="space-y-5">
            <!-- ① Phim — có dữ liệu -->
            <div v-if="movie" class="flex gap-4">
                <!-- Poster – ring + shadow + overlay gradient tinh tế -->
                <div class="relative w-25 h-38 rounded-xl bg-bg-elevated overflow-hidden shrink-0
                 shadow-[0_8px_24px_-8px_rgba(0,0,0,0.4),0_0_0_1px_rgba(255,255,255,0.06)]
                 ring-1 ring-white/10
                 after:absolute after:inset-0 after:rounded-xl after:bg-linear-to-t after:from-black/20 after:to-transparent after:pointer-events-none
                 transition-transform duration-300 hover:scale-[1.02]">
                    <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title"
                        class="w-full h-full object-cover" />
                    <div v-else class="w-full h-full flex items-center justify-center text-text-tertiary">
                        <!-- Thay emoji bằng icon film từ BaseIcon (hoặc fallback SVG) -->
                        <BaseIcon v-if="false" :icon="'Film'" :size="32" />
                        <svg v-else class="w-8 h-8 opacity-40" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="1.5">
                            <rect x="2" y="4" width="20" height="16" rx="2" />
                            <path d="M2 8h20M2 16h20M8 4v16M16 4v16" />
                        </svg>
                    </div>
                </div>

                <div class="min-w-0 flex-1 flex flex-col gap-1">
                    <p class="text-body font-semibold leading-tight line-clamp-2">
                        {{ movie.title }}
                    </p>

                    <p v-if="cinema" class="text-caption text-text-secondary">
                        {{ cinema.name }}
                        <template v-if="showtime"> · {{ roomNames[showtime.roomId] || '…' }} </template>
                    </p>

                    <p v-if="showtime" class="text-caption text-text-secondary">
                        {{ showtimeLabel(showtime.startTime) }}
                    </p>

                    <p v-if="languageLine" class="text-caption text-text-tertiary">
                        {{ languageLine }}
                    </p>

                    <!-- Badges – thêm ring + shadow -->
                    <div v-if="showtime" class="flex items-center gap-2 mt-1">
                        <AgeRatingTag :rating="movie.ageRating" size="sm" />
                        <span v-if="formatBadge" class="px-2 h-6 text-xs bg-bg-elevated border border-border-subtle rounded
                     flex items-center font-bold text-text-secondary
                     shadow-sm ring-1 ring-white/5">
                            {{ formatBadge }}
                        </span>
                    </div>
                </div>
            </div>

            <div v-else class="flex gap-4">
                <!-- Poster placeholder -->
                <div class="w-25 h-38 rounded-xl bg-bg-elevated border border-dashed border-border-default
         shrink-0 flex items-center justify-center
         shadow-[0_4px_12px_-6px_rgba(0,0,0,0.2)]">
                    <!-- Icon Play tối giản, màu accent -->
                    <svg class="w-10 h-10 text-accent/40" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M8 5.14v14l11-7-11-7z" />
                    </svg>
                </div>

                <div class="min-w-0 flex-1 flex flex-col justify-center gap-3">
                    <p class="text-sm font-medium text-accent/60 tracking-wide uppercase">Vui lòng chọn phim</p>
                    <div class="space-y-2.5">
                        <div class="h-3 w-4/5 rounded-full bg-accent/20 animate-pulse"></div>
                        <div class="h-3 w-3/5 rounded-full bg-accent/15 animate-pulse" style="animation-delay: 100ms">
                        </div>
                        <div class="h-3 w-2/5 rounded-full bg-accent/10 animate-pulse" style="animation-delay: 200ms">
                        </div>
                    </div>
                </div>
            </div>

            <!-- ── Divider gradient + ghế / combo / coupon ── -->
            <template v-if="seats.length || combos.length || coupon">
                <hr class="border-0 h-px bg-lỉnear-to-r from-transparent via-border-subtle to-transparent" />

                <div class="space-y-0 text-sm">
                    <template v-if="seatGroups.length">
                        <div class="flex justify-between items-center pb-3">
                            <span class="text-text-secondary font-medium">
                                Ghế ({{ logicalSeatCount }})
                            </span>
                            <span class="font-semibold">{{ seatTotal.toLocaleString() }}đ</span>
                        </div>

                        <div v-for="(group, gi) in seatGroups" :key="group.seatType">
                            <hr v-if="gi > 0" class="my-2.5 border-t border-border-subtle" />

                            <div class="flex items-start justify-between gap-3 py-0.5">
                                <div class="min-w-0">
                                    <span class="font-medium text-text-primary">
                                        x{{ group.displayCount }}
                                        <span class="text-text-secondary">{{ group.seatType }}</span>
                                    </span>
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

                    <!-- Divider trước combo -->
                    <hr v-if="seatGroups.length && (combos.length || coupon)"
                        class="my-3.5 border-t border-border-subtle" />

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

                    <div v-if="coupon"
                        :class="['flex justify-between items-center text-green-500', combos.length ? 'mt-2' : '']">
                        <span class="flex items-center gap-1.5">
                            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M20 12V4H4v8a4 4 0 0 0 0 8v4h16v-4a4 4 0 0 0 0-8Z" />
                                <path d="M10 10h.01M14 14h.01" />
                            </svg>
                            <span class="font-medium">{{ coupon.code }}</span>
                        </span>
                        <span class="font-semibold tabular-nums shrink-0 ml-3">
                            −{{ coupon.discountValue.toLocaleString() }}đ
                        </span>
                    </div>
                </div>
            </template>

            <template v-else-if="movie">
                <hr class="border-0 h-px bg-lỉnear-to-r from-transparent via-border-subtle to-transparent" />
                <div class="space-y-2">
                    <div class="h-3 w-full rounded-full bg-bg-elevated animate-pulse opacity-60 shadow-inner" />
                    <div class="h-3 w-4/5 rounded-full bg-bg-elevated animate-pulse opacity-40 shadow-inner" />
                </div>
            </template>
            <!-- ── Total – divider gradient + text glow ── -->
            <hr class="border-0 h-px bg-lỉnear-to-r from-transparent via-border-subtle to-transparent" />
            <div class="relative flex justify-between items-center text-lg
               before:absolute before:-inset-x-4 before:top-1/2 before:-translate-y-1/2 before:h-12
               before:bg-lỉnear-to-r before:from-accent/2 before:via-accent/6 before:to-accent/2
               before:rounded-xl before:pointer-events-none before:-z-10">
                <span class="text-text-secondary font-medium">Tổng cộng</span>
                <span
                    class="font-bold text-accent tabular-nums drop-shadow-[0_0_8px_rgba(var(--color-accent-rgb),0.3)]">
                    {{ total.toLocaleString() }}đ
                </span>
            </div>
        </div>

        <!-- Buttons – gradient, shadow, ring mạnh mẽ hơn -->
        <div v-if="props.currentStep !== 1" class="flex gap-4 mt-6">
            <BaseButton variant="secondary" size="lg" class="flex-1 shadow-lg shadow-black/10 ring-1 ring-white/5 hover:shadow-xl hover:ring-white/10
               transition-all duration-200" @click="$emit('prev')">
                Quay lại
            </BaseButton>
            <BaseButton variant="primary" size="lg" class="flex-1 shadow-lg shadow-accent/20 ring-1 ring-accent/30 hover:shadow-xl hover:shadow-accent/30
               hover:ring-accent/40 transition-all duration-200
               bg-linear-to-b from-accent to-accent/90" :disabled="isNextDisabled" @click="$emit('next')">
                Tiếp tục
            </BaseButton>
        </div>
    </div>
</template>