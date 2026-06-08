<!-- components/refund/RefundHistoryModal.vue -->
<script setup lang="ts">
import { watch } from 'vue'
import { useRefundHistory } from '@/composables/useRefundHistory'
import { formatDateTimeVN } from '@/utils/dateFormat'
import { formatVND } from '@/utils/currency'
import RefundStatusBadge from '@/components/refund/RefundStatusBadge.vue'
import type { RefundResponse } from '@/types/refund.types'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()

const { loading, refunds, error, hasLoaded, fetchRefunds } = useRefundHistory()

// Fetch lần đầu khi modal mở, không re-fetch nếu đã có data
watch(
    () => props.open,
    (isOpen) => {
        if (isOpen && !hasLoaded.value) fetchRefunds()
    },
    { immediate: true },
)

function close() {
    emit('close')
}

function onBackdropClick(e: MouseEvent) {
    if (e.target === e.currentTarget) close()
}

const refundStepLabels: Record<string, { step: number; label: string }> = {
    REQUESTED: { step: 1, label: 'Đã gửi yêu cầu' },
    APPROVED: { step: 2, label: 'Admin đã duyệt' },
    COMPLETED: { step: 3, label: 'Hoàn tiền thành công' },
    REJECTED: { step: 0, label: 'Bị từ chối' },
    CANCELLED: { step: 0, label: 'Đã hủy yêu cầu' },
}
</script>

