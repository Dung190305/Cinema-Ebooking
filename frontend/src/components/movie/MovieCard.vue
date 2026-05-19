<template>
    <div class="flex flex-col items-start gap-2 max-w-70">
        <!-- Card container -->
        <div class="w-full aspect-2/3 relative flex items-end justify-end group">
            <img class="w-full h-full absolute top-1/2 left-1/2 -translate-1/2 rounded-lg object-cover" :src="posterSrc"
                :alt="movie.title" />

            <!-- Rating & Age Rating badges -->
            <div class="flex flex-col items-center gap-2 py-2 z-10">
                <div v-if="movie.rating !== null"
                    class="flex gap-2 px-2 py-1 bg-overlay-light-30 rounded-l-sm items-center">
                    <BaseIcon :icon="Star" :size="16" class="text-yellow-400" filled />
                    <div class="text-body font-bold text-text-primary">
                        {{ movie.rating.toFixed(1) }}
                    </div>
                </div>
                <AgeRatingTag :rating="movie.ageRating" />
            </div>

            <!-- Hover overlay -->
            <div
                class="w-full h-full absolute top-1/2 left-1/2 -translate-1/2 rounded-lg bg-overlay-light-70 z-20 flex items-center justify-center opacity-0 pointer-events-none transition-opacity duration-200 group-hover:opacity-100 ">
                <BaseButton variant="primary" size="md" rounded="md" @click="$emit('book', movie.id)"
                    class="group-hover:pointer-events-auto cursor-pointer">
                    <div class="flex gap-1 items-center">
                        <BaseIcon :icon="TicketPlus" :size="24" />
                        <span class="text-body">Đặt vé</span>
                    </div>
                </BaseButton>
            </div>
        </div>

        <!-- Title below the card -->
        <p class="text-body font-semibold text-text-primary">
            {{ movie.title }}
        </p>
    </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { Star, TicketPlus } from 'lucide-vue-next'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import AgeRatingTag from '@/components/movie/AgeRatingTag.vue'
import type { MovieResponse } from '@/types/movie'
import { useCloudinaryImage } from '@/composables/useCloudinaryImage'

const props = defineProps<{
    movie: MovieResponse
}>()

defineEmits<{
    book: [id: number]
}>()

const { getTransformedUrl } = useCloudinaryImage()

// Tự động tạo poster URL với kích thước tối ưu
const posterSrc = computed(() =>
    getTransformedUrl(props.movie.posterUrl, { width: 600, crop: 'fill', gravity: 'auto' })
)
</script>