<template>
    <span :class="tagClasses" class="font-bold flex items-center justify-center rounded-sm transition-all">
        {{ rating }}
    </span>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { AgeRating } from '@/types/movie';

const props = defineProps<{
    rating: AgeRating;
    size?: 'sm' | 'md' | 'lg';
}>();

const bgClass = computed(() => {
    switch (props.rating) {
        case 'P':
            return 'bg-green-500 text-white';
        case 'T13':
            return 'bg-yellow-400 text-black';
        case 'T16':
            return 'bg-orange-400 text-white';
        case 'T18':
            return 'bg-orange-600 text-white';
        default:
            return 'bg-gray-400 text-white';
    }
});

const sizeClasses = computed(() => {
    switch (props.size) {
        case 'sm':
            return 'w-8 h-6 text-xs';
        case 'lg':
            return 'w-14 h-10 text-base';
        case 'md':
        default:
            return 'w-11 h-8 text-sm';
    }
});

const tagClasses = computed(() => [
    bgClass.value,
    sizeClasses.value
]);
</script>