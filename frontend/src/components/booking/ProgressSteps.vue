<script setup lang="ts">
defineProps<{ currentStep: number }>()
defineEmits(['update:step'])

const steps = [
    { num: 1, label: 'Chọn rạp' },
    { num: 2, label: 'Phim & Suất' },
    { num: 3, label: 'Chọn ghế' },
    { num: 4, label: 'Bắp nước' },
    { num: 5, label: 'Thanh toán' }
]
</script>

<template>
    <nav class="flex items-center justify-between max-w-2xl mx-auto">
        <button v-for="(step, i) in steps" :key="step.num" class="flex flex-col items-center gap-1 text-caption" :class="{
            'text-accent font-semibold': step.num === currentStep,
            'text-text-secondary': step.num !== currentStep,
            'cursor-not-allowed pointer-events-none opacity-50': step.num > currentStep
        }" :disabled="step.num > currentStep" @click="step.num <= currentStep && $emit('update:step', step.num)">
            <span class="w-8 h-8 rounded-full flex items-center justify-center text-sm border-2 transition-colors"
                :class="step.num === currentStep
                    ? 'border-accent bg-accent text-text-on-accent'
                    : 'border-border-default bg-bg-base'">
                {{ step.num }}
            </span>
            <span class="hidden sm:inline">{{ step.label }}</span>
        </button>
    </nav>
</template>