<template>
    <div class="w-fit flex flex-col items-center" :class="config.gap">

        <!-- Screen TOP -->
        <ScreenBar v-if="config.screenPosition === 'top'" />

        <!-- Seat grid -->
        <div class="overflow-x-auto w-fit" :class="{ 'pointer-events-none opacity-60': disabled }">
            <div ref="gridEl" class="flex flex-col py-6" :class="config.gap">
                <div v-for="(row, rowIdx) in layout.rows" :key="rowIdx" class="flex items-center" :class="config.gap">
                    <!-- Row label LEFT -->
                    <RowLabel :label="rowLabel(rowIdx)" :mode="config.mode" />

                    <!-- Seat columns -->
                    <div class="grid" :class="config.gap" :style="{
                        gridTemplateColumns: `repeat(${layout.totalCols}, ${config.cellWidth})`,
                    }">
                        <template v-for="item in getRowItems(row, rowIdx)" :key="item.key">
                            <div v-if="item.type === 'empty'"
                                :style="{ width: config.cellWidth, height: config.cellHeight }" />

                            <!-- Ghế đôi -->
                            <div v-else-if="item.type === 'couple'"
                                class="flex items-center justify-center border font-medium transition-all select-none cursor-pointer col-span-2"
                                :class="[config.seatRadius, coupleClasses(item.leftSeat!, item.rightSeat!)]"
                                :style="{ height: config.cellHeight }"
                                :data-couple-ids="`${item.leftSeat.id},${item.rightSeat.id}`"
                                @click="onCoupleClick(item.leftSeat!, item.rightSeat!, $event)">
                                <div class="text-xs flex items-center justify-between w-full px-2">
                                    <span class="leading-none">{{ colNumber(item.leftSeat!.colIndex) }}</span>
                                    <span class="leading-none">{{ colNumber(item.rightSeat!.colIndex) }}</span>
                                </div>
                            </div>

                            <!-- Ghế đơn -->
                            <div v-else
                                class="flex items-center justify-center border font-medium transition-all select-none"
                                :data-seat-id="item.seat!.id" :class="[
                                    config.seatSize,
                                    config.seatRadius,
                                    seatClasses(item.seat!),
                                    isInteractive(item.seat!) ? 'cursor-pointer' : 'cursor-not-allowed',
                                ]" @click="onSeatClick(item.seat!, $event)">
                                <span class="text-xs leading-none">
                                    {{ config.showColNumber ? colNumber(item.seat!.colIndex) : item.seat!.label }}
                                </span>
                            </div>

                        </template>
                    </div>

                    <!-- Row label RIGHT -->
                    <RowLabel v-if="config.rowLabelBothSides" :label="rowLabel(rowIdx)" :mode="config.mode" />
                </div>
            </div>
        </div>

        <!-- Screen BOTTOM -->
        <ScreenBar v-if="config.screenPosition === 'bottom'" />

        <!-- Legend -->
        <div class="w-full flex flex-wrap items-center justify-end gap-x-5 gap-y-2 pt-1">

            <!-- Web: chọn + đã bán -->
            <template v-if="config.mode === 'web'">
                <LegendItem>
                    <span :class="[config.legendSize, 'rounded-md bg-amber-500']" />
                    <span>Ghế đã chọn</span>
                </LegendItem>
                <LegendItem>
                    <span :class="[config.legendSize, 'rounded-md bg-white/20']" />
                    <span>Ghế đã bán</span>
                </LegendItem>
            </template>

            <!-- Seat types -->
            <LegendItem v-for="(cfg, typeId) in SEAT_TYPE_CONFIGS" :key="typeId">
                <span v-if="config.mode === 'admin'"
                    :class="[config.legendSize, 'rounded-md border', cfg.adminBg, cfg.adminBorder]" />
                <span v-else :class="[config.legendSizeWide, 'rounded-md border-2 bg-transparent', cfg.webBorder]" />
                <span>{{ cfg.label }}</span>
            </LegendItem>

            <!-- Admin: inactive -->
            <template v-if="config.mode === 'admin'">
                <LegendItem>
                    <span :class="[config.legendSize, 'rounded-md border border-gray-300 bg-gray-200 opacity-70']" />
                    <span>Không hoạt động</span>
                </LegendItem>
            </template>

            <template v-if="config.mode === 'admin' && showBookingLegend">
                <LegendItem>
                    <span
                        :class="[config.legendSize, config.legendBookedClass || 'bg-gray-300 border border-gray-400']" />
                    <span>Đã đặt</span>
                </LegendItem>
                <LegendItem>
                    <span
                        :class="[config.legendSize, config.legendLockedClass || 'bg-red-100 border border-red-400']" />
                    <span>Đã khóa</span>
                </LegendItem>
            </template>

        </div>

    </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, ref } from 'vue'
