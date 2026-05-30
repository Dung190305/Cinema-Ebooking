<template>
    <div class="flex items-stretch rounded-xl overflow-hidden border border-border-subtle bg-bg-surface transition-all"
        :class="isActive ? 'hover:border-accent/50' : 'opacity-60'">
        <!-- Left accent strip + discount value -->
        <div class="w-20 sm:w-24 shrink-0 flex flex-col items-center justify-center gap-1 px-2 py-4"
            :class="isActive ? 'bg-accent/10' : 'bg-bg-base'">
            <span class="text-lg sm:text-xl font-brand font-bold leading-none"
                :class="isActive ? 'text-accent' : 'text-text-disabled'">
                {{ discountLabel }}
            </span>
            <span class="text-[10px] text-text-tertiary uppercase tracking-wider">
                {{ item.couponType === 'PERCENT' ? 'giảm' : 'giảm' }}
            </span>
        </div>

        <!-- Dashed divider -->
        <div class="flex items-center px-0">
            <div class="h-full border-l-2 border-dashed border-border-subtle" />
        </div>

        <!-- Content -->
        <div class="flex-1 px-4 py-3 min-w-0">
            <div class="flex items-start justify-between gap-2 mb-1">
                <code
                    class="text-xs font-mono text-text-secondary tracking-widest bg-bg-base px-2 py-0.5 rounded border border-border-subtle">
          {{ item.couponCode }}
        </code>
                <StatusBadge :status="item.status" />
            </div>

            <div class="space-y-0.5 mt-2">
                <p v-if="item.minimumBookingValue > 0" class="text-xs text-text-tertiary">
                    Đơn tối thiểu {{ formatVnd(item.minimumBookingValue) }}
                </p>
                <p v-if="item.couponType === 'PERCENT' && item.maximumDiscountAmount > 0"
                    class="text-xs text-text-tertiary">
                    Giảm tối đa {{ formatVnd(item.maximumDiscountAmount) }}
                </p>
                <div class="flex items-center gap-3 flex-wrap">
                    <p class="text-xs text-text-tertiary">
                        HSD: {{ formatDate(item.expiredAt) }}
                    </p>
                    <p v-if="item.usageRemain > 0" class="text-xs text-text-tertiary">
                        Còn {{ item.usageRemain }} lượt dùng
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserCouponResponse } from '@/types/user-coupon.types'
import StatusBadge from '@/components/coupon/StatusBadge.vue'

const props = defineProps<{ item: UserCouponResponse }>()

const isActive = computed(() => props.item.status === 'ACTIVE')

const discountLabel = computed(() => {
    if (props.item.couponType === 'PERCENT') return `${props.item.couponValue}%`
    return formatVnd(props.item.couponValue)
})

function formatVnd(value: number) {
    if (value >= 1_000_000) return (value / 1_000_000).toFixed(0) + 'M'
    if (value >= 1_000) return (value / 1_000).toFixed(0) + 'K'
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