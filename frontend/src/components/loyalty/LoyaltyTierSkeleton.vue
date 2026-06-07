<!-- components/loyalty/LoyaltyTierSkeleton.vue -->
<script setup lang="ts">
import Skeleton from '@/components/ui/skeleton/Skeleton.vue'

const skeletonBlocks = [
    { type: 'box', height: '24px', width: '60%', class: 'rounded-lg mb-4' },
    { type: 'text', width: '40%', height: '32px', class: 'mb-3' },
    { type: 'box', height: '70px', class: 'rounded-xl mb-4' },
    { type: 'text', width: '80%', class: 'mb-2' },
    { type: 'text', width: '90%', class: 'mb-2' },
    { type: 'text', width: '50%', class: 'mb-4' },
    { type: 'box', height: '50px', class: 'rounded-lg' }
]
</script>

<template>
    <div class="bg-bg-surface rounded-2xl p-5 sm:p-6 border border-border-subtle">
        <Skeleton :blocks="skeletonBlocks" />
    </div>
</template>