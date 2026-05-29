<template>
    <!-- Dashed border style gợi hình "coupon xé" -->
    <div
        class="relative rounded-xl overflow-hidden border border-dashed border-border-default bg-bg-surface transition-all hover:border-accent/60 hover:shadow-lg hover:shadow-accent/5 group">
        <!-- Left accent strip -->
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-accent rounded-l-xl" />

        <!-- Perforation divider -->
        <div class="absolute left-10 top-0 bottom-0 flex flex-col justify-between py-2 pointer-events-none">
            <div class="w-px h-full border-l border-dashed border-border-subtle" />
        </div>

        <div class="pl-14 pr-4 py-4">
            <!-- Header row -->
            <div class="flex items-start justify-between gap-2 mb-3">
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <!-- Discount badge -->
                        <span class="text-xl sm:text-2xl font-brand font-bold text-accent leading-none">
                            {{ discountLabel }}
                        </span>
                    </div>
                    <code
                        class="text-xs font-mono text-text-secondary bg-bg-base px-2 py-0.5 rounded border border-border-subtle tracking-widest">
            {{ coupon.code }}
          </code>
                </div>

                <!-- Status chip -->
                <span :class="statusClass"
                    class="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full shrink-0">
                    {{ statusLabel }}
                </span>
            </div>

            <!-- Conditions -->
            <ul class="space-y-1 mb-4">
                <li v-if="coupon.minimumBookingValue > 0" class="text-xs text-text-tertiary flex items-center gap-1.5">
                    <InfoIcon class="w-3 h-3 shrink-0" />
                    Đơn tối thiểu {{ formatVnd(coupon.minimumBookingValue) }}
                </li>
                <li v-if="coupon.type === 'PERCENT' && coupon.maximumDiscountAmount > 0"
                    class="text-xs text-text-tertiary flex items-center gap-1.5">
                    <InfoIcon class="w-3 h-3 shrink-0" />
                    Giảm tối đa {{ formatVnd(coupon.maximumDiscountAmount) }}
                </li>
                <li class="text-xs text-text-tertiary flex items-center gap-1.5">
                    <CalendarIcon class="w-3 h-3 shrink-0" />
                    HSD: {{ formatDate(coupon.endDate) }}
                </li>
                <li v-if="coupon.remainingSlots !== null" class="text-xs text-text-tertiary flex items-center gap-1.5">
                    <UsersIcon class="w-3 h-3 shrink-0" />
                    Còn {{ coupon.remainingSlots }} lượt
                </li>
            </ul>

            <!-- CTA -->
            <button :disabled="!canRedeem || loading" @click.stop="$emit('redeem')"
                class="w-full py-2 rounded-lg text-sm font-medium transition-all disabled:opacity-40 disabled:cursor-not-allowed"
                :class="canRedeem
                    ? 'bg-accent text-text-on-accent hover:opacity-90'
                    : 'bg-bg-base text-text-disabled border border-border-subtle'
                    ">
                <LoaderIcon v-if="loading" class="w-3.5 h-3.5 animate-spin inline mr-1.5" />
                <span v-else>{{ redeemButtonLabel }}</span>
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { InfoIcon, CalendarIcon, UsersIcon, LoaderIcon } from 'lucide-vue-next'
import type { PublicCouponResponse } from '@/types/coupon.types'

const props = defineProps<{
    coupon: PublicCouponResponse
    loading?: boolean
}>()

defineEmits<{ redeem: [] }>()

const discountLabel = computed(() => {
    if (props.coupon.type === 'PERCENT') return `${props.coupon.value}%`
    return formatVnd(props.coupon.value)
})

const canRedeem = computed(() => {
    const points = Number(props.coupon.pointsToRedeem)
    return (
        props.coupon.status === 'ACTIVE' &&
        (props.coupon.remainingSlots === null || props.coupon.remainingSlots > 0) &&
        points === 0
    )
})

const redeemButtonLabel = computed(() => {
    if (props.coupon.status !== 'ACTIVE') return 'Không khả dụng'
    if (props.coupon.remainingSlots !== null && props.coupon.remainingSlots === 0) return 'Hết lượt'
    const points = Number(props.coupon.pointsToRedeem)
    if (points > 0) return `${points.toLocaleString()} điểm`
    return 'Lưu coupon'
})

const statusLabel = computed(() => {
    const map: Record<string, string> = {
        ACTIVE: 'Đang có',
        EXPIRED: 'Hết hạn',
        OUT_OF_STOCK: 'Hết lượt',
        DISABLED: 'Ngừng',
        DRAFT: 'Nháp',
    }
    return map[props.coupon.status] ?? props.coupon.status
})

const statusClass = computed(() => {
    if (props.coupon.status === 'ACTIVE') return 'bg-green-500/10 text-green-500'
    if (props.coupon.status === 'EXPIRED' || props.coupon.status === 'OUT_OF_STOCK')
        return 'bg-text-disabled/10 text-text-disabled'
    return 'bg-text-disabled/10 text-text-disabled'
})

function formatVnd(value: number) {
    return value.toLocaleString('vi-VN') + '₫'
}

function formatDate(dateStr: string) {
    return new Date(dateStr).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    })
}
</script>