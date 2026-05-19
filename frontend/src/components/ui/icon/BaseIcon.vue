<script setup lang="ts">
import { computed, useAttrs } from 'vue'

const props = withDefaults(defineProps<{
    icon: any
    size?: number
    strokeWidth?: number
    scale?: number
    filled?: boolean
}>(), {
    size: 20,
    strokeWidth: 2,
    scale: 1
})

const attrs = useAttrs()

const style = computed(() => ({
    width: props.size + 'px',
    height: props.size + 'px',
    transform: props.scale !== 1 ? `scale(${props.scale})` : undefined
}))
</script>

<template>
    <span :class="[
        'inline-flex items-center justify-center [&>svg]:stroke-current',
        filled && '[&>svg]:fill-current'
    ]" :style="style">
        <component :is="icon" :size="size" :stroke-width="strokeWidth" v-bind="attrs" class="w-full h-full" />
    </span>
</template>