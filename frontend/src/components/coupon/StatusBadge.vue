<!-- StatusBadge.vue -->
<template>
    <span class="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full shrink-0" :class="cls">{{
        label }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserCouponStatus } from '@/types/user-coupon.types'

const props = defineProps<{ status: UserCouponStatus }>()

const label = computed(() => {
    const map: Record<UserCouponStatus, string> = {
        ACTIVE: 'Khả dụng',
        USED: 'Đã dùng',
        EXPIRED: 'Hết hạn',
        REVOKED: 'Thu hồi',
    }
    return map[props.status]
})

const cls = computed(() => {
    const map: Record<UserCouponStatus, string> = {
        ACTIVE: 'bg-green-500/10 text-green-500',
        USED: 'bg-blue-500/10 text-blue-400',
        EXPIRED: 'bg-text-disabled/10 text-text-disabled',
        REVOKED: 'bg-red-500/10 text-red-400',
    }
    return map[props.status]
})
</script>