// src/composables/booking/useShowtimeSwitcher.ts
import { computed, watch } from 'vue'
import { useShowtimes } from '@/composables/useShowtimes'
import { getDateKeyVN } from '@/utils/dateFormat'

export function useShowtimeSwitcher(booking: any) {
    const currentShowtime = computed(() => booking.selectedShowtime.value)

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
        const merged = hasCurrent
            ? otherShowtimesRaw.value
            : [current, ...otherShowtimesRaw.value]

        return [...merged].sort(
            (a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
        )
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

    const changeShowtime = (newShowtime: any) => {
        if (!newShowtime) return
        
        if (newShowtime.id === currentShowtime.value?.id) return

        booking.selectedShowtime.value = newShowtime
        booking.selectedSeats.value = []
    }

    function getFormatBadge(st: any): string {
        if (!st) return ''
        if (st.formatName) return st.formatName
        const map: Record<number, string> = { 1: '2D', 2: '3D', 3: 'IMAX' }
        return map[st.formatId] ?? '2D'
    }

    return {
        allShowtimes,
        loadingOtherShowtimes,
        currentShowtime,
        changeShowtime,
        getFormatBadge,
        isCurrentShowtime: (id: number) => currentShowtime.value?.id === id
    }
}