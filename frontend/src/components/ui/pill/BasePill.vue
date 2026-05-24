<template>
    <button :class="pillClasses" v-bind="$attrs" @click="$emit('click')">
        <!-- Phần prefix (format, icon, badge…) -->
        <span v-if="$slots.prefix || prefixText" class="inline-flex items-center gap-1 text-xs leading-none">
            <slot name="prefix">
                <span v-if="prefixText"
                    class="bg-accent/20 text-accent px-1 py-0.5 rounded text-[10px] font-semibold">{{ prefixText
                    }}</span>
            </slot>
        </span>

        <!-- Nội dung chính -->
        <slot>
            <span>{{ label }}</span>
        </slot>

        <!-- Phần suffix (nếu cần) -->
        <span v-if="$slots.suffix" class="inline-flex items-center">
            <slot name="suffix" />
        </span>
    </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
    label?: string
    prefixText?: string
    active?: boolean
    size?: 'sm' | 'md' | 'lg'
    variant?: 'outline' | 'ghost'
}>(), {
    active: false,
    size: 'md',
    variant: 'outline'
})

defineEmits<{
    click: []
}>()

const pillClasses = computed(() => [
    'inline-flex items-center gap-1.5 rounded-full font-medium transition-all border',
    {
        'px-2.5 py-0.5 text-xs': props.size === 'sm',
        'px-3 py-2 text-xs': props.size === 'md',
        'px-4 py-2 text-sm': props.size === 'lg',
    },
    props.active
        ? 'bg-accent text-white border-accent shadow-lg shadow-accent/25'
        : props.variant === 'outline'
            ? 'border-border-default bg-transparent hover:border-accent hover:text-accent'
            : 'border-transparent bg-transparent hover:bg-surface-hover'
])
</script>