<template>
    <Teleport to="body">
        <Transition name="fade">
            <div v-if="open"
                class="fixed inset-0 z-50 flex items-start justify-end pt-16 pr-4 sm:pr-6 bg-black/40 backdrop-blur-sm"
                @click="onBackdropClick">
                <Transition name="slide-down">
                    <div v-if="open"
                        class="w-full max-w-sm bg-bg-surface rounded-2xl shadow-2xl border border-border-subtle flex flex-col overflow-hidden max-h-[80vh]"
                        @click.stop>
                        <!-- Header -->
                        <div class="flex items-center justify-between px-5 py-4 border-b border-border-subtle shrink-0">
                            <div class="flex items-center gap-2.5">
                                <div class="w-8 h-8 rounded-full bg-accent/10 flex items-center justify-center">
                                    <svg class="w-4 h-4 text-accent" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                                    </svg>
                                </div>
                                <div>
                                    <h2 class="text-body font-semibold text-text-primary">Yêu cầu hoàn tiền</h2>
                                    <p class="text-caption text-text-tertiary">Theo dõi trạng thái xử lý</p>
                                </div>
                            </div>
                            <button @click="close"
                                class="p-1.5 rounded-full hover:bg-bg-base text-text-tertiary transition-colors"
                                aria-label="Đóng">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>

                        <!-- Body -->
                        <div class="overflow-y-auto flex-1">
                            <!-- Loading -->
                            <template v-if="loading">
                                <div class="px-5 py-4 space-y-4">
                                    <div v-for="i in 3" :key="i" class="animate-pulse space-y-2">
                                        <div class="h-4 bg-gray-200 rounded w-3/4"></div>
                                        <div class="h-3 bg-gray-200 rounded w-1/2"></div>
                                        <div class="h-3 bg-gray-200 rounded w-full"></div>
                                        <div class="border-b border-border-subtle pt-3"></div>
                                    </div>
                                </div>
                            </template>

                            <!-- Error -->
                            <template v-else-if="error">
                                <div class="px-5 py-8 text-center">
                                    <p class="text-caption text-red-500 mb-3">{{ error }}</p>
                                    <button @click="fetchRefunds"
                                        class="px-4 py-2 rounded-lg bg-accent text-white text-caption hover:opacity-90 transition-opacity">
                                        Thử lại
                                    </button>
                                </div>
                            </template>

                            <!-- Empty -->
                            <template v-else-if="refunds.length === 0">
                                <div class="px-5 py-10 text-center flex flex-col items-center gap-3">
                                    <div
                                        class="w-14 h-14 rounded-full bg-bg-base border border-border-subtle flex items-center justify-center">
                                        <svg class="w-7 h-7 text-text-tertiary" fill="none" stroke="currentColor"
                                            viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                                        </svg>
                                    </div>
                                    <p class="text-body text-text-secondary">Chưa có yêu cầu hoàn tiền nào</p>
                                    <p class="text-caption text-text-tertiary">
                                        Khi bạn yêu cầu hủy vé đã thanh toán, trạng thái sẽ hiển thị tại đây.
                                    </p>
                                </div>
                            </template>

                            <!-- Refund list -->
                            <template v-else>
                                <ul class="divide-y divide-border-subtle">
                                    <li v-for="refund in refunds" :key="refund.id"
                                        class="px-5 py-4 hover:bg-bg-base transition-colors">
                                        <!-- Top row: booking ID + status -->
                                        <div class="flex items-start justify-between gap-2 mb-2">
                                            <div>
                                                <p class="text-caption text-text-tertiary">Đơn #{{ refund.bookingId }}
                                                </p>
                                                <p class="text-body font-semibold text-text-primary">
                                                    {{ formatVND(refund.refundAmount) }}
                                                    <span class="text-caption text-text-tertiary font-normal ml-1">
                                                        / {{ formatVND(refund.originalAmount) }}
                                                    </span>
                                                </p>
                                            </div>
                                            <RefundStatusBadge :status="refund.status" class="shrink-0 mt-0.5" />
                                        </div>

                                        <!-- Progress stepper (chỉ hiện khi không bị reject/cancel) -->
                                        <template v-if="refund.status !== 'REJECTED' && refund.status !== 'CANCELLED'">
                                            <div class="flex items-center gap-1 mb-2.5">
                                                <template
                                                    v-for="(stepLabel, idx) in ['Đã gửi', 'Đã duyệt', 'Hoàn tiền']"
                                                    :key="idx">
                                                    <div class="flex items-center gap-1">
                                                        <div :class="[
                                                            'w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0',
                                                            (refundStepLabels[refund.status]?.step ?? 0) > idx
                                                                ? 'bg-accent text-white'
                                                                : (refundStepLabels[refund.status]?.step ?? 0) === idx + 1
                                                                    ? 'bg-accent text-white ring-2 ring-accent/30'
                                                                    : 'bg-bg-base border border-border-default text-text-tertiary',
                                                        ]">
                                                            <svg v-if="(refundStepLabels[refund.status]?.step ?? 0) > idx + 1"
                                                                class="w-3 h-3" fill="none" stroke="currentColor"
                                                                viewBox="0 0 24 24">
                                                                <path stroke-linecap="round" stroke-linejoin="round"
                                                                    stroke-width="2.5" d="M5 13l4 4L19 7" />
                                                            </svg>
                                                            <span v-else>{{ idx + 1 }}</span>
                                                        </div>
                                                        <span class="text-[10px] text-text-tertiary hidden sm:block">{{
                                                            stepLabel }}</span>
                                                    </div>
                                                    <div v-if="idx < 2" :class="[
                                                        'flex-1 h-px',
                                                        (refundStepLabels[refund.status]?.step ?? 0) > idx + 1
                                                            ? 'bg-accent'
                                                            : 'bg-border-subtle',
                                                    ]" />
                                                </template>
                                            </div>
                                        </template>

                                        <!-- Meta info -->
                                        <div class="space-y-1">
                                            <div class="flex justify-between text-caption text-text-tertiary">
                                                <span>Hoàn {{ refund.refundPercentage }}% đơn hàng</span>
                                                <span>{{ formatDateTimeVN(refund.requestedAt) }}</span>
                                            </div>
                                            <p v-if="refund.reason" class="text-caption text-text-secondary italic">
                                                "{{ refund.reason }}"
                                            </p>
                                            <p v-if="refund.adminNote" class="text-caption text-text-secondary">
                                                <span class="font-medium">Ghi chú admin:</span> {{ refund.adminNote }}
                                            </p>
                                            <p v-if="refund.processedAt" class="text-caption text-text-tertiary">
                                                Xử lý: {{ formatDateTimeVN(refund.processedAt) }}
                                            </p>
                                        </div>
                                    </li>
                                </ul>
                            </template>
                        </div>

                        <!-- Footer -->
                        <div class="px-5 py-3 border-t border-border-subtle shrink-0 flex items-center justify-between">
                            <p class="text-caption text-text-tertiary">
                                {{ refunds.length }} yêu cầu
                            </p>
                            <button @click="fetchRefunds" :disabled="loading"
                                class="inline-flex items-center gap-1.5 text-caption text-accent hover:underline disabled:opacity-50">
                                <svg class="w-3.5 h-3.5" :class="{ 'animate-spin': loading }" fill="none"
                                    stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                                Làm mới
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

.slide-down-enter-active {
    transition: transform 0.25s ease, opacity 0.25s ease;
}

.slide-down-leave-active {
    transition: transform 0.2s ease, opacity 0.2s ease;
}

.slide-down-enter-from {
    transform: translateY(-16px);
    opacity: 0;
}

.slide-down-leave-to {
    transform: translateY(-8px);
    opacity: 0;
}
</style>