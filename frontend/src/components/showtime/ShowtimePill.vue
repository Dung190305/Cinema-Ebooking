<template>
    <BasePill :prefix-text="formatName" :size="size" :active="active" @click="$emit('book', showtimeId)">
        {{ formattedStart }} - {{ formattedEnd }}
    </BasePill>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatTimeVN } from '@/utils/dateFormat'
import BasePill from '@/components/ui/pill/BasePill.vue'

const props = withDefaults(defineProps<{
    showtimeId: number
    formatName: string
    startTime: string
    endTime: string
    size?: 'sm' | 'md' | 'lg'
    active?: boolean
}>(), {
    size: 'md',
    active: false
})

defineEmits<{
    book: [showtimeId: number]
}>()

const formattedStart = computed(() => formatTimeVN(props.startTime))
const formattedEnd = computed(() => formatTimeVN(props.endTime))
</script>