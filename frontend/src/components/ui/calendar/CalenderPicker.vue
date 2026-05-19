<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick, onUnmounted } from 'vue'

const props = withDefaults(defineProps<{
    modelValue: Date | null | string
    mode?: 'date' | 'datetime'    // mặc định: 'date'
    variant?: 'web' | 'admin'     // mặc định: 'web'
    hasError?: boolean
    initialYear?: number          // mặc định: năm hiện tại
    minDate?: Date | null
    autoSelectToday?: boolean
    maxDate?: Date | null
}>(), {
    mode: 'date',
    variant: 'web',
    hasError: false,
    minDate: null,
    autoSelectToday: false,
    maxDate: null,
})

const emit = defineEmits<{
    'update:modelValue': [value: Date | null]
    'blur': []
}>()

const normalizedModelValue = computed(() => {
    if (!props.modelValue) return null
    const date = props.modelValue instanceof Date
        ? props.modelValue
        : new Date(String(props.modelValue))
    return isNaN(date.getTime()) ? null : date
})

const effectiveMaxDate = computed(() =>
    props.maxDate instanceof Date ? props.maxDate : null
)

const effectiveMinDate = computed(() => {
    // Nếu có truyền minDate thì dùng, ngược lại không giới hạn (null)
    return props.minDate instanceof Date ? props.minDate : null
})

// ─── Theme map ────────────────────────────────────────────────────────────────
const th = computed(() => {
    if (props.variant === 'admin') return {
        wrapper: 'bg-white border-border-admin-subtle',
        focusRing: 'border-accent ring-2 ring-slate-100',
        errorRing: 'border-red-300 ring-2 ring-red-100',
        input: 'text-text-admin-primary placeholder:text-text-admin-tertiary',
        iconBtn: 'text-text-admin-tertiary hover:text-text-admin-primary',
        dropdown: 'bg-white border-border-admin-subtle',
        navBtn: 'text-text-admin-tertiary hover:text-text-admin-primary hover:bg-slate-100',
        monthLabel: 'text-text-admin-primary',
        dayHeader: 'text-text-admin-tertiary',
        dayBtn: 'text-text-admin-primary hover:bg-slate-100 hover:text-accent',
        selectedDay: 'bg-accent text-white',
        todayDot: 'bg-accent',
        timeDivider: 'border-border-admin-subtle',
        timeLabel: 'text-text-admin-tertiary',
        timeInput: 'border-border-admin-subtle bg-white text-text-admin-primary focus:border-accent focus:ring-1 focus:ring-slate-100 outline-none',
        timeValue: 'text-text-admin-primary',
        timeBtn: 'text-text-admin-tertiary hover:text-text-admin-primary hover:bg-slate-100',
        confirmBtn: 'bg-accent text-white hover:bg-accent/90',
    }
    return {
        wrapper: 'bg-transparent border-border-subtle',
        focusRing: 'border-accent ring-2 ring-slate-100',
        errorRing: 'border-red-300 ring-2 ring-red-100',
        input: 'text-text-primary placeholder:text-text-secondary',
        iconBtn: 'text-text-secondary hover:text-text-primary',
        dropdown: 'bg-bg-surface border-border-subtle',
        navBtn: 'text-text-secondary hover:text-text-primary hover:bg-bg-muted',
        monthLabel: 'text-text-primary',
        dayHeader: 'text-text-secondary',
        dayBtn: 'text-text-primary hover:bg-accent hover:text-white',
        selectedDay: 'bg-accent text-white',
        todayDot: 'bg-accent',
        timeDivider: 'border-border-subtle',
        timeLabel: 'text-text-secondary',
        timeInput: 'border-border-subtle bg-transparent text-text-primary focus:border-accent focus:ring-1 focus:ring-slate-100 outline-none',
        timeValue: 'text-text-primary',
        timeBtn: 'text-text-secondary hover:text-text-primary hover:bg-bg-muted',
        confirmBtn: 'bg-accent text-white hover:bg-accent/90',
    }
})

// ─── State ────────────────────────────────────────────────────────────────────
const inputRef = ref<HTMLInputElement | null>(null)
const showCalendar = ref(false)
const calendarYear = ref(props.initialYear ?? new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth())
const inputValue = ref('')
const timeHour = ref(0)
const timeMinute = ref(0)

const prevYear = () => { calendarYear.value-- }
const nextYear = () => { calendarYear.value++ }

const MONTH_NAMES = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
    'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
const DAY_NAMES = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7']

const initToToday = () => {
    const now = new Date()
    calendarYear.value = now.getFullYear()
    calendarMonth.value = now.getMonth()

    if (props.mode === 'datetime') {
        timeHour.value = now.getHours()
        timeMinute.value = now.getMinutes()
    }
}

