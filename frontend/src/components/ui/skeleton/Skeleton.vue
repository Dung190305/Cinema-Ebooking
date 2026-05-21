<!-- components/ui/skeleton/Skeleton.vue -->
<script setup lang="ts">
export interface SkeletonBlock {
    type: 'text' | 'box' | 'avatar' | 'circle'
    width?: string | number
    height?: string | number
    class?: string
    rounded?: string
    repeat?: number
}

const props = defineProps<{
    blocks: SkeletonBlock[]
}>()

const getSize = (value?: string | number) => {
    if (value === undefined) return '100%'
    if (typeof value === 'number') return `${value}px`
    return value
}
</script>

<template>
    <div class="animate-pulse space-y-3">
        <template v-for="(block, idx) in blocks" :key="idx">
            <div v-for="n in (block.repeat || 1)" :key="n">
                <!-- Text line -->
                <div v-if="block.type === 'text'" class="bg-gray-200 dark:bg-gray-700 rounded"
                    :style="{ width: getSize(block.width), height: getSize(block.height || '1rem') }"
                    :class="block.class"></div>

                <!-- Box / Card -->
                <div v-else-if="block.type === 'box'" class="bg-gray-200 dark:bg-gray-700"
                    :style="{ width: getSize(block.width), height: getSize(block.height || '3rem') }"
                    :class="[block.rounded || 'rounded-md', block.class]"></div>

                <!-- Avatar (hình tròn) -->
                <div v-else-if="block.type === 'avatar'" class="bg-gray-200 dark:bg-gray-700 rounded-full"
                    :style="{ width: getSize(block.width || '3rem'), height: getSize(block.height || '3rem') }"
                    :class="block.class"></div>

                <!-- Circle chung chung -->
                <div v-else-if="block.type === 'circle'" class="bg-gray-200 dark:bg-gray-700 rounded-full"
                    :style="{ width: getSize(block.width || '2rem'), height: getSize(block.height || '2rem') }"
                    :class="block.class"></div>
            </div>
        </template>
    </div>
</template>