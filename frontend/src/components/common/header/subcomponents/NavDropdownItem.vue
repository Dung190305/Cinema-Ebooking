<template>
    <div class="relative inline-block">
        <a ref="triggerRef"
            class="flex items-center gap-2 hover:underline hover:text-accent text-text-primary text-body cursor-pointer select-none"
            @mouseenter="handleTriggerEnter" @mouseleave="handleTriggerLeave">
            {{ label }}
            <BaseIcon v-if="items.length" :icon="ChevronDown" :size="14" :scale="1.2" :stroke-width="1.5" />
        </a>

        <Transition name="nav-dropdown">
            <!--
                ✅ FIX 1: Dùng localShow (từ useSafeTriangleHover) thay vì isActive.
                useSafeTriangleHover được thiết kế để showDropdown là nguồn sự thật
                duy nhất — giống hệt AuthSection.vue. isActive chỉ dùng cho mutual
                exclusion (đóng dropdown khác), không dùng để render.
            -->
            <div v-if="localShow && items.length" ref="dropdownRef" @mouseenter="handleDropdownEnter"
                @mouseleave="handleDropdownLeave"
                class="absolute left-1/2 -translate-x-1/2 top-full mt-2 w-48 bg-bg-surface border border-border-default rounded-md shadow-md z-50">
                <div v-for="(item, index) in items" :key="index"
                    class="block px-4 py-2 text-sm text-text-primary hover:bg-accent hover:text-white cursor-pointer transition-colors"
                    @click="item.onClick?.()">
                    {{ item.label }}
                </div>
            </div>
        </Transition>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, getCurrentInstance, watch } from 'vue';
// ✅ FIX 2: Bỏ nextTick import — không cần nữa
import { ChevronDown } from 'lucide-vue-next';
import BaseIcon from '@/components/ui/icon/BaseIcon.vue';
import { useSafeTriangleHover } from '@/composables/useSafeTriangleHover';
import { useDropdownManager } from '@/composables/useDropdownManager';

export interface NavDropdownItemData {
    label: string;
    onClick?: () => void;
}

const props = defineProps<{
    label: string;
    items: NavDropdownItemData[];
}>();

const instance = getCurrentInstance();
const dropdownKey = instance?.uid ?? Symbol(props.label);

const { register } = useDropdownManager();
const { isActive, activate, deactivate } = register(dropdownKey);

const {
    showDropdown: localShow,
    handleTriggerEnter: localTriggerEnter,
    handleTriggerLeave: localTriggerLeave,
    handleDropdownEnter: localDropdownEnter,
    handleDropdownLeave: localDropdownLeave,
    setDropdownElement,
    setTriggerElement,
    cleanup,
    forceClose,
} = useSafeTriangleHover(400);

// ✅ FIX 3: Pass MouseEvent xuống composable để safe-triangle tính đúng vị trí chuột
function handleTriggerEnter(e: MouseEvent) {
    activate();
    localTriggerEnter(e);
}

function handleTriggerLeave(e: MouseEvent) {
    localTriggerLeave(e);
}

function handleDropdownEnter(e: MouseEvent) {
    localDropdownEnter(e);
}

function handleDropdownLeave(e: MouseEvent) {
    localDropdownLeave(e);
}

// Sync: khi localShow tắt → báo manager dropdown này đã đóng
watch(localShow, (newVal) => {
    if (!newVal && isActive.value) {
        deactivate();
    }
});

// Sync: khi dropdown khác được activate → manager set isActive=false → force close
watch(() => isActive.value, (newVal) => {
    if (!newVal && localShow.value) {
        forceClose();
    }
});

const triggerRef = ref<HTMLElement | null>(null);
const dropdownRef = ref<HTMLElement | null>(null);

// ✅ FIX 2: Bỏ await nextTick() — đăng ký element ngay khi DOM mount,
// không để thêm 1 tick race condition nữa
watch(dropdownRef, (el) => {
    setDropdownElement(el ?? null);
});

onMounted(() => {
    if (triggerRef.value) {
        setTriggerElement(triggerRef.value);
    }
});

onUnmounted(() => {
    cleanup();
    if (isActive.value) deactivate();
});
</script>

<style scoped>
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