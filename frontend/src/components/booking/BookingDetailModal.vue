<!-- components/booking/BookingDetailModal.vue -->
<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useBookingDetail } from '@/composables/useBookingDetail'
import { formatDateTimeVN } from '@/utils/dateFormat'
import { formatVND } from '@/utils/currency'
import { getStatusBadgeClass, getStatusText } from '@/utils/bookingStatus'

const props = defineProps<{
    bookingId: number | null
}>()

const emit = defineEmits<{
    (e: 'close'): void
    (e: 'refundRequested'): void
}>()

const router = useRouter()

const {
    loading,
    loadingQR,
    detail,
    qrBase64,
    error,
    showCancelModal,
    cancelLoading,
    cancelError,
    cancelSuccess,
    refundCalc,
    refundCalcLoading,
    hasDiscount,
    isShowtimePassed,
    canRequestRefund,
    retry,
    openCancelModal,
    closeCancelModal,
    confirmCancel,
} = useBookingDetail(() => props.bookingId)

function close() {
    emit('close')
}

function onBackdropClick(e: MouseEvent) {
    if (e.target === e.currentTarget) close()
}

function handleCloseCancelModal() {
    const wasSuccess = closeCancelModal()
    if (wasSuccess) emit('refundRequested')
}

async function handleConfirmCancel() {
    await confirmCancel()
}

function goToRefundPolicy() {
    router.push({ name: 'refund-policy' })
    showCancelModal.value = false
    emit('close')
}
</script>

