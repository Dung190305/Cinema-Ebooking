<!-- src/components/common/home/HeroSection.vue -->
<template>
    <div class="w-full pt-12 pb-5">
        <AppCarousel :items="carouselItems" :edgeButtons="true" :peekPercent="15" :gap="48" :autoplay="!loading"
            :interval="4000" :dots="true">
            <template #default="{ item }">
                <!-- Skeleton banner -->
                <div v-if="item._skeleton"
                    class="flex h-90 w-full items-center justify-center relative overflow-hidden rounded-lg bg-bg-surface animate-pulse" />
                <!-- Banner thật -->
                <div v-else
                    class="flex h-90 w-full items-center justify-center text-white relative overflow-hidden rounded-lg">
                    <img v-if="item.imageUrl" :src="item.imageUrl" alt=""
                        class="absolute inset-0 w-full h-full object-cover" />
                </div>
            </template>
        </AppCarousel>

        <BookingQuickSelector class="-translate-y-4" />
    </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import AppCarousel from '@/components/common/AppCarousel.vue'
import BookingQuickSelector from '@/components/common/home/subcomponents/BookingQuickSelector.vue'
import { useHeroBanner } from '@/composables/useHeroBanner'

const { banners, loading } = useHeroBanner()

const carouselItems = computed(() => {
    if (loading.value) {
        return Array.from({ length: 3 }, (_, i) => ({ _skeleton: true, _id: i }))
    }
    return banners.value
})
</script>