// Sync modelValue
watch(() => props.modelValue, (val) => {
    if (!val) {
        inputValue.value = ''
        return
    }

    const date = val instanceof Date ? val : new Date(String(val))
    if (isNaN(date.getTime())) {
        inputValue.value = ''
        return
    }

    const dd = String(date.getDate()).padStart(2, '0')
    const mm = String(date.getMonth() + 1).padStart(2, '0')
    const yyyy = date.getFullYear()
    timeHour.value = date.getHours()
    timeMinute.value = date.getMinutes()

    const hh = String(date.getHours()).padStart(2, '0')
    const min = String(date.getMinutes()).padStart(2, '0')

    inputValue.value = props.mode === 'datetime'
        ? `${dd}/${mm}/${yyyy} ${hh}:${min}`
        : `${dd}/${mm}/${yyyy}`
}, { immediate: true })

const containerRef = ref<HTMLElement | null>(null)

// Hàm đóng khi click bên ngoài
function handleClickOutside(event: MouseEvent) {
    if (!containerRef.value || !showCalendar.value) return
    // Nếu click vào phần tử bên trong container thì không làm gì
    if (containerRef.value.contains(event.target as Node)) return
    showCalendar.value = false
}

// Khởi tạo calendar
onMounted(() => {
    document.addEventListener('click', handleClickOutside)
    initToToday()
    if (props.autoSelectToday && !normalizedModelValue.value) {
        const todayDay = new Date().getDate()
        if (!isDayDisabled(todayDay)) {
            selectDay(todayDay)
        }
    }
})

onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
})

watch(showCalendar, async (isOpen) => {
    if (!isOpen) return
    await nextTick()

    if (!normalizedModelValue.value || normalizedModelValue.value < effectiveMinDate.value) {
        initToToday()

        if (props.autoSelectToday && !normalizedModelValue.value) {
            const todayDay = new Date().getDate()
            if (!isDayDisabled(todayDay)) {
                selectDay(todayDay)
            }
        }
    }
})
// ─── Kiểm tra ngày có bị disable không ─────────────────────────────────────
const isDayDisabled = (day: number | null): boolean => {
    if (!day) return true
    const dateToCheck = new Date(calendarYear.value, calendarMonth.value, day)

    if (effectiveMinDate.value) {
        const min = new Date(effectiveMinDate.value)
        min.setHours(0, 0, 0, 0)
        if (dateToCheck < min) return true
    }

    if (effectiveMaxDate.value) {
        const max = new Date(effectiveMaxDate.value)
        max.setHours(23, 59, 59, 999)
        if (dateToCheck > max) return true
    }

    return false
}

// ─── Kiểm tra giờ có bị disable không (chỉ áp dụng khi chọn ngày hôm nay) ──
const isTimeDisabled = computed(() => {
    if (props.mode !== 'datetime' || !normalizedModelValue.value || !effectiveMinDate.value) return false

    const selectedDate = normalizedModelValue.value
    const today = new Date(effectiveMinDate.value)

    return (
        selectedDate.getFullYear() === today.getFullYear() &&
        selectedDate.getMonth() === today.getMonth() &&
        selectedDate.getDate() === today.getDate()
    )
})

// ─── Auto-format khi gõ ──────────────────────────────────────────────────────
const handleInput = (e: Event) => {
    const raw = (e.target as HTMLInputElement).value

    if (props.mode === 'datetime') {
        // Tối đa 12 chữ số: DD MM YYYY HH mm
        const d = raw.replace(/\D/g, '').slice(0, 12)
        let f = d
        if (d.length > 2) f = d.slice(0, 2) + '/' + d.slice(2)
        if (d.length > 4) f = f.slice(0, 5) + '/' + d.slice(4)
        if (d.length > 8) f = f.slice(0, 10) + ' ' + d.slice(8)
        if (d.length > 10) f = f.slice(0, 13) + ':' + d.slice(10)
        inputValue.value = f

        if (d.length === 12) {
            const day = parseInt(d.slice(0, 2)), month = parseInt(d.slice(2, 4)) - 1
            const year = parseInt(d.slice(4, 8)), hour = parseInt(d.slice(8, 10))
            const min = parseInt(d.slice(10, 12))
            const date = new Date(year, month, day, hour, min)
            const valid = date.getFullYear() === year && date.getMonth() === month
                && date.getDate() === day && hour < 24 && min < 60
            if (valid) {
                timeHour.value = hour; timeMinute.value = min
                calendarYear.value = year; calendarMonth.value = month
                emit('update:modelValue', date)
            } else emit('update:modelValue', null)
        } else emit('update:modelValue', null)
        return
    }

    // date mode
    const d = raw.replace(/\D/g, '').slice(0, 8)
    let f = d
    if (d.length > 2) f = d.slice(0, 2) + '/' + d.slice(2)
    if (d.length > 4) f = f.slice(0, 5) + '/' + d.slice(4)
    inputValue.value = f

    if (d.length === 8) {
        const day = parseInt(d.slice(0, 2)), month = parseInt(d.slice(2, 4)) - 1
        const year = parseInt(d.slice(4, 8))
        const date = new Date(year, month, day)
        const valid = date.getFullYear() === year && date.getMonth() === month && date.getDate() === day
        if (valid) {
            calendarYear.value = year; calendarMonth.value = month
            emit('update:modelValue', date)
        } else emit('update:modelValue', null)
    } else emit('update:modelValue', null)
}

