<script setup lang="ts">
import { computed } from 'vue'
import { Crown, Sparkles, Star } from 'lucide-vue-next'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import type { Component } from 'vue'

// ===== TYPES =====
type Membership = 'basic' | 'silver' | 'gold'

// ===== PROPS =====
const props = defineProps<{
    membership?: string
}>()

const tier = computed<Membership>(() => {
    const raw = (props.membership || 'basic').toLowerCase()
    return (['basic', 'silver', 'gold'].includes(raw) ? raw : 'basic') as Membership
})

// ===== CONFIG =====
const membershipConfig: Record<Membership, {
    icon: Component
    class: string
    scale: number
}> = {
    basic: {
        icon: Star,
        class: 'text-zinc-400',
        scale: 1
    },
    silver: {
        icon: Sparkles,
        class: 'text-gray-200 drop-shadow-[0_0_6px_rgba(255,255,255,0.5)]',
        scale: 1
    },
    gold: {
        icon: Crown,
        class: 'text-accent brightness-110 drop-shadow-[0_0_10px_rgba(201,167,78,0.7)]',
        scale: 1.1
    }
}

// ===== COMPUTED =====
const config = computed(() => membershipConfig[tier.value])
</script>

<template>
    <BaseIcon :icon="config.icon" :scale="config.scale" :class="config.class" :stroke-width="1.5" :size="20" />
</template>