import type { RoomLayoutResponse, SeatResponse } from '@/types/seat'
import { SEAT_TYPE_CONFIGS, SEAT_TYPE_FALLBACK } from '@/components/seat/seatGridConfig'
import type { SeatGridConfig } from '@/components/seat/seatGridConfig'

// ── Sub-components ────────────────────────────────────────────────────────────

const gridEl = ref<HTMLElement | null>(null)
defineExpose({ gridEl })

const RowLabel = defineComponent({
    props: {
        label: { type: String, required: true },
        mode: { type: String, required: true },
    },
    setup: (props) => () =>
        h(
            'span',
            {
                class: [
                    'w-6 shrink-0 text-center text-xs font-semibold select-none',
                    props.mode === 'web' ? 'text-white/40' : 'text-text-admin-tertiary',
                ],
            },
            props.label,
        ),
})

const ScreenBar = defineComponent({
    setup: () => () =>
        h('div', { class: 'w-full flex flex-col items-center gap-1 py-1' }, [
            h('span', { class: 'text-xs font-medium tracking-widest uppercase text-slate-400' }, 'Màn hình'),
            h('div', { class: 'h-1 w-full rounded-full bg-linear-to-r from-transparent via-amber-400/60 to-transparent' }),
        ]),
})

const LegendItem = defineComponent({
    setup: (_, { slots }) => () =>
        h('div', { class: 'flex items-center gap-1.5 text-xs text-slate-400' }, slots.default?.()),
})

// ── Row item type ─────────────────────────────────────────────────────────────

type RowItem =
    | { type: 'single'; key: string; seat: SeatResponse; leftSeat?: never; rightSeat?: never }
    | { type: 'couple'; key: string; seat?: never; leftSeat: SeatResponse; rightSeat: SeatResponse }
    | { type: 'empty'; key: string; seat?: never; leftSeat?: never; rightSeat?: never }

// ── Props & Emits ─────────────────────────────────────────────────────────────

const props = withDefaults(
    defineProps<{
        layout: RoomLayoutResponse
        config: SeatGridConfig
        selectedIds?: number[]
        bookedIds?: number[]
        lockedIds?: number[]
        disabled?: boolean
        pendingSeatIds?: number[]
        showBookingLegend?: boolean
    }>(),
    {
        selectedIds: () => [],
        bookedIds: () => [],
        lockedIds: () => [],
        disabled: () => false,
        pendingSeatIds: () => [],
        showBookingLegend: false,
    },
)

const emit = defineEmits<{
    'seat-click': [seat: SeatResponse, event: MouseEvent]
    'couple-click': [left: SeatResponse, right: SeatResponse, event: MouseEvent]
}>()

// ── Helpers ───────────────────────────────────────────────────────────────────

function rowLabel(rowIdx: number): string {
    return String.fromCharCode(65 + rowIdx)
}

function colNumber(colIdx: number): number {
    return props.config.rtl ? props.layout.totalCols - colIdx + 1 : colIdx
}

function typeConfig(typeId: number) {
    return SEAT_TYPE_CONFIGS[typeId] ?? SEAT_TYPE_FALLBACK
}

const selectedSet = computed(() => new Set(props.selectedIds))
const bookedSet = computed(() => new Set(props.bookedIds))
const lockedSet = computed(() => new Set(props.lockedIds))
function isInteractive(seat: SeatResponse): boolean {
    if (props.config.mode === 'admin') return true
    if (seat.status === 'INACTIVE') return false
    if (bookedSet.value.has(seat.id)) return false
    if (lockedSet.value.has(seat.id)) return false
    return true
}

function onSeatClick(seat: SeatResponse, event: MouseEvent): void {
    if (!isInteractive(seat)) return
    if (props.disabled) return
    emit('seat-click', seat, event)
}

function onCoupleClick(left: SeatResponse, right: SeatResponse, event: MouseEvent): void {
    if (props.disabled) return
    emit('couple-click', left, right, event)
}

