<template>
    <div class="border-b border-border-default">
        <button @click="toggleExpand"
            class="w-full flex items-center justify-between px-4 py-3 text-text-primary hover:bg-accent/10 rounded-lg transition-colors">
            <span class="text-body font-medium">{{ label }}</span>
            <BaseIcon :icon="ChevronDown" :size="18" :class="{ 'rotate-180': isExpanded }"
                class="transition-transform" />
        </button>

        <Transition name="expand">
            <div v-if="isExpanded" class="ml-4 space-y-1 pb-2">
                <button v-for="(item, idx) in items" :key="idx" @click="navigate(item)"
                    class="block w-full text-left px-4 py-2 text-text-secondary hover:bg-accent/10 hover:text-accent rounded-lg transition-colors">
                    {{ item.label }}
                </button>
            </div>
        </Transition>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { ChevronDown } from 'lucide-vue-next'

const props = defineProps<{
    label: string
    items: Array<{ label: string; route: string; query?: any; getQuery?: () => any }>
}>()

const emit = defineEmits<{ close: [] }>()
const router = useRouter()
const isExpanded = ref(false)

const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
}

const navigate = (item: any) => {
    let query = item.query
    if (item.getQuery) {
        query = item.getQuery()
    }
    router.push({ path: item.route, query })
    emit('close')
}
</script>

<style scoped>
.expand-enter-active,
.expand-leave-active {
    transition: all 0.2s ease;
}

.expand-enter-from,
.expand-leave-to {
    opacity: 0;
    transform: translateY(-8px);
}
</style>