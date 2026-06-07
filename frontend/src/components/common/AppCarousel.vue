<template>
    <div ref="wrapperRef" class="relative w-full" @mouseenter="pauseAuto" @mouseleave="resumeAuto">
        <div class="relative w-full overflow-hidden">
            <div ref="trackRef" class="flex will-change-transform" :class="{
                'transition-transform duration-300 ease-in-out': isAnimating && transitionEnabled,
            }" :style="{
                transform: `translateX(${offset}px)`,
                gap: `${gap}px`,
            }" @transitionend="onTransitionEnd">
                <div v-for="slide in slides" :key="slide.virtualIndex" class="shrink-0"
                    :style="{ width: slideWidth + 'px' }">
                    <slot :item="slide.item" :index="slide.realIndex" />
                </div>
            </div>
        </div>

        <div v-if="dots && items.length > 1" class="absolute bottom-6 left-1/2 flex -translate-x-1/2 gap-2">
            <button v-for="(item, idx) in items" :key="idx" type="button"
                class="h-2.5 w-2.5 rounded-full transition-all duration-300 ease-out focus:outline-none will-change-transform"
                :class="idx === activeDotIndex
                    ? 'bg-white border-transparent scale-110 shadow-md'
                    : 'border border-white bg-transparent hover:bg-white/80'
                    " :aria-label="`Đi tới slide ${idx + 1}`" @click="goToRealIndex(idx)" />
        </div>

        <!-- Prev Button -->
        <button v-if="(infinite ? items.length > 1 : items.length > slidesPerView)"
            class="absolute top-1/2 z-10 flex items-center justify-center rounded-full transition disabled:pointer-events-none disabled:opacity-40"
            :class="[
                simpleNav
                    ? 'p-3 bg-transparent shadow-none'    // đơn giản
                    : edgeButtons
                        ? 'h-16 w-16 shadow-lg bg-overlay-light-30 hover:bg-overlay-light-50'
                        : 'h-16 w-16 shadow-sm bg-overlay-dark-10 hover:bg-overlay-dark-30',
                navButtonClass,
            ]" :style="prevBtnStyle" :disabled="isAnimating || (!infinite && currentIndex <= 0)" @click="prev"
            aria-label="Previous">
            <span :style="edgeButtons ? { transform: 'translateX(30%)', display: 'inline-flex' } : {}"
                class="text-text-secondary hover:text-text-primary">
                <BaseIcon :icon="prevIcon || ChevronLeft" :size="simpleNav ? 28 : 32" />
            </span>
        </button>

        <!-- Next Button -->
        <button v-if="(infinite ? items.length > 1 : items.length > slidesPerView)"
            class="absolute top-1/2 z-10 flex items-center justify-center rounded-full transition disabled:pointer-events-none disabled:opacity-40"
            :class="[
                simpleNav
                    ? 'p-3 bg-transparent shadow-none'
                    : edgeButtons
                        ? 'h-16 w-16 shadow-lg bg-overlay-light-30 hover:bg-overlay-light-50'
                        : 'h-16 w-16 shadow-sm bg-overlay-dark-10 hover:bg-overlay-dark-30',
                navButtonClass,
            ]" :style="nextBtnStyle" :disabled="isAnimating || (!infinite && currentIndex <= 0)" @click="next"
            aria-label="Next">
            <span :style="edgeButtons ? { transform: 'translateX(-30%)', display: 'inline-flex' } : {}"
                class="text-text-secondary hover:text-text-primary">
                <BaseIcon :icon="nextIcon || ChevronRight" :size="simpleNav ? 28 : 32" />
            </span>
        </button>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

const props = withDefaults(
    defineProps<{
        items: any[]
        peek?: number
        peekPercent?: number
        gap?: number
        autoplay?: boolean
        interval?: number
        edgeButtons?: boolean
        navInset?: number
        prevIcon?: any
        nextIcon?: any
        navButtonClass?: string
        slideWidthFixed?: number
        infinite?: boolean
        slidesPerView?: number
        paddingLeft?: number
        paddingRight?: number
        dots?: boolean               // mới: bật/tắt dot indicators
        dotsClass?: string
        simpleNav?: boolean
    }>(),
    {
        peek: 64,
        gap: 48,
        autoplay: false,
        interval: 3000,
        edgeButtons: false,
        navInset: 0,
        navButtonClass: '',
        slideWidthFixed: undefined,
        infinite: true,
        slidesPerView: 1,
        paddingLeft: 0,
        paddingRight: 0,
        dots: false,
        dotsClass: '',
        simpleNav: false,
    }
)

const emit = defineEmits<{ change: [index: number] }>()

const wrapperRef = ref<HTMLElement | null>(null)
const trackRef = ref<HTMLElement | null>(null)
const containerWidth = ref(0)
const isAnimating = ref(false)
const transitionEnabled = ref(true)
let autoTimer: ReturnType<typeof setInterval> | null = null
let resizeObserver: ResizeObserver | null = null

const actualPeek = computed(() => {
    if (props.peekPercent !== undefined) {
        return (containerWidth.value * props.peekPercent) / 100
    }
    return props.peek
})

// ----- Danh sách slide -----
const slides = computed(() => {
    const list = props.items
    if (!list.length) return []
    if (props.infinite && list.length > 1) {
        const last = list[list.length - 1]
        const first = list[0]
        return [
            { item: last, virtualIndex: 0, realIndex: list.length - 1 },
            ...list.map((item, i) => ({ item, virtualIndex: i + 1, realIndex: i })),
            { item: first, virtualIndex: list.length + 1, realIndex: 0 },
        ]
    }
    return list.map((item, i) => ({ item, virtualIndex: i, realIndex: i }))
})

