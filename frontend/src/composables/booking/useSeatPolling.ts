// src/composables/booking/useSeatPolling.ts
import { ref, watch, onUnmounted } from 'vue'
import { showtimeApi } from '@/api/showtime.api'
import type { ShowtimeSeatLayoutResponse } from '@/types/showtime-seat'

const POLL_INTERVAL_MS = 20_000   

export function useSeatPolling(
  booking: any,
  rawLayout: Ref<ShowtimeSeatLayoutResponse | null>,
) {
  const isPolling = ref(false)
  let timerId: ReturnType<typeof setInterval> | null = null

    async function poll() {
        const showtimeId = booking.selectedShowtime.value?.id
        if (!showtimeId) return
        if (document.hidden) return

        try {
            isPolling.value = true
            if (import.meta.env.DEV) console.log('[Polling] fetching...')
            const fresh = await showtimeApi.getSeatMap(showtimeId)
            rawLayout.value = fresh
            if (import.meta.env.DEV) console.log('[Polling] updated')
        } catch (err) {
            if (import.meta.env.DEV) console.warn('[Polling] error', err)
        } finally {
            isPolling.value = false
        }
    }

  function startPolling() {
    stopPolling()
    timerId = setInterval(poll, POLL_INTERVAL_MS)
  }

  function stopPolling() {
    if (timerId) { clearInterval(timerId); timerId = null }
  }

  // Start khi có showtime, stop khi đổi showtime hoặc null
  watch(
    () => booking.selectedShowtime.value?.id,
    (id) => { id ? startPolling() : stopPolling() },
    { immediate: true }
  )

  // Pause khi tab bị ẩn (tiết kiệm request)
  const handleVisibility = () => document.hidden ? stopPolling() : startPolling()
  document.addEventListener('visibilitychange', handleVisibility)

  onUnmounted(() => {
    stopPolling()
    document.removeEventListener('visibilitychange', handleVisibility)
  })

  return { isPolling }
}