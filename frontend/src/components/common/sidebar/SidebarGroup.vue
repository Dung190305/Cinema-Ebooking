<template>
    <div>
        <!-- Parent trigger -->
        <button class="flex w-full items-center gap-4 rounded-r-lg px-4 py-2 text-sm transition-colors" :class="hasActiveChild
            ? 'font-medium bg-accent text-text-on-accent'
            : 'text-text-admin-secondary hover:text-text-admin-primary'
            " @click="handleGroupClick">
            <component :is="item.icon" class="size-4 shrink-0" />
            <span v-if="!ui.isSidebarCollapsed" class="flex-1 text-left">{{ item.label }}</span>
            <ChevronDown class="size-3.5 shrink-0 transition-transform" :class="{ 'rotate-180': isExpanded }" />
        </button>

        <!-- Sub items — max-h trick để animate collapse -->
        <div class="overflow-hidden transition-all duration-200"
            :class="isExpanded && !ui.isSidebarCollapsed ? 'max-h-96' : 'max-h-0'">
            <div class="mt-0.5 flex flex-col gap-0.5">
                <RouterLink v-for="child in item.children" :key="child.key" :to="child.to"
                    class="rounded-r-md px-12 py-1.5 text-sm transition-all group relative" :class="route.path.startsWith(child.to)
                        ? 'text-text-on-accent bg-accent'
                        : 'text-text-admin-secondary hover:text-text-admin-primary'">
                    <div v-if="!route.path.startsWith(child.to)"
                        class="absolute left-0 top-0 h-full w-0 bg-accent transition-all ease-in-out group-hover:w-2">
                    </div>
                    {{ child.label }}
                </RouterLink>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ChevronDown } from 'lucide-vue-next'
import { useUIStore } from '@/stores/ui.store'
const ui = useUIStore()
const props = defineProps({
    item: Object, // { key, label, icon, children: [{ key, label, to }] }
})

const route = useRoute()
const isExpanded = ref(false)

const hasActiveChild = computed(() =>
    props.item.children?.some((c) => route.path.startsWith(c.to))
)
const handleGroupClick = () => {
    if (ui.isSidebarCollapsed) {
        ui.openSidebar()
    }

    isExpanded.value = !isExpanded.value
}
// Tự mở nếu đang ở trang con
onMounted(() => { if (hasActiveChild.value) isExpanded.value = true })
watch(hasActiveChild, (val) => { if (val) isExpanded.value = true })
</script>