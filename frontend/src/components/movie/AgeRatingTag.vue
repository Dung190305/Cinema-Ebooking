<template>
    <span class="text-body font-bold w-11 h-8 flex items-center justify-center rounded-sm" :class="bgClass">
        {{ rating }}
    </span>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { AgeRating } from '@/types/movie';

const props = defineProps<{
    rating: AgeRating;
}>();

const bgClass = computed(() => {
    switch (props.rating) {
        case 'P':
            return 'bg-green-500 text-white';
        case 'T13':
            return 'bg-yellow-400 text-black';          // vàng
        case 'T16':
            return 'bg-orange-400 text-white';          // cam nhạt
        case 'T18':
            return 'bg-orange-600 text-white';          // cam đậm
        default:
            return 'bg-gray-400 text-white';
    }
});
</script>