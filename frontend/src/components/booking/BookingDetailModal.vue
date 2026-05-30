<!-- components/booking/BookingDetailModal.vue -->
<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { BookingDetailResponse } from '@/types/booking.types'
import { bookingApi } from '@/api/booking.api'
import { formatDateTimeVN } from '@/utils/dateFormat'
import { formatVND } from '@/utils/currency'
import { getStatusBadgeClass, getStatusText } from '@/utils/bookingStatus'

const props = defineProps<{
    bookingId: number | null
}>()

const emit = defineEmits<{
    (e: 'close'): void
}>()

// ── State ──────────────────────────────────────────────────────────────────
const loading = ref(false)
const loadingQR = ref(false)
const detail = ref<BookingDetailResponse | null>(null)
const qrBase64 = ref<string | null>(null)
const error = ref<string | null>(null)

// ── Fetch ──────────────────────────────────────────────────────────────────
async function fetchDetail(id: number) {
    loading.value = true
    error.value = null
    detail.value = null
    qrBase64.value = null

    try {
        detail.value = await bookingApi.getById(id)
        // Chỉ fetch QR nếu đã được xác nhận thanh toán
        if (detail.value.status === 'CONFIRMED') {
            fetchQR(id)
        }
    } catch {
        error.value = 'Không thể tải thông tin đơn hàng. Vui lòng thử lại.'
    } finally {
        loading.value = false
    }
}

async function fetchQR(id: number) {
    loadingQR.value = true
    try {
        const response = await bookingApi.getQRCode(id)
        qrBase64.value = response.base64Image
    } catch {
        // QR không critical — silent fail
    } finally {
        loadingQR.value = false
    }
}

watch(
    () => props.bookingId,
    (id) => {
        if (id !== null) fetchDetail(id)
    },
    { immediate: true }
)

const hasDiscount = computed(() =>
    detail.value && (
        (detail.value.tierDiscountAmount ?? 0) > 0 ||
        (detail.value.couponDiscountAmount ?? 0) > 0
    )
)

function close() {
    emit('close')
}

function onBackdropClick(e: MouseEvent) {
    if (e.target === e.currentTarget) close()
}
</script>

