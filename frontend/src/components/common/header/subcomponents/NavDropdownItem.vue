<!-- components/layout/NavDropdownItem.vue -->
<template>
    <div class="relative inline-block">
        <!-- Trigger -->
        <a ref="triggerRef"
            class="flex items-center gap-2 hover:underline hover:text-accent text-text-primary text-body cursor-pointer select-none"
            @mouseenter="handleTriggerEnter" @mouseleave="handleTriggerLeave">
            {{ label }}
            <BaseIcon v-if="items.length" :icon="ChevronDown" :size="14" :scale="1.2" :stroke-width="1.5" />
        </a>

        <!-- Dropdown -->
        <Transition name="nav-dropdown">
            <div v-if="showDropdown && items.length" ref="dropdownRef" @mouseenter="handleDropdownEnter"
                @mouseleave="handleDropdownLeave"
                class="absolute left-1/2 -translate-x-1/2 top-full mt-2 w-48 bg-bg-surface border border-border-default rounded-md shadow-md z-50">
                <div v-for="(item, index) in items" :key="index"
                    class="block px-4 py-2 text-sm text-text-primary hover:bg-accent cursor-pointer transition-colors"
                    @click="item.onClick?.()">
                    {{ item.label }}
                </div>
            </div>
        </Transition>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ChevronDown } from 'lucide-vue-next';
import BaseIcon from '@/components/ui/icon/BaseIcon.vue';
import { useSafeTriangleHover } from '@/composables/useSafeTriangleHover';

export interface NavDropdownItemData {
    label: string;
    onClick?: () => void;
}

defineProps<{
    label: string;
    items: NavDropdownItemData[];
}>();

const {
    showDropdown,
    handleTriggerEnter,
    handleTriggerLeave,
    handleDropdownEnter,
    handleDropdownLeave,
    setDropdownElement,
    setTriggerElement,
    cleanup,
} = useSafeTriangleHover(400); // delay nhỏ hơn một chút cho menu nav

const triggerRef = ref<HTMLElement | null>(null);
const dropdownRef = ref<HTMLElement | null>(null);

onMounted(() => {
    if (triggerRef.value) setTriggerElement(triggerRef.value);
    if (dropdownRef.value) setDropdownElement(dropdownRef.value);
});

onUnmounted(() => {
    cleanup();
});
</script>

<style scoped>
/* Animation riêng cho dropdown navigation (nhẹ nhàng, khác biệt với user dropdown) */
.nav-dropdown-enter-active,
.nav-dropdown-leave-active {
    transition: opacity 0.2s ease, transform 0.2s ease;
}

.nav-dropdown-enter-from,
.nav-dropdown-leave-to {
    opacity: 0;
    transform: translateY(-6px);
}
</style>