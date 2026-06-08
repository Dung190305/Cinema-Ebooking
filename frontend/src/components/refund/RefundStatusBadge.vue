<!-- components/refund/RefundStatusBadge.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import type { RefundStatus } from '@/types/refund.types'

const props = defineProps<{ status: RefundStatus }>()

const config = computed(() => {
    switch (props.status) {
        case 'REQUESTED':
            return {
                label: 'Chờ duyệt',
                class: 'bg-yellow-100 text-yellow-800 border-yellow-200',
                dot: 'bg-yellow-500',
            }
        case 'APPROVED':
            return {
                label: 'Đã duyệt',
                class: 'bg-blue-100 text-blue-800 border-blue-200',
                dot: 'bg-blue-500',
            }
        case 'COMPLETED':
            return {
                label: 'Hoàn tiền xong',
                class: 'bg-green-100 text-green-800 border-green-200',
                dot: 'bg-green-500',
            }
        case 'REJECTED':
            return {
                label: 'Từ chối',
                class: 'bg-red-100 text-red-800 border-red-200',
                dot: 'bg-red-500',
            }
        case 'CANCELLED':
            return {
                label: 'Đã hủy yêu cầu',
                class: 'bg-gray-100 text-gray-600 border-gray-200',
                dot: 'bg-gray-400',
            }
        default:
            return {
                label: props.status,
                class: 'bg-gray-100 text-gray-600 border-gray-200',
                dot: 'bg-gray-400',
            }
    }
})
</script>

<template>
    <span :class="[
        'inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border',
        config.class,
    ]">
        <span :class="['w-1.5 h-1.5 rounded-full shrink-0', config.dot]" />
        {{ config.label }}
    </span>
</template>