<template>
    <!-- Main Modal -->
    <Teleport to="body">
        <Transition name="fade">
            <div v-if="bookingId !== null"
                class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
                @click="onBackdropClick">
                <Transition name="slide-up">
                    <div v-if="bookingId !== null"
                        class="relative w-full max-w-2xl max-h-[90vh] bg-bg-surface rounded-2xl shadow-2xl border border-border-subtle flex flex-col overflow-hidden">
                        <!-- ── Header ── -->
                        <div class="flex items-center justify-between px-6 py-4 border-b border-border-subtle shrink-0">
                            <h2 class="text-title text-text-primary">Chi tiết đơn hàng</h2>
                            <button @click="close"
                                class="p-2 rounded-full hover:bg-bg-base text-text-secondary hover:text-text-primary transition-colors"
                                aria-label="Đóng">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>

                        <!-- ── Scrollable body ── -->
                        <div class="overflow-y-auto flex-1 px-6 py-5 space-y-6">
                            <!-- SKELETON -->
                            <template v-if="loading">
                                <div class="animate-pulse space-y-4">
                                    <div class="flex justify-between">
                                        <div class="h-5 bg-gray-200 rounded w-40"></div>
                                        <div class="h-6 bg-gray-200 rounded-full w-24"></div>
                                    </div>
                                    <div class="h-4 bg-gray-200 rounded w-3/4"></div>
                                    <div class="h-4 bg-gray-200 rounded w-1/2"></div>
                                    <div class="grid grid-cols-2 gap-3 mt-2">
                                        <div class="h-10 bg-gray-200 rounded-lg"></div>
                                        <div class="h-10 bg-gray-200 rounded-lg"></div>
                                    </div>
                                    <div class="h-48 bg-gray-200 rounded-xl mx-auto w-48"></div>
                                </div>
                            </template>

                            <!-- ERROR -->
                            <template v-else-if="error">
                                <div class="text-center py-10">
                                    <p class="text-body text-red-500">{{ error }}</p>
                                    <button @click="retry"
                                        class="mt-4 px-4 py-2 rounded-lg bg-accent text-white text-caption hover:opacity-90">
                                        Thử lại
                                    </button>
                                </div>
                            </template>

                            <!-- CONTENT -->
                            <template v-else-if="detail">
                                <!-- ① Booking meta -->
                                <section class="space-y-3">
                                    <div class="flex flex-wrap items-start justify-between gap-2">
                                        <div>
                                            <span class="text-caption text-text-tertiary">Mã đơn</span>
                                            <p
                                                class="text-body font-mono font-semibold text-text-primary tracking-wider">
                                                {{ detail.bookingCode }}
                                            </p>
                                        </div>
                                        <span :class="[
                                            'px-3 py-1 rounded-full text-xs font-semibold shrink-0',
                                            getStatusBadgeClass(detail.status),
                                        ]">
                                            {{ getStatusText(detail.status) }}
                                        </span>
                                    </div>

                                    <!-- Movie / showtime info -->
                                    <div class="bg-bg-base rounded-xl p-4 space-y-2 border border-border-subtle">
                                        <div class="flex items-start gap-3">
                                            <svg class="w-5 h-5 mt-0.5 text-accent shrink-0" fill="none"
                                                stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                    d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 20h16a1 1 0 001-1V5a1 1 0 00-1-1H4a1 1 0 00-1 1v14a1 1 0 001 1z" />
                                            </svg>
                                            <div>
                                                <p class="text-body font-semibold text-text-primary">
                                                    {{ detail.movieTitle }}
                                                </p>
                                                <p class="text-caption text-text-secondary mt-0.5">
                                                    {{ detail.cinemaName }} · {{ detail.roomName }}
                                                </p>
                                                <p class="text-caption text-text-secondary">
                                                    {{ formatDateTimeVN(detail.showtimeStartTime) }}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </section>

                                <!-- ② Seats -->
                                <section>
                                    <h3 class="text-body font-semibold text-text-primary mb-3">
                                        Ghế đã đặt
                                        <span class="ml-1.5 text-caption text-text-tertiary font-normal">({{
                                            detail.seats.length }} ghế)</span>
                                    </h3>
                                    <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
                                        <div v-for="seat in detail.seats" :key="seat.showtimeSeatId"
                                            class="flex items-center gap-2 bg-bg-base rounded-lg px-3 py-2.5 border border-border-subtle">
                                            <span
                                                class="flex items-center justify-center w-8 h-8 rounded-md bg-accent/10 text-accent font-bold text-sm shrink-0">
                                                {{ seat.seatName }}
                                            </span>
                                            <div class="min-w-0">
                                                <p class="text-caption text-text-secondary truncate">{{ seat.seatType }}
                                                </p>
                                                <p class="text-caption font-medium text-text-primary">
                                                    {{ formatVND(seat.price) }}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </section>

                                <!-- ③ Combos -->
                                <section v-if="detail.combos && detail.combos.length > 0">
                                    <h3 class="text-body font-semibold text-text-primary mb-3">Bắp / Nước</h3>
                                    <div class="space-y-2">
                                        <div v-for="combo in detail.combos" :key="combo.comboId"
                                            class="flex items-center justify-between bg-bg-base rounded-lg px-4 py-3 border border-border-subtle">
                                            <div>
                                                <p class="text-body text-text-primary">{{ combo.comboName }}</p>
                                                <p class="text-caption text-text-secondary">
                                                    {{ formatVND(combo.unitPrice) }} × {{ combo.quantity }}
                                                </p>
                                            </div>
                                            <p class="text-body font-semibold text-text-primary">
                                                {{ formatVND(combo.totalPrice) }}
                                            </p>
                                        </div>
                                    </div>
                                </section>

                                <!-- ④ Price breakdown -->
                                <section class="bg-bg-base rounded-xl border border-border-subtle overflow-hidden">
                                    <div class="px-4 py-3 space-y-2">
                                        <div class="flex justify-between text-caption text-text-secondary">
                                            <span>Tiền vé ({{ detail.seats.length }} ghế)</span>
                                            <span>{{ formatVND(detail.totalTicketPrice) }}</span>
                                        </div>
                                        <div v-if="detail.totalComboPrice && detail.totalComboPrice > 0"
                                            class="flex justify-between text-caption text-text-secondary">
                                            <span>Bắp / Nước</span>
                                            <span>{{ formatVND(detail.totalComboPrice) }}</span>
                                        </div>

                                        <template v-if="hasDiscount">
                                            <div class="border-t border-dashed border-border-subtle pt-2">
                                                <div v-if="(detail.tierDiscountAmount ?? 0) > 0"
                                                    class="flex justify-between text-caption text-green-700">
                                                    <span>
                                                        Ưu đãi thành viên
                                                        <template v-if="detail.membershipTierName">
                                                            ({{ detail.membershipTierName }}
                                                            <template v-if="detail.membershipDiscountPercent">· {{
                                                                detail.membershipDiscountPercent }}%</template>)
                                                        </template>
                                                    </span>
                                                    <span>-{{ formatVND(detail.tierDiscountAmount) }}</span>
                                                </div>
                                                <div v-if="(detail.couponDiscountAmount ?? 0) > 0"
                                                    class="flex justify-between text-caption text-green-700">
                                                    <span>
                                                        Mã giảm giá
                                                        <span v-if="detail.coupon" class="font-mono">({{
                                                            detail.coupon.code }})</span>
                                                    </span>
                                                    <span>-{{ formatVND(detail.couponDiscountAmount) }}</span>
                                                </div>
                                            </div>
                                        </template>
                                    </div>
                                    <div
                                        class="px-4 py-3 bg-accent/5 border-t border-border-subtle flex justify-between items-center">
                                        <span class="text-body font-semibold text-text-primary">Tổng thanh toán</span>
                                        <span class="text-title font-bold text-accent">{{
                                            formatVND(detail.finalAmount)
                                            }}</span>
                                    </div>
                                </section>

                                <!-- ⑤ Timestamps -->
                                <section class="grid grid-cols-1 sm:grid-cols-3 gap-3 text-caption">
                                    <div class="bg-bg-base rounded-lg px-3 py-2 border border-border-subtle">
                                        <p class="text-text-tertiary">Đặt lúc</p>
                                        <p class="text-text-primary font-medium mt-0.5">
                                            {{ formatDateTimeVN(detail.createdAt) }}
                                        </p>
                                    </div>
                                    <div v-if="detail.paidAt"
                                        class="bg-bg-base rounded-lg px-3 py-2 border border-border-subtle">
                                        <p class="text-text-tertiary">Thanh toán</p>
                                        <p class="text-text-primary font-medium mt-0.5">
                                            {{ formatDateTimeVN(detail.paidAt) }}
                                        </p>
                                    </div>
                                    <div v-if="detail.expiredAt && detail.status === 'PENDING'"
                                        class="bg-yellow-50 rounded-lg px-3 py-2 border border-yellow-200">
                                        <p class="text-yellow-700">Hết hạn lúc</p>
                                        <p class="text-yellow-900 font-medium mt-0.5">
                                            {{ formatDateTimeVN(detail.expiredAt) }}
                                        </p>
                                    </div>
                                </section>

                                <!-- ⑥ QR Code -->
                                <section v-if="detail.status === 'CONFIRMED'"
                                    class="flex flex-col items-center pt-2 pb-1">
                                    <h3 class="text-body font-semibold text-text-primary mb-4">QR Check-in</h3>
                                    <div v-if="loadingQR" class="animate-pulse w-48 h-48 bg-gray-200 rounded-xl"></div>
                                    <template v-else-if="qrBase64">
                                        <div class="p-3 bg-white rounded-xl shadow-sm border border-border-subtle">
                                            <img :src="`data:image/png;base64,${qrBase64}`" alt="QR Check-in"
                                                class="w-44 h-44 sm:w-52 sm:h-52" />
                                        </div>
                                        <p class="text-caption text-text-tertiary mt-3 text-center">
                                            Xuất trình mã QR này tại quầy để nhận vé
                                        </p>
                                    </template>
                                    <p v-else class="text-caption text-text-secondary">Không thể tải mã QR.</p>
                                </section>
                            </template>
                        </div>

                        <!-- ── Footer ── -->
                        <div
                            class="px-6 py-4 border-t border-border-subtle shrink-0 flex items-center justify-between gap-3">
                            <button v-if="canRequestRefund || (detail?.status === 'CONFIRMED' && isShowtimePassed)"
                                @click="!isShowtimePassed && openCancelModal()" :disabled="isShowtimePassed"
                                :title="isShowtimePassed ? 'Suất chiếu đã diễn ra, không thể hủy đơn đặt vé' : undefined"
                                :class="[
                                    'inline-flex items-center gap-1.5 px-4 py-2 rounded-lg border text-caption transition-colors',
                                    isShowtimePassed
                                        ? 'border-border-subtle text-text-tertiary bg-bg-base cursor-not-allowed opacity-60'
                                        : 'border-red-200 text-red-600 hover:bg-red-50',
                                ]">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M6 18L18 6M6 6l12 12" />
                                </svg>
                                Yêu cầu hủy vé
                            </button>
                            <div v-else></div>

                            <button @click="close"
                                class="px-5 py-2 rounded-lg border border-border-default text-text-primary text-caption hover:bg-bg-base transition-colors">
                                Đóng
                            </button>
                        </div>
                    </div>
                </Transition>
            </div>
        </Transition>

        <!-- ── Cancel Confirmation Modal ── -->
        <Transition name="fade">
            <div v-if="showCancelModal"
                class="fixed inset-0 z-60 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm"
                @click.self="!cancelLoading && handleCloseCancelModal()">
                <Transition name="slide-up">
                    <div
                        class="w-full max-w-md bg-bg-surface rounded-2xl shadow-2xl border border-border-subtle overflow-hidden">
                        <!-- SUCCESS STATE -->
                        <template v-if="cancelSuccess">
                            <div class="px-6 py-8 text-center space-y-4">
                                <div
                                    class="mx-auto w-14 h-14 rounded-full bg-green-100 flex items-center justify-center">
                                    <svg class="w-7 h-7 text-green-600" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M5 13l4 4L19 7" />
                                    </svg>
                                </div>
                                <h3 class="text-title text-text-primary">Yêu cầu đã được gửi</h3>
                                <p class="text-body text-text-secondary">
                                    Yêu cầu hoàn tiền của bạn đã được ghi nhận. Quản trị viên sẽ xem xét và phê
                                    duyệt trong vòng <strong>5–7 ngày làm việc</strong>.
                                </p>
                                <p class="text-caption text-text-tertiary">
                                    Vé đã bị hủy và không còn hiệu lực. Bạn sẽ nhận được thông báo khi hoàn tiền
                                    được xử lý.
                                </p>
                                <button @click="handleCloseCancelModal"
                                    class="w-full mt-2 px-5 py-2.5 rounded-lg bg-accent text-white text-body font-medium hover:opacity-90 transition-opacity">
                                    Đã hiểu
                                </button>
                            </div>
                        </template>

                        <!-- CONFIRM STATE -->
                        <template v-else>
                            <!-- Modal header -->
                            <div class="flex items-center justify-between px-6 py-4 border-b border-border-subtle">
                                <div class="flex items-center gap-2">
                                    <div
                                        class="w-8 h-8 rounded-full bg-red-100 flex items-center justify-center shrink-0">
                                        <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor"
                                            viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                        </svg>
                                    </div>
                                    <h3 class="text-body font-semibold text-text-primary">Xác nhận hủy vé</h3>
                                </div>
                                <button @click="handleCloseCancelModal" :disabled="cancelLoading"
                                    class="p-1.5 rounded-full hover:bg-bg-base text-text-tertiary transition-colors disabled:opacity-50">
                                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </div>

                            <!-- Modal body -->
                            <div class="px-6 py-5 space-y-4">
                                <!-- Warning banner -->
                                <div class="bg-amber-50 border border-amber-200 rounded-lg p-4 space-y-1.5">
                                    <p class="text-caption font-semibold text-amber-800">Lưu ý quan trọng</p>
                                    <ul class="text-caption text-amber-700 space-y-1 list-disc list-inside">
                                        <li>Sau khi hủy, vé sẽ <strong>không còn hiệu lực</strong></li>
                                        <li>Yêu cầu hoàn tiền sẽ gửi đến quản trị viên để phê duyệt</li>
                                        <li>Thời gian xử lý: <strong>5–7 ngày làm việc</strong></li>
                                    </ul>
                                </div>

                                <!-- Refund calculation -->
                                <div class="bg-bg-base rounded-lg border border-border-subtle overflow-hidden">
                                    <div class="px-4 py-3 border-b border-border-subtle">
                                        <p class="text-caption font-semibold text-text-primary">Ước tính hoàn tiền</p>
                                    </div>
                                    <div class="px-4 py-3 space-y-2">
                                        <template v-if="refundCalcLoading">
                                            <div class="animate-pulse space-y-2">
                                                <div class="h-4 bg-gray-200 rounded w-full"></div>
                                                <div class="h-4 bg-gray-200 rounded w-3/4"></div>
                                            </div>
                                        </template>
                                        <template v-else-if="refundCalc">
                                            <div class="flex justify-between text-caption text-text-secondary">
                                                <span>Số tiền đã thanh toán</span>
                                                <span>{{ formatVND(refundCalc.originalAmount) }}</span>
                                            </div>
                                            <div class="flex justify-between text-caption text-text-secondary">
                                                <span>Tỷ lệ hoàn</span>
                                                <span class="font-medium" :class="refundCalc.refundPercentage > 0 ? 'text-green-600' : 'text-red-500'
                                                    ">
                                                    {{ refundCalc.refundPercentage }}%
                                                </span>
                                            </div>
                                            <div
                                                class="border-t border-border-subtle pt-2 flex justify-between text-body font-semibold">
                                                <span class="text-text-primary">Dự kiến hoàn</span>
                                                <span :class="refundCalc.refundAmount > 0 ? 'text-green-600' : 'text-text-secondary'
                                                    ">
                                                    {{ formatVND(refundCalc.refundAmount) }}
                                                </span>
                                            </div>
                                            <p class="text-caption text-text-tertiary italic">{{ refundCalc.message }}
                                            </p>
                                        </template>
                                        <template v-else>
                                            <p class="text-caption text-text-tertiary">
                                                Không thể tính toán hoàn tiền lúc này.
                                            </p>
                                        </template>
                                    </div>
                                </div>

                                <!-- Error message -->
                                <div v-if="cancelError"
                                    class="flex items-start gap-2 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
                                    <svg class="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                    </svg>
                                    <p class="text-caption text-red-700">{{ cancelError }}</p>
                                </div>

                                <!-- Refund policy link -->
                                <p class="text-caption text-text-tertiary text-center">
                                    Xem
                                    <button @click="goToRefundPolicy"
                                        class="text-accent hover:underline focus:outline-none">
                                        chính sách hoàn tiền
                                    </button>
                                    để biết thêm chi tiết.
                                </p>
                            </div>

                            <!-- Modal footer -->
                            <div class="px-6 py-4 border-t border-border-subtle flex gap-3 justify-end">
                                <button @click="handleCloseCancelModal" :disabled="cancelLoading"
                                    class="px-4 py-2 rounded-lg border border-border-default text-text-primary text-caption hover:bg-bg-base transition-colors disabled:opacity-50">
                                    Giữ vé
                                </button>
                                <button @click="handleConfirmCancel" :disabled="cancelLoading"
                                    class="inline-flex items-center gap-2 px-5 py-2 rounded-lg bg-red-600 text-white text-caption font-medium hover:bg-red-700 transition-colors disabled:opacity-60">
                                    <svg v-if="cancelLoading" class="w-4 h-4 animate-spin" fill="none"
                                        viewBox="0 0 24 24">
                                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                            stroke-width="4"></circle>
                                        <path class="opacity-75" fill="currentColor"
                                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                                    </svg>
                                    {{ cancelLoading ? 'Đang xử lý...' : 'Xác nhận hủy vé' }}
                                </button>
                            </div>
                        </template>
                    </div>
                </Transition>
            </div>
        </Transition>
    </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.slide-up-enter-active {
    transition: transform 0.25s ease, opacity 0.25s ease;
}

.slide-up-leave-active {
    transition: transform 0.2s ease, opacity 0.2s ease;
}

.slide-up-enter-from {
    transform: translateY(24px);
    opacity: 0;
}

.slide-up-leave-to {
    transform: translateY(12px);
    opacity: 0;
}
</style>