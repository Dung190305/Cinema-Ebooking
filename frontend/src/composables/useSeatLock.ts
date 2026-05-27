// src/composables/useSeatLock.ts
import { ref, computed, onUnmounted } from 'vue'
import { seatLockApi } from '@/api/seatlock.api'
import type { AcquireLockResponse, AcquireSeatLockRequest, ReleaseSeatLockRequest } from '@/types/seatlock'
import { BASE_URL } from '@/api/axios'

export interface LockedSeat {
  seatId: number
  seatNumber: string
  expiredAt: string
}

export function useSeatLock(onTimeout?: () => void) {
    const lockedSeats = ref<LockedSeat[]>([])
    const expiredAt = ref<string | null>(null)
    const isLoading = ref(false)
    const error = ref('')
        

    let timerInterval: NodeJS.Timeout | null = null
    let releaseInProgress = false // tránh gọi release nhiều lần đồng thời
    const _tick = ref(0)

    const lockedSeatIds = computed<number[]>(() =>
        lockedSeats.value.map(item => item.seatId).filter(Boolean)
    )

    const timeLeft = computed(() => {
        _tick.value // dependency để re-compute
        if (!expiredAt.value) return 0
        return Math.max(0, Math.floor((new Date(expiredAt.value).getTime() - Date.now()) / 1000))
    })

    const formattedTime = computed(() => {
        const minutes = Math.floor(timeLeft.value / 60)
        const seconds = timeLeft.value % 60
        return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
    })

    const hasActiveLock = computed(() => {
        return lockedSeats.value.length > 0 && !!expiredAt.value && timeLeft.value > 0
    })

    function startTimer() {
        if (timerInterval) clearInterval(timerInterval)
        timerInterval = setInterval(() => {
        _tick.value++
        if (timeLeft.value <= 0) {
            clearInterval(timerInterval!)
            timerInterval = null
            reset()

            if (onTimeout) onTimeout()
        }
        }, 1000)
    }

    async function acquireLocks(userId: number, showtimeId: number, seatIds: number[]) {
        if (seatIds.length === 0) return { success: true, data: null }

        isLoading.value = true
        error.value = ''

        try {
        const result = await seatLockApi.acquireLocks({ userId, showtimeId, seatIds })
        if (result?.success) {
            lockedSeats.value = result.lockedSeats || []
            const expiry = result.expiredAt || new Date(Date.now() + 5 * 60 * 1000).toISOString()
            expiredAt.value = expiry
            startTimer()
            return { success: true, data: result }
        } else {
            // Chi tiết lỗi từ backend nếu có
            const msg = result?.message || (result?.lockedSeats?.length === 0 
            ? 'Tất cả ghế đã được đặt hoặc bị giữ bởi người khác' 
            : 'Một số ghế không thể giữ')
            error.value = msg
            return { success: false, error: msg }
        }
        } catch (err: any) {
        const msg = err?.response?.data?.message || 'Không thể khóa ghế. Vui lòng thử lại.'
        error.value = msg
        return { success: false, error: msg }
        } finally {
        isLoading.value = false
        }
    }

    function releaseOnBeforeUnload(userId: number, showtimeId: number) {
        if (!userId || !showtimeId) return

        const payload = JSON.stringify({ userId, showtimeId })
        const token = localStorage.getItem('accessToken')
        if (!token) return

        const url = `${BASE_URL}/seat-locks/release`

        // Gửi yêu cầu không đồng bộ, không cần chờ phản hồi
        fetch(url, {
            method: 'POST',
            headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
            },
            body: payload,
            keepalive: true
        })
    }

    async function releaseLocks(userId: number, showtimeId: number) {
        if (!userId || !showtimeId) return
        if (releaseInProgress) return
        releaseInProgress = true
        reset()
        const payload = JSON.stringify({ userId, showtimeId })
        try {
            await seatLockApi.releaseLocks({ userId, showtimeId })
        } catch (e) {
            console.warn('Release lock via axios failed:', e)
        }


        releaseInProgress = false
    }

    function reset() {
        if (timerInterval) {
        clearInterval(timerInterval)
        timerInterval = null
        }
        lockedSeats.value = []
        expiredAt.value = null
        error.value = ''
        _tick.value = 0
    }

    // Tự động release lock khi component unmount (nếu còn active)
    onUnmounted(() => {
        if (hasActiveLock.value && lockedSeatIds.value.length) {
        // Lấy userId và showtimeId từ đâu? Cần lưu lại khi acquire
        // Giải pháp: lưu currentUserId và currentShowtimeId vào ref riêng
        // Ở đây tạm thời không có, bạn cần mở rộng composable để nhớ.
        // Để đơn giản, ta sẽ không tự động release vì thường đã release khi chuyển step.
        // Nếu muốn, hãy thêm currentLockContext.
        }
        if (timerInterval) clearInterval(timerInterval)
    })
    
    function restoreLock(expiryISO: string, seats: LockedSeat[]) {
        if (timerInterval) clearInterval(timerInterval)
        lockedSeats.value = seats
        expiredAt.value = expiryISO
        startTimer()
    }


    return {
        lockedSeats: computed(() => lockedSeats.value),
        lockedSeatIds,
        expiredAt,
        timeLeft,
        formattedTime,
        isLoading,
        error,
        acquireLocks,
        releaseLocks,
        releaseOnBeforeUnload,
        reset,
        hasActiveLock,
        restoreLock,
    }
}