<template>
    <!-- Backdrop -->
    <Teleport to="body">
        <Transition name="fade">
            <div v-if="bookingId !== null"
                class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
                @click="onBackdropClick">
                <!-- Modal panel -->
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
                                    <div class="h-4 bg-gray-200 rounded w-2/3"></div>
                                    <div class="grid grid-cols-2 gap-3 mt-2">
                                        <div class="h-10 bg-gray-200 rounded-lg"></div>
                                        <div class="h-10 bg-gray-200 rounded-lg"></div>
                                        <div class="h-10 bg-gray-200 rounded-lg"></div>
                                        <div class="h-10 bg-gray-200 rounded-lg"></div>
                                    </div>
                                    <div class="h-32 bg-gray-200 rounded-xl mt-2"></div>
                                    <div class="h-48 bg-gray-200 rounded-xl mx-auto w-48"></div>
                                </div>
                            </template>

                            <!-- ERROR -->
                            <template v-else-if="error">
                                <div class="text-center py-10">
                                    <p class="text-body text-red-500">{{ error }}</p>
                                    <button @click="bookingId !== null && fetchDetail(bookingId)"
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
                                        <span
                                            :class="['px-3 py-1 rounded-full text-xs font-semibold shrink-0', getStatusBadgeClass(detail.status)]">
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
                                                <p class="text-body font-semibold text-text-primary">{{
                                                    detail.movieTitle }}</p>
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
                                                <p class="text-caption font-medium text-text-primary">{{
                                                    formatVND(seat.price) }}</p>
                                            </div>
                                        </div>
                                    </div>
                                </section>

                                <!-- ③ Combos (nếu có) -->
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
                                            <p class="text-body font-semibold text-text-primary">{{
                                                formatVND(combo.totalPrice) }}</p>
                                        </div>
                                    </div>
                                </section>

                                <!-- ④ Price breakdown -->
                                <section class="bg-bg-base rounded-xl border border-border-subtle overflow-hidden">
                                    <div class="px-4 py-3 space-y-2">
                                        <div class="flex justify-between text-caption text-text-secondary">
                                            <span>Tổng vé</span>
                                            <span>{{ formatVND(detail.totalTicketPrice) }}</span>
                                        </div>
                                        <div v-if="(detail.totalComboPrice ?? 0) > 0"
                                            class="flex justify-between text-caption text-text-secondary">
                                            <span>Bắp / Nước</span>
                                            <span>{{ formatVND(detail.totalComboPrice) }}</span>
                                        </div>

                                        <template v-if="hasDiscount">
                                            <div class="border-t border-border-subtle my-1"></div>
                                            <div v-if="(detail.tierDiscountAmount ?? 0) > 0"
                                                class="flex justify-between text-caption text-green-700">
                                                <span>
                                                    Ưu đãi thành viên
                                                    <template v-if="detail.membershipTierName">
                                                        ({{ detail.membershipTierName }}
                                                        <template v-if="detail.membershipDiscountPercent">
                                                            · {{ detail.membershipDiscountPercent }}%
                                                        </template>)
                                                    </template>
                                                </span>
                                                <span>-{{ formatVND(detail.tierDiscountAmount) }}</span>
                                            </div>
                                            <div v-if="(detail.couponDiscountAmount ?? 0) > 0"
                                                class="flex justify-between text-caption text-green-700">
                                                <span>
                                                    Mã giảm giá
                                                    <span v-if="detail.coupon" class="font-mono">({{ detail.coupon.code
                                                        }})</span>
                                                </span>
                                                <span>-{{ formatVND(detail.couponDiscountAmount) }}</span>
                                            </div>
                                        </template>
                                    </div>

                                    <div
                                        class="px-4 py-3 bg-accent/5 border-t border-border-subtle flex justify-between items-center">
                                        <span class="text-body font-semibold text-text-primary">Tổng thanh toán</span>
                                        <span class="text-title font-bold text-accent">{{ formatVND(detail.finalAmount)
                                            }}</span>
                                    </div>
                                </section>

                                <!-- ⑤ Timestamps -->
                                <section class="grid grid-cols-1 sm:grid-cols-3 gap-3 text-caption">
                                    <div class="bg-bg-base rounded-lg px-3 py-2 border border-border-subtle">
                                        <p class="text-text-tertiary">Đặt lúc</p>
                                        <p class="text-text-primary font-medium mt-0.5">{{
                                            formatDateTimeVN(detail.createdAt) }}</p>
                                    </div>
                                    <div v-if="detail.paidAt"
                                        class="bg-bg-base rounded-lg px-3 py-2 border border-border-subtle">
                                        <p class="text-text-tertiary">Thanh toán</p>
                                        <p class="text-text-primary font-medium mt-0.5">{{
                                            formatDateTimeVN(detail.paidAt) }}</p>
                                    </div>
                                    <div v-if="detail.expiredAt && detail.status === 'PENDING'"
                                        class="bg-yellow-50 rounded-lg px-3 py-2 border border-yellow-200">
                                        <p class="text-yellow-700">Hết hạn lúc</p>
                                        <p class="text-yellow-900 font-medium mt-0.5">{{
                                            formatDateTimeVN(detail.expiredAt) }}</p>
                                    </div>
                                </section>

                                <!-- ⑥ QR Code (chỉ hiển thị khi CONFIRMED) -->
                                <section v-if="detail.status === 'CONFIRMED'"
                                    class="flex flex-col items-center pt-2 pb-1">
                                    <h3 class="text-body font-semibold text-text-primary mb-4">QR Check-in</h3>

                                    <!-- QR skeleton -->
                                    <div v-if="loadingQR" class="animate-pulse w-48 h-48 bg-gray-200 rounded-xl"></div>

                                    <!-- QR image -->
                                    <template v-else-if="qrBase64">
                                        <div class="p-3 bg-white rounded-xl shadow-sm border border-border-subtle">
                                            <img :src="`data:image/png;base64,${qrBase64}`" alt="QR Check-in"
                                                class="w-44 h-44 sm:w-52 sm:h-52" />
                                        </div>
                                        <p class="text-caption text-text-tertiary mt-3 text-center">
                                            Xuất trình mã QR này tại quầy để nhận vé
                                        </p>
                                    </template>

                                    <!-- QR error (silent) -->
                                    <p v-else class="text-caption text-text-secondary">Không thể tải mã QR.</p>
                                </section>

                            </template>
                        </div>

                        <!-- ── Footer ── -->
                        <div class="px-6 py-4 border-t border-border-subtle shrink-0 flex justify-end">
                            <button @click="close"
                                class="px-5 py-2 rounded-lg border border-border-default text-text-primary text-caption hover:bg-bg-base transition-colors">
                                Đóng
                            </button>
                        </div>

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