// ── Class builders ────────────────────────────────────────────────────────────
const pendingSet = computed(() => new Set(props.pendingSeatIds))

function seatClasses(seat: SeatResponse): string {
    const cfg = typeConfig(seat.seatTypeId)
    const inactive = seat.status === 'INACTIVE'
    const selected = selectedSet.value.has(seat.id)
    const booked = bookedSet.value.has(seat.id)
    const locked = lockedSet.value.has(seat.id)
    const isPending = pendingSet.value.has(seat.id)

    let baseClass = ''

    if (props.config.mode === 'admin') {
        if (selected) baseClass = props.config.adminSelectedClass!
        else if (inactive) baseClass = props.config.adminInactiveClass!
        else if (booked) baseClass = props.config.adminBookedClass!
        else if (locked) baseClass = props.config.adminLockedClass!
        else baseClass = `${cfg.adminBg} ${cfg.adminBorder} ${cfg.adminText} hover:brightness-110`
    } else {
        // web mode
        if (inactive) baseClass = props.config.webInactiveClass!
        else if (booked) baseClass = props.config.webBookedClass!
        else if (locked) baseClass = props.config.webLockedClass! || props.config.webInactiveClass! // fallback
        else if (selected) baseClass = `${cfg.webSelectedBg} border-transparent ${cfg.webSelectedText}`
        else baseClass = `bg-white/10 border-2 ${cfg.webBorder} text-white/80 hover:bg-white/20`
    }

    if (props.config.mode === 'admin' && isPending) {
        baseClass += ' ring-2 ring-blue-400 border-dashed'
    }
    return baseClass
}

function coupleClasses(left: SeatResponse, right: SeatResponse): string {
    const cfg = typeConfig(left.seatTypeId)
    const inactive = left.status === 'INACTIVE' || right.status === 'INACTIVE'
    const selected = selectedSet.value.has(left.id) || selectedSet.value.has(right.id)
    const isPending = pendingSet.value.has(left.id) || pendingSet.value.has(right.id)
    const booked = bookedSet.value.has(left.id) || bookedSet.value.has(right.id)
    const locked = lockedSet.value.has(left.id) || lockedSet.value.has(right.id)

    let baseClass = ''

    if (props.config.mode === 'admin') {
        if (selected) baseClass = 'bg-blue-500 border-blue-600 text-white shadow-md ring-2 ring-blue-300'
        else if (inactive) baseClass = 'bg-slate-50 border-slate-200 text-slate-300 opacity-50'
        else if (booked) baseClass = 'bg-gray-300 border-gray-400 text-gray-600 line-through opacity-80'
        else if (locked) baseClass = 'bg-red-100 border-red-400 text-red-700 opacity-80'
        else baseClass = `${cfg.adminBg} ${cfg.adminBorder} ${cfg.adminText} hover:brightness-110`
    } else {
        if (inactive) baseClass = 'bg-white/5 border-white/10 text-white/20 opacity-40'
        else if (booked) baseClass = 'bg-white/20 border-white/20 text-white/30'
        else if (locked) baseClass = props.config.webLockedClass || 'bg-white/5 border-white/10 text-white/20 opacity-40'
        else if (selected) baseClass = 'bg-amber-500 border-transparent text-white'
        else baseClass = `bg-white/10 border-2 ${cfg.webBorder} text-white/80 hover:bg-white/20`
    }

    if (props.config.mode === 'admin' && isPending) {
        baseClass += ' ring-2 ring-blue-400 border-dashed'
    }

    return baseClass

}

// ── Row item builder ──────────────────────────────────────────────────────────

function getRowItems(row: SeatResponse[], rowIdx: number): RowItem[] {
    const items: RowItem[] = []

    for (let c = 0; c < row.length; c++) {
        const seat = row[c]

        if (seat === null) {                                         // ✅ guard null
            items.push({ type: 'empty', key: `empty-${rowIdx}-${c}` })
            continue
        }
        const next = row[c + 1]
        const isCouplePair =
            seat.seatTypeId === 3 &&
            seat.colIndex % 2 === 1
            && next?.seatTypeId === 3
        if (isCouplePair) {
            items.push({ type: 'couple', key: `couple-${rowIdx}-${seat.colIndex}`, leftSeat: seat, rightSeat: next })
            c++ // bỏ qua ghế phải
        } else {
            items.push({ type: 'single', key: `seat-${seat.id}`, seat })
        }
    }

    return items
}


</script>