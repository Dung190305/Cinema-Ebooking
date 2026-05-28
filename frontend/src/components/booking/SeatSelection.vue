<template>
    <div>
        <!-- Login Prompt -->
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
            <p class="text-error text-body">{{ error }}</p>
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
        <div v-else-if="adapted" class="relative bg-bg-surface ...">
            <div v-if="isRefetching" class="absolute inset-0 ...">Đang cập nhật...</div>

            <SeatGrid :layout="adapted.grid" :config="webSeatGridConfig" :selected-ids="selectedIds"
                :booked-ids="adapted.bookedIds" :locked-ids="adapted.lockedIds" :show-booking-legend="true"
                @seat-click="onSeatClick" @couple-click="onCoupleClick" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, inject } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useUIStore } from '@/stores/ui.store'
import SeatGrid from '@/components/seat/SeatGrid.vue'
import ShowtimePill from '@/components/showtime/ShowtimePill.vue'
import { webSeatGridConfig } from '@/components/seat/seatGridConfig'

import { useSeatSelection } from '@/composables/booking/useSeatSelection'
import { useShowtimeSwitcher } from '@/composables/booking/useShowtimeSwitcher'
import { useSeatLock } from '@/composables/useSeatLock'

const emit = defineEmits(['next', 'prev'])
const booking = inject<any>('booking')!
const authStore = useAuthStore()
const uiStore = useUIStore()
const seatLock = inject<ReturnType<typeof useSeatLock>>('seatLock')!

const showLoginPrompt = ref(false)

// ── Composables ─────────────────────────────
const {
    adapted, loading, isRefetching, error,
    selectedIds,
    onSeatClick: originalOnSeatClick,     // đổi tên hàm gốc
    onCoupleClick: originalOnCoupleClick,  // đổi tên hàm gốc
    retry,
    selectedSeats
} = useSeatSelection(booking, seatLock)

const {
    allShowtimes, loadingOtherShowtimes,
    changeShowtime, getFormatBadge, isCurrentShowtime
} = useShowtimeSwitcher(booking)

// Auth helper
const requireLogin = () => {
    if (!authStore.isLoggedIn) {
        showLoginPrompt.value = true
        return false
    }
    return true
}

// ── Wrappers kiểm tra đăng nhập trước khi chọn ghế ──
const onSeatClick = (seat: any) => {
    if (!requireLogin()) {
        showLoginPrompt = true
        return
    }
    originalOnSeatClick(seat)
}

const onCoupleClick = (left: any, right: any) => {
    if (!requireLogin()) {
        showLoginPrompt = true
        return
    }
    originalOnCoupleClick(left, right)
}

const goToLogin = () => {
    showLoginPrompt.value = false
    uiStore.openLoginModal()
}

async function validateAndNext() {
    if (!requireLogin()) return false
    if (selectedSeats.value.length === 0) {
        alert('Vui lòng chọn ít nhất một ghế')
        return false
    }

    const currentLayout = adapted.value
    if (currentLayout) {
        const stillAvailable = selectedSeats.value.every(seat =>
            !currentLayout.bookedIds.includes(seat.id) && !currentLayout.lockedIds.includes(seat.id)
        )
        if (!stillAvailable) {
            alert('Một số ghế đã bị đặt hoặc giữ bởi người khác. Vui lòng chọn lại.')
            await retry()
            return false
        }
    }

    const userId = authStore.user?.id
    const showtimeId = booking.selectedShowtime.value?.id
    if (!userId || !showtimeId) return false

    const result = await seatLock.acquireLocks(userId, showtimeId, selectedIds.value)
    if (!result?.success) {
        alert(seatLock.error.value || 'Không thể giữ ghế. Vui lòng thử lại.')
        await retry()
        return false
    }

    const lockData = {
        expiredAt: seatLock.expiredAt,
        seats: result.lockedSeats,
        savedAt: Date.now()
    }
    sessionStorage.setItem(`seatlock_${showtimeId}`, JSON.stringify(lockData))

    emit('next')
    return true
}

defineExpose({ next: validateAndNext })
</script>