const handleKeydown = (e: KeyboardEvent) => {
    if (e.key !== 'Backspace') return
    const val = inputValue.value
    if (val.endsWith('/') || val.endsWith(' ') || val.endsWith(':')) {
        e.preventDefault()
        inputValue.value = val.slice(0, -1)
    }
}

// ─── Calendar ────────────────────────────────────────────────────────────────
const today = new Date()

const calendarDays = computed(() => {
    const first = new Date(calendarYear.value, calendarMonth.value, 1).getDay()
    const total = new Date(calendarYear.value, calendarMonth.value + 1, 0).getDate()
    const days: (number | null)[] = []
    for (let i = 0; i < first; i++) days.push(null)
    for (let d = 1; d <= total; d++) days.push(d)
    return days
})

const prevMonth = () => {
    if (calendarMonth.value === 0) { calendarMonth.value = 11; calendarYear.value-- }
    else calendarMonth.value--
}
const nextMonth = () => {
    if (calendarMonth.value === 11) { calendarMonth.value = 0; calendarYear.value++ }
    else calendarMonth.value++
}

const isSelectedDay = (day: number | null) => {
    if (!day || !normalizedModelValue.value) return false
    const d = normalizedModelValue.value
    return d.getFullYear() === calendarYear.value
        && d.getMonth() === calendarMonth.value
        && d.getDate() === day
}
const isTodayDay = (day: number | null) => {
    if (!day) return false
    return today.getFullYear() === calendarYear.value
        && today.getMonth() === calendarMonth.value
        && today.getDate() === day
}

const suppressBlur = ref(false)

const selectDay = (day: number | null) => {
    if (!day || isDayDisabled(day)) return

    const newDate = new Date(calendarYear.value, calendarMonth.value, day)

    // Xóa đoạn tự động đóng date mode trước đây
    // if (props.mode === 'date') { ... }  ← BỎ

    if (props.mode === 'datetime' && effectiveMinDate.value) {
        // Giới hạn giờ nếu chọn đúng ngày minDate
        const minDate = effectiveMinDate.value
        if (
            newDate.getFullYear() === minDate.getFullYear() &&
            newDate.getMonth() === minDate.getMonth() &&
            newDate.getDate() === minDate.getDate()
        ) {
            const minHour = minDate.getHours()
            const minMinute = minDate.getMinutes()

            if (timeHour.value < minHour) timeHour.value = minHour
            if (timeHour.value === minHour && timeMinute.value < minMinute) {
                timeMinute.value = minMinute
            }
        }
    }

    // Luôn emit giá trị mới khi chọn ngày (người dùng thấy ngay trên input)
    emit('update:modelValue', new Date(calendarYear.value, calendarMonth.value, day, timeHour.value, timeMinute.value))
    // Không đóng dropdown
}

// ─── Time helpers ─────────────────────────────────────────────────────────────
const buildDateWithTime = () => {
    const base = normalizedModelValue.value
        ? new Date(normalizedModelValue.value)
        : new Date(calendarYear.value, calendarMonth.value, 1)
    base.setHours(timeHour.value, timeMinute.value, 0, 0)
    return base
}

const emitWithCurrentTime = () => {
    // Nếu chưa có ngày nào được chọn, không emit gì hết
    if (!normalizedModelValue.value) return

    const day = normalizedModelValue.value.getDate()
    emit(
        'update:modelValue',
        new Date(calendarYear.value, calendarMonth.value, day, timeHour.value, timeMinute.value)
    )
}

const onHourChange = (e: Event) => {
    let val = parseInt((e.target as HTMLInputElement).value)
    if (isNaN(val)) val = 0

    if (isTimeDisabled.value) {
        const minHour = effectiveMinDate.value.getHours()
        val = Math.max(minHour, val)
    }

    timeHour.value = Math.max(0, Math.min(23, val))
    emitWithCurrentTime()
}

