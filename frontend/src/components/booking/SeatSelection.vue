<template>
    <div>
        <Transition name="modal-fade">
            <div v-if="orphanWarning"
                class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-md"
                @click.self="clearOrphan">
                <div
                    class="relative bg-linear-to-br from-bg-surface via-bg-surface to-bg-surface/95 rounded-2xl max-w-md w-full shadow-2xl border border-white/10 overflow-hidden">
                    <!-- Subtle accent top bar -->
                    <div class="absolute top-0 left-0 right-0 h-1 bg-linear-to-r from-accent to-accent/40"></div>

                    <div class="p-6 sm:p-7">
                        <!-- Header -->
                        <div class="flex items-start gap-4">
                            <div class="shrink-0 w-11 h-11 rounded-full bg-accent/15 flex items-center justify-center">
                                <AlertTriangle class="w-6 h-6 text-accent" />
                            </div>
                            <div class="flex-1">
                                <h3 class="text-title font-bold text-text-primary mb-1">
                                    Ghế tạo khoảng trống
                                </h3>
                                <p class="text-text-secondary text-sm leading-relaxed">
                                    {{ orphanWarning.message }}
                                </p>
                            </div>
                        </div>

                        <!-- Hint / suggestion -->
                        <div class="mt-5 pt-4 border-t border-border-default/50">
                            <p class="text-caption text-text-tertiary flex items-center gap-2">
                                <Info class="w-3.5 h-3.5" />
                                Mẹo: Chọn các ghế liền kề hoặc bỏ chọn ghế đang gây lẻ
                            </p>
                        </div>

                        <!-- Actions -->
                        <div class="mt-6 flex justify-end gap-3">
                            <button
                                class="px-5 py-2 rounded-xl text-sm font-medium transition-all duration-200 border border-border-default text-text-secondary hover:bg-white/5 hover:border-accent/30"
                                @click="clearOrphan">
                                Chọn lại ghế
                            </button>

                        </div>
                    </div>
                </div>
            </div>
        </Transition>

        <!-- Polling indicator (rõ ràng hơn) -->
        <div v-if="isPolling"
            class="fixed bottom-4 right-4 z-40 flex items-center gap-2.5 bg-black/80 backdrop-blur-md px-4 py-2 rounded-full border border-accent-500/40 shadow-lg">
            <RefreshCw class="w-3.5 h-3.5 text-accent-400 animate-spin" />
            <span class="text-xs font-medium text-accent-300">Đồng bộ sơ đồ ghế...</span>
        </div>
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
        <div v-else-if="adapted"
            class="relative bg-bg-surface border border-border-default rounded-xl p-4 lg:p-6 overflow-x-auto">
            <!-- Loading overlay -->
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
import { ref, inject } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useUIStore } from '@/stores/ui.store'
import { useToast } from 'vue-toastification'
import SeatGrid from '@/components/seat/SeatGrid.vue'
import ShowtimePill from '@/components/showtime/ShowtimePill.vue'
import { webSeatGridConfig } from '@/components/seat/seatGridConfig'

import { useSeatSelection } from '@/composables/booking/useSeatSelection'
import { useSeatPolling } from '@/composables/booking/useSeatPolling'
import { useShowtimeSwitcher } from '@/composables/booking/useShowtimeSwitcher'
import { useSeatLock } from '@/composables/useSeatLock'

import { AlertTriangle, RefreshCw, Info } from 'lucide-vue-next'

const emit = defineEmits(['next', 'prev'])
const booking = inject<any>('booking')!
const authStore = useAuthStore()
const uiStore = useUIStore()
const seatLock = inject<ReturnType<typeof useSeatLock>>('seatLock')!
const toast = useToast()
const showLoginPrompt = ref(false)

// ── Composables ─────────────────────────────
const {
    adapted, loading, isRefetching, error,
    selectedIds,
    onSeatClick: originalOnSeatClick,
    onCoupleClick: originalOnCoupleClick,
    retry,
    selectedSeats, rawLayout,
    orphanWarning, checkOrphan, clearOrphan,
} = useSeatSelection(booking, seatLock)

const { isPolling } = useSeatPolling(booking, rawLayout)

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

const onSeatClick = (seat: any) => {
    if (!requireLogin()) return
    originalOnSeatClick(seat)
}

const onCoupleClick = (left: any, right: any) => {
    if (!requireLogin()) return
    originalOnCoupleClick(left, right)
}

const goToLogin = () => {
    showLoginPrompt.value = false
    uiStore.openLoginModal()
}

async function validateAndNext() {
    if (!requireLogin()) return false
    if (selectedSeats.value.length === 0) {
        toast.warning('Vui lòng chọn ít nhất một ghế')
        return false
    }

    const currentLayout = adapted.value
    if (currentLayout) {
        const stillAvailable = selectedSeats.value.every(seat =>
            !currentLayout.bookedIds.includes(seat.id) && !currentLayout.lockedIds.includes(seat.id)
        )
        if (!stillAvailable) {
            toast.error('Một số ghế đã bị đặt hoặc giữ bởi người khác. Vui lòng chọn lại.')
            await retry()
            return false
        }

        // Orphan check — chạy tại đây, không phải khi click ghế
        const orphanResult = checkOrphan(
            currentLayout.grid,
            new Set(currentLayout.bookedIds),
            new Set(currentLayout.lockedIds),
            new Set(selectedIds.value),
        )
        if (orphanResult.hasOrphan) {
            orphanWarning.value = orphanResult   // hiện modal, chờ user confirm
            return false
        }
    }

    // Bảo mật bổ sung: Poll sơ đồ ghế trước khi xác nhận để lấy trạng thái mới nhất
    // Điều này giúp phát hiện nếu người khác vừa khóa ghế làm ghế lẻ
    await retry()

    // Re-validate sau khi polling
    const freshLayout = adapted.value
    if (freshLayout) {
        const stillAvailableAfterPoll = selectedSeats.value.every(seat =>
            !freshLayout.bookedIds.includes(seat.id) && !freshLayout.lockedIds.includes(seat.id)
        )
        if (!stillAvailableAfterPoll) {
            toast.error('Một số ghế vừa bị đặt hoặc giữ bởi người khác. Vui lòng chọn lại.')
            return false
        }

        // Re-check orphan sau khi polling
        const orphanResultAfterPoll = checkOrphan(
            freshLayout.grid,
            new Set(freshLayout.bookedIds),
            new Set(freshLayout.lockedIds),
            new Set(selectedIds.value),
        )
        if (orphanResultAfterPoll.hasOrphan) {
            orphanWarning.value = orphanResultAfterPoll
            return false
        }
    }

    return doNext()
}

async function doNext() {
    const userId = authStore.user?.id
    const showtimeId = booking.selectedShowtime.value?.id
    if (!userId || !showtimeId) return false

    const result = await seatLock.acquireLocks(userId, showtimeId, selectedIds.value)
    if (!result?.success) {
        const orphanError = extractOrphanError(seatLock.error.value)
        const rawError = (result as any)?.rawError ?? null
        const orphanFromRaw = rawError ? extractOrphanError(rawError) : null
        const orphan = orphanError ?? orphanFromRaw

        if (orphan) {
            orphanWarning.value = {
                hasOrphan: true,
                orphanCount: orphan.orphanCount,
                message: orphan.message,
            }
            await retry()
            return false
        }

        toast.error(seatLock.error.value || 'Không thể giữ ghế. Vui lòng thử lại.')
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