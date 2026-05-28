<template>
  <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm flex flex-col gap-3">
    <div class="flex items-start justify-between">
      <div class="flex flex-col gap-1">
        <span class="text-xs font-medium text-gray-500 uppercase tracking-wide">{{ label }}</span>
        <div class="flex items-baseline gap-1">
          <span v-if="prefix" class="text-xl font-bold text-gray-800">{{ prefix }}</span>
          <span class="text-2xl font-bold text-gray-800">{{ formattedValue }}</span>
          <span v-if="suffix" class="text-sm font-medium text-gray-500">{{ suffix }}</span>
        </div>
      </div>
      <div
        class="flex-shrink-0 w-10 h-10 rounded-lg flex items-center justify-center"
        :class="iconBgClass"
      >
        <component :is="iconComponent" class="size-5" :class="iconColorClass" />
      </div>
    </div>

    <!-- Change Indicator -->
    <div class="flex items-center gap-1.5">
      <span
        class="flex items-center gap-0.5 text-xs font-medium px-1.5 py-0.5 rounded"
        :class="changeBgClass"
      >
        <TrendingUp v-if="change >= 0" class="size-3" />
        <TrendingDown v-else class="size-3" />
        {{ Math.abs(change).toFixed(1) }}%
      </span>
      <span class="text-xs text-gray-400">so với hôm qua</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  TrendingUp,
  TrendingDown,
  Ticket,
  Users,
  Armchair,
  Banknote,
} from 'lucide-vue-next'

const props = defineProps<{
  label: string
  value: number
  change: number
  prefix?: string
  suffix?: string
  icon: 'revenue' | 'ticket' | 'seat' | 'user'
  theme: 'gold' | 'violet' | 'emerald' | 'amber'
}>()

const formattedValue = computed(() => {
  if (props.value >= 1000000) {
    return (props.value / 1000000).toFixed(1) + 'M'
  }
  if (props.value >= 1000) {
    return (props.value / 1000).toFixed(1) + 'K'
  }
  return props.value.toLocaleString('vi-VN')
})

const iconComponent = computed(() => {
  switch (props.icon) {
    case 'revenue': return Banknote
    case 'ticket': return Ticket
    case 'seat': return Armchair
    case 'user': return Users
    default: return Banknote
  }
})

const themeMap = {
  gold: { bg: 'bg-gold-50', color: 'text-gold-500' },
  violet: { bg: 'bg-violet-50', color: 'text-violet-500' },
  emerald: { bg: 'bg-emerald-50', color: 'text-emerald-500' },
  amber: { bg: 'bg-amber-50', color: 'text-amber-500' },
}

const iconBgClass = computed(() => themeMap[props.theme]?.bg ?? 'bg-gray-50')
const iconColorClass = computed(() => themeMap[props.theme]?.color ?? 'text-gray-500')

const changeBgClass = computed(() =>
  props.change >= 0
    ? 'bg-emerald-50 text-emerald-600'
    : 'bg-red-50 text-red-500',
)
</script>