const onMinuteChange = (e: Event) => {
    let val = parseInt((e.target as HTMLInputElement).value)
    if (isNaN(val)) val = 0

    if (isTimeDisabled.value) {
        const minHour = effectiveMinDate.value.getHours()
        const minMinute = effectiveMinDate.value.getMinutes()

        if (timeHour.value === minHour) {
            val = Math.max(minMinute, val)
        }
    }

    timeMinute.value = Math.max(0, Math.min(59, val))
    emitWithCurrentTime()
}

const onHourWheel = (e: WheelEvent) => {
    e.preventDefault()
    adjustHour(e.deltaY < 0 ? 1 : -1)
}

const onMinuteWheel = (e: WheelEvent) => {
    e.preventDefault()
    adjustMinute(e.deltaY < 0 ? 5 : -5)
}

const adjustHour = (delta: number) => { timeHour.value = (timeHour.value + delta + 24) % 24; emitWithCurrentTime() }
const adjustMinute = (delta: number) => { timeMinute.value = (timeMinute.value + delta + 60) % 60; emitWithCurrentTime() }
const confirmTime = () => {
    emitWithCurrentTime()
    suppressBlur.value = true
    showCalendar.value = false
    nextTick(() => {
        inputRef.value?.focus()
        suppressBlur.value = false
    })
}

const onFocusOut = (e: FocusEvent) => {
    if (suppressBlur.value) return  // <-- chặn blur nội bộ

    const target = e.relatedTarget as HTMLElement | null
    if (target && (e.currentTarget as HTMLElement).contains(target)) return
    emit('blur')
}
const clearValue = () => {
    emit('update:modelValue', null)
    showCalendar.value = false
}

const confirmSelection = () => {
    if (props.mode === 'datetime' && normalizedModelValue.value) {
        emitWithCurrentTime() // đảm bảo emit giờ/phút mới nhất
    }
    // Nếu date mode, không cần emit thêm vì đã emit khi chọn ngày
    showCalendar.value = false
}
</script>

