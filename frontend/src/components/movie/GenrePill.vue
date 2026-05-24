<!-- components/movie/GenrePill.vue -->
<template>
    <BasePill :label="genre.name" size="md" class="bg-overlay-light-30" @click="$emit('click', genre.id)" />
</template>

<script setup lang="ts">
import BasePill from '@/components/ui/pill/BasePill.vue'
import type { Genre } from '@/types/movie.types'

defineProps<{
    genre: Genre
}>()

defineEmits<{
    click: [genreId: number]
}>()
</script>