// ----- Kích thước slide -----
const slideWidth = computed(() => {
    if (props.slideWidthFixed) return props.slideWidthFixed
    if (props.slidesPerView > 1) {
        const totalGap = (props.slidesPerView - 1) * props.gap
        const available = props.infinite
            ? containerWidth.value - 2 * actualPeek.value
            : containerWidth.value - props.paddingLeft - props.paddingRight
        return Math.max(0, (available - totalGap) / props.slidesPerView)
    }
    // slidesPerView = 1
    return Math.max(0, containerWidth.value - 2 * actualPeek.value)
})

const step = computed(() => slideWidth.value + props.gap)

// ----- Chỉ số hiện tại -----
const currentIndex = ref(props.items.length > 1 ? (props.infinite ? 1 : 0) : 0)

const maxIndex = computed(() => {
    if (props.infinite || props.items.length === 0) return 0
    return Math.max(0, props.items.length - props.slidesPerView)
})

// Computed index của slide thực đang active (0-based)
const activeDotIndex = computed(() => {
    const slide = slides.value[currentIndex.value]
    return slide ? slide.realIndex : 0
})

// Điều hướng đến slide thực có index cho trước
function goToRealIndex(realIdx: number) {
    if (isAnimating.value) return
    if (!props.infinite) {
        goTo(realIdx, true)
        return
    }
    // Với infinite: ánh xạ real index -> virtual index (real items bắt đầu từ virtualIndex 1)
    const virtualIdx = realIdx + 1
    // Nếu realIdx là phần tử cuối, virtualIdx vẫn là realIdx + 1 (sẽ nằm trước clone đầu)
    // Nếu realIdx là 0, virtualIdx là 1 (đúng)
    goTo(virtualIdx, true)
}

// ----- Vị trí dịch chuyển -----
const offset = computed(() => {
    const base = props.infinite ? actualPeek.value : props.paddingLeft
    return base - currentIndex.value * step.value
})

// ----- Style nút điều hướng -----
const prevBtnStyle = computed(() => {
    if (props.edgeButtons) {
        return { left: `${props.navInset}px`, transform: 'translate(-50%, -50%)' }
    }
    const refPeek = props.infinite ? actualPeek.value : props.paddingLeft
    return { left: `${refPeek / 2 + props.navInset}px`, transform: 'translate(-50%, -50%)' }
})

const nextBtnStyle = computed(() => {
    if (props.edgeButtons) {
        return { right: `${props.navInset}px`, transform: 'translate(50%, -50%)' }
    }
    const refPeek = props.infinite ? actualPeek.value : props.paddingRight
    return { right: `${refPeek / 2 + props.navInset}px`, transform: 'translate(50%, -50%)' }
})

// ----- Điều hướng -----
function goTo(index: number, animate = true) {
    if (props.items.length === 0) return
    transitionEnabled.value = animate
    currentIndex.value = index
    if (!animate) {
        nextTick(() => {
            transitionEnabled.value = true
        })
    }
}

function next() {
    if (isAnimating.value) return
    if (props.infinite) {
        if (props.items.length <= 1) return
        isAnimating.value = true
        const maxVirtual = props.items.length + 1
        if (currentIndex.value === maxVirtual) {
            goTo(1, true)
        } else {
            goTo(currentIndex.value + 1, true)
        }
    } else {
        if (currentIndex.value >= maxIndex.value) {
            stopAuto()
            return
        }
        isAnimating.value = true
        goTo(currentIndex.value + 1, true)
    }
}

function prev() {
    if (isAnimating.value) return
    if (props.infinite) {
        if (props.items.length <= 1) return
        isAnimating.value = true
        if (currentIndex.value === 0) {
            goTo(props.items.length, true)
        } else {
            goTo(currentIndex.value - 1, true)
        }
    } else {
        if (currentIndex.value <= 0) return
        isAnimating.value = true
        goTo(currentIndex.value - 1, true)
    }
}

function onTransitionEnd() {
    if (props.items.length === 0) {
        isAnimating.value = false
        return
    }

    if (props.infinite && props.items.length > 1) {
        const maxVirtual = props.items.length + 1
        if (currentIndex.value === maxVirtual) {
            goTo(1, false)
        } else if (currentIndex.value === 0) {
            goTo(props.items.length, false)
        }
    }

    isAnimating.value = false

    const realIndex = slides.value[currentIndex.value]?.realIndex
    if (realIndex !== undefined) emit('change', realIndex)
}

// ----- Autoplay -----
function startAuto() {
    if (!props.autoplay) return
    if (props.items.length <= 1) return
    if (!props.infinite && currentIndex.value >= maxIndex.value) return
    stopAuto()
    autoTimer = setInterval(() => {
        next()
    }, props.interval)
}

function stopAuto() {
    if (autoTimer) {
        clearInterval(autoTimer)
        autoTimer = null
    }
}

function pauseAuto() { stopAuto() }
function resumeAuto() {
    if (props.autoplay) startAuto()
}

// ----- Resize -----
onMounted(() => {
    if (wrapperRef.value) {
        resizeObserver = new ResizeObserver((entries) => {
            for (const entry of entries) {
                containerWidth.value = entry.contentRect.width
            }
        })
        resizeObserver.observe(wrapperRef.value)
    }
    if (props.autoplay) startAuto()
})

onBeforeUnmount(() => {
    stopAuto()
    if (resizeObserver) resizeObserver.disconnect()
})

watch(() => props.items, () => {
    if (props.infinite && props.items.length > 1) {
        currentIndex.value = 1
    } else {
        currentIndex.value = 0
    }
    stopAuto()
    if (props.autoplay) startAuto()
})
</script>