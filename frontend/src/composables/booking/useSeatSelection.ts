// src/composables/booking/useSeatSelection.ts
import { ref, computed, watch } from 'vue'
import { showtimeApi } from '@/api/showtime.api'
import type { ShowtimeSeatLayoutResponse, ShowtimeSeatResponse } from '@/types/showtime-seat'
import type { RoomLayoutResponse, SeatResponse } from '@/types/seat'
import { useSeatLock } from '@/composables/useSeatLock'
import { useAuthStore } from '@/stores/auth.store'

const TYPE_NAME_TO_ID: Record<string, number> = {
    STANDARD: 1, NORMAL: 1, ECONOMY: 1,
    VIP: 2,
    COUPLE: 3, DOUBLE: 3,
}

export function useSeatSelection(booking: any, seatLock: ReturnType<typeof useSeatLock>) {
    const authStore = useAuthStore()
    const rawLayout = ref<ShowtimeSeatLayoutResponse | null>(null)
    const loading = ref(false)
    const isRefetching = ref(false)
    const error = ref('')

    const fullSeatMap = ref<Record<number, ShowtimeSeatResponse>>({})
    const couplePairMap = ref<Record<number, number>>({})

    // ── Adapt Layout ─────────────────────────────────────
    function adaptLayout(raw: ShowtimeSeatLayoutResponse, ownLockedSet: Set<number>) {
        const flatSeats: ShowtimeSeatResponse[] = []
        for (const row of raw.rows ?? []) {
            for (const seat of row ?? []) {
                if (seat) flatSeats.push(seat)
            }
        }

        const maxRow = raw.totalRows ?? raw.rows?.length ?? 0
        const maxCol = raw.totalCols ?? (raw.rows?.[0]?.length ?? 0)

        const rows: (SeatResponse | null)[][] = Array.from({ length: maxRow }, () => new Array(maxCol).fill(null))

        const bookedIds: number[] = []
        const otherLockedIds: number[] = []
        const priceMap: Record<number, number> = {}
        const nameMap: Record<number, string> = {}
        const typeMap: Record<number, number> = {}
        const newPairMap: Record<number, number> = {}
        const newFullSeatMap: Record<number, ShowtimeSeatResponse> = {}

        for (const s of flatSeats) {
            const r = s.rowIndex - 1
            const c = s.colIndex - 1
            if (r < 0 || r >= maxRow || c < 0 || c >= maxCol) continue

            const seatTypeId = typeof s.seatTypeId === 'number'
                ? s.seatTypeId
                : (TYPE_NAME_TO_ID[String(s.seatTypeId ?? '').toUpperCase()] ?? 1)

            rows[r][c] = {
                id: s.id,
                rowIndex: s.rowIndex,
                colIndex: s.colIndex,
                seatTypeId,
                status: s.status,
                label: s.seatNumber,
            } as SeatResponse

            if (s.status === 'BOOKED') bookedIds.push(s.id)
            if (s.status === 'LOCKED' && !ownLockedSet.has(s.id)) {
                otherLockedIds.push(s.id)
            }

            priceMap[s.id] = s.price
            nameMap[s.id] = s.seatNumber
            typeMap[s.id] = seatTypeId
            newFullSeatMap[s.id] = s
        }

        // Couple pairing
        const groupByCoupleId: Record<number, ShowtimeSeatResponse[]> = {}
        for (const s of flatSeats) {
            if (s.seatTypeId === 3 && s.coupleGroupId != null) {
                const gid = s.coupleGroupId
                groupByCoupleId[gid] ||= []
                groupByCoupleId[gid].push(s)
            }
        }

        for (const group of Object.values(groupByCoupleId)) {
            if (group.length === 2) {
                newPairMap[group[0].id] = group[1].id
                newPairMap[group[1].id] = group[0].id
            }
        }

        couplePairMap.value = newPairMap
        fullSeatMap.value = newFullSeatMap

        return {
            grid: { rows: rows as SeatResponse[][], totalCols: maxCol } as RoomLayoutResponse,
            bookedIds,
            lockedIds: otherLockedIds,
            priceMap,
            nameMap,
            typeMap
        }
    }

    const ownLockedSet = computed(() => new Set(seatLock.lockedSeatIds.value))

    const adapted = computed(() =>
        rawLayout.value ? adaptLayout(rawLayout.value, ownLockedSet.value) : null
    )

    const selectedSeats = computed({
        get: () => booking.selectedSeats.value as ShowtimeSeatResponse[],
        set: (val) => { booking.selectedSeats.value = val }
    })

    const selectedIds = computed(() => selectedSeats.value.map(s => s.id))

    // ── Seat Interaction ─────────────────────────────────
    function isSeatAvailable(seatId: number): boolean {
        if (!adapted.value) return false
        return !adapted.value.bookedIds.includes(seatId) && !adapted.value.lockedIds.includes(seatId)
    }

    function onSeatClick(seat: SeatResponse) {
        const fullSeat = fullSeatMap.value[seat.id]
        if (!fullSeat || !isSeatAvailable(seat.id)) return

        if (fullSeat.seatTypeId === 3) {
            const partnerId = couplePairMap.value[seat.id]
            if (partnerId) {
                const partner = adapted.value?.grid.rows.flat().find(s => s?.id === partnerId)
                if (partner) {
                    onCoupleClick(seat, partner)
                    return
                }
            }
        }

        const idx = selectedSeats.value.findIndex(s => s.id === fullSeat.id)
        if (idx >= 0) {
            selectedSeats.value = selectedSeats.value.filter((_, i) => i !== idx)
        } else {
            selectedSeats.value = [...selectedSeats.value, fullSeat]
        }
    }

    function onCoupleClick(left: SeatResponse, right: SeatResponse) {
        const fullLeft = fullSeatMap.value[left.id]
        const fullRight = fullSeatMap.value[right.id]
        if (!fullLeft || !fullRight || !isSeatAvailable(fullLeft.id) || !isSeatAvailable(fullRight.id)) return

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

    // ── Load & Retry ─────────────────────────────────────
    const loadSeatMap = async (showtimeId: number) => {
        loading.value = true
        error.value = ''
        rawLayout.value = null

        try {
            rawLayout.value = await showtimeApi.getSeatMap(showtimeId)
        } catch {
            error.value = 'Không thể tải sơ đồ ghế. Vui lòng thử lại.'
        } finally {
            loading.value = false
        }
    }

    const retry = async () => {
        const st = booking.selectedShowtime.value
        if (!st) return

        isRefetching.value = true
        error.value = ''

        try {
            const newLayout = await showtimeApi.getSeatMap(st.id)
            rawLayout.value = newLayout

            // Giữ lại ghế của mình
            const currentLayout = adapted.value
            if (currentLayout) {
                booking.selectedSeats.value = booking.selectedSeats.value.filter(seat =>
                    !currentLayout.bookedIds.includes(seat.id) && !currentLayout.lockedIds.includes(seat.id)
                )
            }
        } catch {
            error.value = 'Không thể tải sơ đồ ghế.'
        } finally {
            isRefetching.value = false
        }
    }

    watch(() => booking.selectedShowtime.value, async (st, oldSt) => {
        if (!st) return
        booking.selectedSeats.value = []
        await loadSeatMap(st.id)
    }, { immediate: true })

    return {
        adapted,
        loading,
        isRefetching,
        error,
        selectedSeats,
        selectedIds,
        fullSeatMap,
        couplePairMap,
        onSeatClick,
        onCoupleClick,
        retry,
        loadSeatMap
    }
}