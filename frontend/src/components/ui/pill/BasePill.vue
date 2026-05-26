<template>
    <button :class="pillClasses" v-bind="$attrs" @click="$emit('click')">
        <!-- Prefix Badge -->
        <span v-if="$slots.prefix || prefixText" class="inline-flex items-center gap-1 text-xs leading-none">
            <slot name="prefix">
                <span v-if="prefixText" :class="prefixClasses">
                    {{ prefixText }}
                </span>
            </slot>
        </span>

        <!-- Nội dung chính -->
        <slot>
            <span class="font-medium">{{ label }}</span>
        </slot>

        <!-- Suffix -->
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

const prefixClasses = computed(() => [
    'px-1.5 py-0.5 rounded-full text-[10px] font-semibold tracking-wider',
    props.active
        ? 'bg-accent/10 text-accent border border-accent/30'     // Active: nổi bật hơn
        : 'bg-accent/20 text-accent'
])

const pillClasses = computed(() => [
    'inline-flex items-center gap-1.5 rounded-full font-medium transition-all duration-200 border',
    'focus:outline-none focus-visible:ring-2 focus-visible:ring-accent/50',

    // Size
    {
        'px-3 py-1 text-xs': props.size === 'sm',
        'px-4 py-1.5 text-sm': props.size === 'md',
        'px-5 py-2 text-sm': props.size === 'lg',
    },

    // Active State - Chỉ border + text highlight (không đổi background)
    props.active
        ? [
            'border-accent text-accent',
            'shadow-md shadow-accent/20',
            'scale-[1.02]',
            'bg-accent/5'                    // Nền rất nhạt để tăng độ nổi
        ]
        : props.variant === 'outline'
            ? [
                'border-border-default bg-transparent text-default',
                'hover:border-accent hover:text-accent hover:bg-accent/5',
                'active:scale-95 active:bg-accent/10'
            ]
            : [
                'border-transparent bg-transparent text-default',
                'hover:bg-surface-hover hover:text-accent',
                'active:scale-95 active:bg-accent/10'
            ]
])
</script>