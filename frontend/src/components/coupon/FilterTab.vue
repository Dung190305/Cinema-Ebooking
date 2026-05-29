<template>
    <button @click="$emit('click')"
        class="px-3 sm:px-4 py-1.5 rounded-full text-sm font-medium transition-all shrink-0 border" :class="active
                ? 'bg-accent text-text-on-accent border-accent'
                : 'border-border-default text-text-secondary hover:border-accent hover:text-accent bg-transparent'
            ">
        {{ label }}
    </button>
</template>

<script setup lang="ts">
defineProps<{ label: string; active: boolean }>()
defineEmits<{ click: [] }>()
</script>