<template>

    <div ref="containerRef" class="relative" @focusout="onFocusOut">
        <!-- Input chính -->
        <div :class="[
            'w-full flex items-center border rounded-md transition-all overflow-hidden',
            showCalendar ? th.focusRing : hasError ? th.errorRing : th.wrapper
        ]">
            <input ref="inputRef" type="text" inputmode="numeric"
                :placeholder="mode === 'datetime' ? 'DD/MM/YYYY HH:mm' : 'DD/MM/YYYY'" :value="inputValue"
                :class="['flex-1 px-3 py-2 text-sm bg-transparent outline-none', th.input]" @input="handleInput"
                @keydown="handleKeydown" />
            <button type="button" :class="['px-2 py-2 transition-colors', th.iconBtn]"
                @click="showCalendar = !showCalendar">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24"
                    stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
            </button>
        </div>

        <!-- Dropdown lịch & giờ -->
        <div v-if="showCalendar"
            :class="['absolute top-full left-0 z-70 mt-1 border rounded-lg shadow-xl p-3', mode === 'datetime' ? 'min-w-95' : 'min-w-62.5', th.dropdown]">

            <!-- Container dạng flex row khi datetime, block khi date -->
            <div :class="mode === 'datetime' ? 'flex gap-3' : ''">
                <!-- Phần lịch (luôn hiển thị) -->
                <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between mb-2">
                        <div class="flex items-center gap-0.5">
                            <!-- Nút năm trước -->
                            <button type="button"
                                :class="['w-6 h-6 flex items-center justify-center rounded transition-colors', th.navBtn]"
                                @click.stop="prevYear" title="Năm trước">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
                                </svg>
                            </button>
                            <!-- Nút tháng trước (giữ nguyên) -->
                            <button type="button"
                                :class="['w-6 h-6 flex items-center justify-center rounded transition-colors', th.navBtn]"
                                @click.stop="prevMonth">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5"
                                        d="M15 19l-7-7 7-7" />
                                </svg>
                            </button>
                        </div>

                        <span :class="['text-[13px] font-medium', th.monthLabel]">
                            {{ MONTH_NAMES[calendarMonth] }} {{ calendarYear }}
                        </span>

                        <div class="flex items-center gap-0.5">
                            <!-- Nút tháng sau (giữ nguyên) -->
                            <button type="button"
                                :class="['w-6 h-6 flex items-center justify-center rounded transition-colors', th.navBtn]"
                                @click.stop="nextMonth">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5"
                                        d="M9 5l7 7-7 7" />
                                </svg>
                            </button>
                            <!-- Nút năm sau -->
                            <button type="button"
                                :class="['w-6 h-6 flex items-center justify-center rounded transition-colors', th.navBtn]"
                                @click.stop="nextYear" title="Năm sau">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M13 5l7 7-7 7m-8-14l7 7-7 7" />
                                </svg>
                            </button>
                        </div>
                    </div>

                    <!-- Header thứ -->
                    <div class="grid grid-cols-7 mb-1">
                        <span v-for="day in DAY_NAMES" :key="day"
                            :class="['text-center text-[10px] font-medium py-1', th.dayHeader]">{{ day }}</span>
                    </div>

                    <!-- Lưới ngày -->
                    <div class="grid grid-cols-7 gap-y-0.5">
                        <button v-for="(day, idx) in calendarDays" :key="idx" type="button"
                            :disabled="!day || isDayDisabled(day)" @click.stop="selectDay(day)" :class="[
                                'relative h-7 w-full rounded text-[12px] transition-colors',
                                !day || isDayDisabled(day)
                                    ? 'text-slate-300 cursor-not-allowed'
                                    : isSelectedDay(day)
                                        ? [th.selectedDay, 'font-medium']
                                        : [th.dayBtn, 'cursor-pointer']
                            ]">
                            {{ day || '' }}
                            <span v-if="isTodayDay(day) && !isSelectedDay(day)"
                                :class="['absolute bottom-0.5 left-1/2 -translate-x-1/2 w-1 h-1 rounded-full', th.todayDot]" />
                        </button>
                    </div>
                </div>

                <!-- Phần chọn giờ (chỉ có ở datetime) – nằm bên phải với đường kẻ dọc -->
                <template v-if="mode === 'datetime'">
                    <div :class="['border-l', th.timeDivider]" />
                    <div class="flex flex-col items-center justify-center min-w-22.5">
                        <div class="flex items-center gap-1">
                            <!-- Hour -->
                            <div class="flex flex-col items-center gap-1">
                                <button type="button"
                                    :class="['w-8 h-6 flex items-center justify-center rounded text-xs transition-colors', th.timeBtn]"
                                    @click.stop="adjustHour(1)">▲</button>
                                <input type="number" min="0" max="23" :value="String(timeHour).padStart(2, '0')"
                                    :class="['w-14 text-center text-base font-mono font-semibold tabular-nums border rounded px-1 py-2 transition-colors appearance-none [-moz-appearance:textfield]', th.timeInput, th.timeValue]"
                                    @change="onHourChange" @wheel="onHourWheel" />
                                <button type="button"
                                    :class="['w-8 h-6 flex items-center justify-center rounded text-xs transition-colors', th.timeBtn]"
                                    @click.stop="adjustHour(-1)">▼</button>
                            </div>

                            <span :class="['text-lg font-bold', th.timeValue]">:</span>

                            <!-- Minute -->
                            <div class="flex flex-col items-center gap-1">
                                <button type="button"
                                    :class="['w-8 h-6 flex items-center justify-center rounded text-xs transition-colors', th.timeBtn]"
                                    @click.stop="adjustMinute(5)">▲</button>
                                <input type="number" min="0" max="59" :value="String(timeMinute).padStart(2, '0')"
                                    :class="['w-14 text-center text-base font-mono font-semibold tabular-nums border rounded px-1 py-2 transition-colors appearance-none [-moz-appearance:textfield]', th.timeInput, th.timeValue]"
                                    @change="onMinuteChange" @wheel="onMinuteWheel" />
                                <button type="button"
                                    :class="['w-8 h-6 flex items-center justify-center rounded text-xs transition-colors', th.timeBtn]"
                                    @click.stop="adjustMinute(-5)">▼</button>
                            </div>
                        </div>
                        <p :class="['text-center text-[10px] mt-2', th.timeLabel]">
                            Nhập · cuộn · ±5 phút
                        </p>
                    </div>
                </template>
            </div>
            <!-- Footer -->
            <div :class="['flex justify-between items-center mt-3 pt-3 border-t', th.timeDivider]">
                <button type="button"
                    :class="['px-3 py-1.5 text-xs rounded transition-colors', th.dayBtn, 'hover:bg-red-50 hover:text-red-500']"
                    @click.stop="clearValue">
                    Xóa
                </button>
                <button type="button"
                    :class="['px-4 py-1.5 text-xs font-medium rounded transition-colors', th.selectedDay]"
                    @click.stop="confirmSelection">
                    Xác nhận
                </button>
            </div>
        </div>
    </div>
</template>