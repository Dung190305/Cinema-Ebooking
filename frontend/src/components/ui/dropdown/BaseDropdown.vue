<!-- src/components/ui/dropdown/BaseDropdown.vue -->
<template>
    <div ref="containerRef" class="relative" :class="fullWidth ? 'block w-full' : 'inline-block'">
        <!-- Trigger -->
        <div ref="triggerRef" class="flex items-center gap-2 cursor-pointer select-none"
            :class="[triggerClass, localShow ? activeTriggerClass : '']"
            @mouseenter="triggerType === 'hover' && handleTriggerEnter($event)"
            @mouseleave="triggerType === 'hover' && handleTriggerLeave($event)"
            @click="triggerType === 'click' && toggleClick()">
            <slot name="trigger" :isOpen="localShow" :close="closeDropdown">
                <span class="text-body" :class="labelClass">
                    {{ label }}
                </span>
                <BaseIcon v-if="items.length && showChevron" :icon="ChevronDown" :size="14" :scale="1.2"
                    :stroke-width="1.5" class="transition-transform" :class="{ 'rotate-180': localShow }" />
            </slot>
        </div>

        <!-- Dropdown Panel -->
        <Transition name="dropdown-fade">
            <div v-if="localShow && items.length" ref="dropdownRef"
                @mouseenter="triggerType === 'hover' && handleDropdownEnter($event)"
                @mouseleave="triggerType === 'hover' && handleDropdownLeave($event)" :class="[
                    'absolute z-50 mt-2 rounded-md border border-border-default bg-bg-surface shadow-lg',
                    fullWidth ? 'w-full left-0' : dropdownWidthClass + ' ' + dropdownPositionClass,
                ]">
                <slot name="dropdown" :close="closeDropdown">
                    <div v-for="(item, index) in items" :key="index"
                        class="block px-4 py-2 text-sm text-text-primary hover:bg-accent hover:text-white cursor-pointer transition-colors first:rounded-t-md last:rounded-b-md"
                        @click="handleItemClick(item)">
                        {{ item.label }}
                    </div>
                </slot>
            </div>
        </Transition>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, getCurrentInstance, computed } from 'vue'
import { ChevronDown } from 'lucide-vue-next'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { useSafeTriangleHover } from '@/composables/useSafeTriangleHover'
import { useDropdownManager } from '@/composables/useDropdownManager'

export interface DropdownItem {
    label: string
    onClick?: () => void
}

const props = withDefaults(
    defineProps<{
        label?: string
        items?: DropdownItem[]
        triggerClass?: string
        labelClass?: string
        dropdownWidthClass?: string
        dropdownPositionClass?: string
        showChevron?: boolean
        triggerType?: 'hover' | 'click'
        fullWidth?: boolean
        activeTriggerClass?: string
    }>(),
    {
        label: '',
        items: () => [],
        triggerClass: 'hover:underline hover:text-accent text-text-primary',
        labelClass: '',
        dropdownWidthClass: 'w-48',
        dropdownPositionClass: 'left-1/2 -translate-x-1/2',
        showChevron: true,
        triggerType: 'hover',
        fullWidth: true,
        activeTriggerClass: 'ring-2 ring-accent'
    }
)

// ── Hover mode (giữ nguyên cơ chế cũ) ──
const instance = getCurrentInstance()
const dropdownKey = instance?.uid ?? Symbol('dropdown')

const { register } = useDropdownManager()
const { isActive, activate, deactivate } = register(dropdownKey)

const {
    showDropdown: hoverShow,
    handleTriggerEnter: localTriggerEnter,
    handleTriggerLeave: localTriggerLeave,
    handleDropdownEnter: localDropdownEnter,
    handleDropdownLeave: localDropdownLeave,
    setDropdownElement,
    setTriggerElement,
    cleanup,
    forceClose: hoverForceClose,
} = useSafeTriangleHover(400)

// Wrapper để truyền MouseEvent (hover)
function handleTriggerEnter(e: MouseEvent) {
    activate()
    localTriggerEnter(e)
}
function handleTriggerLeave(e: MouseEvent) {
    localTriggerLeave(e)
}
function handleDropdownEnter(e: MouseEvent) {
    localDropdownEnter(e)
}
function handleDropdownLeave(e: MouseEvent) {
    localDropdownLeave(e)
}

// ── Click mode ──
const clickShow = ref(false)
function toggleClick() {
    clickShow.value = !clickShow.value
}
function handleClickOutside(event: MouseEvent) {
    const container = containerRef.value
    if (container && !container.contains(event.target as Node)) {
        clickShow.value = false
    }
}

// ── Trạng thái hiển thị chung (dựa theo triggerType) ──
const localShow = computed(() =>
    props.triggerType === 'click' ? clickShow.value : hoverShow.value
)

// Hàm đóng dùng chung (có thể gọi từ slot hoặc item click)
function closeDropdown() {
    if (props.triggerType === 'hover') {
        hoverForceClose()
    } else {
        clickShow.value = false
    }
}

// Khi click vào 1 item → gọi onClick của item (nếu có) và đóng dropdown
function handleItemClick(item: DropdownItem) {
    item.onClick?.()
    closeDropdown()
}

// Đồng bộ hover ↔ manager (chỉ cần cho chế độ hover)
watch(hoverShow, (val) => {
    if (!val && isActive.value) deactivate()
})
watch(() => isActive.value, (val) => {
    if (!val && hoverShow.value) hoverForceClose()
})

// Đăng ký / hủy click outside cho chế độ click
onMounted(() => {
    if (props.triggerType === 'click') {
        document.addEventListener('click', handleClickOutside, true)
    }
})
onUnmounted(() => {
    if (props.triggerType === 'click') {
        document.removeEventListener('click', handleClickOutside, true)
    }
})

// Đăng ký phần tử trigger/dropdown cho safe triangle (chỉ hover)
const triggerRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)
const containerRef = ref<HTMLElement | null>(null) // dùng cho click outside

onMounted(() => {
    if (triggerRef.value && props.triggerType === 'hover') {
        setTriggerElement(triggerRef.value)
    }
})
watch(dropdownRef, (el) => {
    if (props.triggerType === 'hover') {
        setDropdownElement(el ?? null)
    }
})
onUnmounted(() => {
    if (props.triggerType === 'hover') {
        cleanup()
        if (isActive.value) deactivate()
    }
})
</script>

<style scoped>
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
    transition: opacity 0.2s ease, transform 0.2s ease;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
    opacity: 0;
    transform: translateY(-6px);
}
</style>