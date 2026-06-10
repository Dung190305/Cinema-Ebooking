<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { createLocalDate, parseDateSafe } from '@/utils/dateFormat'

const props = withDefaults(defineProps<{
    modelValue: Date | null | string
    mode?: 'date' | 'datetime'
    variant?: 'web' | 'admin'
    hasError?: boolean
    initialYear?: number
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
    const date = parseDateSafe(props.modelValue)
    return date ? new Date(date.getTime()) : null
})

const effectiveMaxDate = computed(() => {
    return props.maxDate instanceof Date ? new Date(props.maxDate.getTime()) : null
})

const effectiveMinDate = computed(() => {
    if (!props.minDate) return null
    const min = parseDateSafe(props.minDate)
    if (!min) return null

    const zonedDate = createLocalDate(
        min.getFullYear(),
        min.getMonth(),
        min.getDate()
    )
    zonedDate.setHours(0, 0, 0, 0)
    return zonedDate
})

// ─── Theme ────────────────────────────────────────────────────────────────
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
        wrapper: 'bg-transparent border-border-default',
        focusRing: 'border-accent ring-2 ring-slate-100',
        errorRing: 'border-red-300 ring-2 ring-red-100',
        input: 'text-text-primary placeholder:text-text-secondary',
        iconBtn: 'text-text-secondary hover:text-text-primary',
        dropdown: 'bg-bg-surface border-border-default',
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

const MONTH_NAMES = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
    'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
const DAY_NAMES = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7']

watch(() => showCalendar.value, (isOpen) => {
    if (isOpen && normalizedModelValue.value) {
        const date = normalizedModelValue.value
        calendarYear.value = date.getFullYear()
        calendarMonth.value = date.getMonth()
    }
})

const initToToday = () => {
    const vnNow = new Date(
        new Date().toLocaleString('en-US', { timeZone: 'Asia/Ho_Chi_Minh' })
    )

    calendarYear.value = vnNow.getFullYear()
    calendarMonth.value = vnNow.getMonth()

    if (props.mode === 'datetime') {
        timeHour.value = vnNow.getHours()
        timeMinute.value = vnNow.getMinutes()
    }
}

watch(() => props.modelValue, (val) => {
    const date = parseDateSafe(val)
    if (!date) {
        inputValue.value = ''
        return
    }

    const dd = String(date.getDate()).padStart(2, '0')
    const mm = String(date.getMonth() + 1).padStart(2, '0')
    const yyyy = date.getFullYear()

    timeHour.value = date.getHours()
    timeMinute.value = date.getMinutes()

    inputValue.value = props.mode === 'datetime'
        ? `${dd}/${mm}/${yyyy} ${String(timeHour.value).padStart(2, '0')}:${String(timeMinute.value).padStart(2, '0')}`
        : `${dd}/${mm}/${yyyy}`
}, { immediate: true })

const containerRef = ref<HTMLElement | null>(null)

function handleClickOutside(event: MouseEvent) {
    if (!containerRef.value || !showCalendar.value) return
    if (containerRef.value.contains(event.target as Node)) return
    showCalendar.value = false
}

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

// ─── Kiểm tra ngày disabled ─────────────────────────────────────
const isDayDisabled = (day: number | null): boolean => {
    if (!day) return true

    const dateToCheck = createLocalDate(calendarYear.value, calendarMonth.value, day)
    dateToCheck.setHours(0, 0, 0, 0)

    // So sánh minDate
    if (effectiveMinDate.value) {
        const minCheck = new Date(effectiveMinDate.value)
        minCheck.setHours(0, 0, 0, 0)
        if (dateToCheck < minCheck) return true
    }

    // So sánh maxDate
    if (effectiveMaxDate.value) {
        const maxCheck = new Date(effectiveMaxDate.value)
        maxCheck.setHours(23, 59, 59, 999)
        if (dateToCheck > maxCheck) return true
    }

    return false
}

const isTimeDisabled = computed(() => {
    if (props.mode !== 'datetime' || !normalizedModelValue.value || !effectiveMinDate.value) return false

    const selected = normalizedModelValue.value
    const min = effectiveMinDate.value

    return (
        selected.getFullYear() === min.getFullYear() &&
        selected.getMonth() === min.getMonth() &&
        selected.getDate() === min.getDate()
    )
})

// ─── Handle Input ──────────────────────────────────────────────────────
const handleInput = (e: Event) => {
    const raw = (e.target as HTMLInputElement).value

    if (props.mode === 'datetime') {
        const d = raw.replace(/\D/g, '').slice(0, 12)
        let f = d
        if (d.length > 2) f = d.slice(0, 2) + '/' + d.slice(2)
        if (d.length > 4) f = f.slice(0, 5) + '/' + d.slice(4)
        if (d.length > 8) f = f.slice(0, 10) + ' ' + d.slice(8)
        if (d.length > 10) f = f.slice(0, 13) + ':' + d.slice(10)
        inputValue.value = f

        if (d.length === 12) {
            const day = parseInt(d.slice(0, 2))
            const month = parseInt(d.slice(2, 4)) - 1
            const year = parseInt(d.slice(4, 8))
            const hour = parseInt(d.slice(8, 10))
            const min = parseInt(d.slice(10, 12))

            const date = createLocalDate(year, month, day, hour, min)
            const valid = date.getFullYear() === year && date.getMonth() === month && date.getDate() === day

            if (valid) {
                timeHour.value = hour
                timeMinute.value = min
                calendarYear.value = year
                calendarMonth.value = month
                emit('update:modelValue', date)
            } else {
                emit('update:modelValue', null)
            }
        } else {
            emit('update:modelValue', null)
        }
        return
    }

    // Date only mode
    const d = raw.replace(/\D/g, '').slice(0, 8)
    let f = d
    if (d.length > 2) f = d.slice(0, 2) + '/' + d.slice(2)
    if (d.length > 4) f = f.slice(0, 5) + '/' + d.slice(4)
    inputValue.value = f

    if (d.length === 8) {
        const day = parseInt(d.slice(0, 2))
        const month = parseInt(d.slice(2, 4)) - 1
        const year = parseInt(d.slice(4, 8))

        const date = createLocalDate(year, month, day)
        const valid = date.getFullYear() === year && date.getMonth() === month && date.getDate() === day

        if (valid) {
            calendarYear.value = year
            calendarMonth.value = month
            emit('update:modelValue', date)
        } else {
            emit('update:modelValue', null)
        }
    } else {
        emit('update:modelValue', null)
    }
}

const handleKeydown = (e: KeyboardEvent) => {
    if (e.key !== 'Backspace') return
    const val = inputValue.value
    if (val.endsWith('/') || val.endsWith(' ') || val.endsWith(':')) {
        e.preventDefault()
        inputValue.value = val.slice(0, -1)
    }
}

// ─── Calendar Logic ─────────────────────────────────────────────────────
const today = computed(() => {
    const now = new Date()
    // Lấy ngày theo múi giờ Việt Nam để tránh lệch ngày
    return new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Ho_Chi_Minh' }))
})

const calendarDays = computed(() => {
    const first = new Date(calendarYear.value, calendarMonth.value, 1).getDay()
    const total = new Date(calendarYear.value, calendarMonth.value + 1, 0).getDate()
    const days: (number | null)[] = []
    for (let i = 0; i < first; i++) days.push(null)
    for (let d = 1; d <= total; d++) days.push(d)
    return days
})

const prevMonth = () => {
    if (calendarMonth.value === 0) {
        calendarMonth.value = 11
        calendarYear.value--
    } else calendarMonth.value--
}

const nextMonth = () => {
    if (calendarMonth.value === 11) {
        calendarMonth.value = 0
        calendarYear.value++
    } else calendarMonth.value++
}

const prevYear = () => {
    calendarYear.value--
}

const nextYear = () => {
    calendarYear.value++
}

const isSelectedDay = (day: number | null) => {
    if (!day || !normalizedModelValue.value) return false
    const d = normalizedModelValue.value
    return d.getFullYear() === calendarYear.value &&
        d.getMonth() === calendarMonth.value &&
        d.getDate() === day
}

const isTodayDay = (day: number | null) => {
    if (!day) return false
    const t = today.value   // ← thêm .value vì là computed
    return t.getFullYear() === calendarYear.value &&
        t.getMonth() === calendarMonth.value &&
        t.getDate() === day
}

const selectDay = (day: number | null) => {
    if (!day || isDayDisabled(day)) return

    const finalDate = createLocalDate(
        calendarYear.value,
        calendarMonth.value,
        day,
        timeHour.value,
        timeMinute.value
    )

    emit('update:modelValue', new Date(finalDate.getTime()))
}

// ─── Time Handlers (sửa lại) ─────────────────────────────────────────

const updateTimeOnly = (newHour: number, newMinute: number) => {
    // Nếu đã có ngày trong modelValue, giữ nguyên ngày
    if (normalizedModelValue.value) {
        const current = normalizedModelValue.value
        const newDate = createLocalDate(
            current.getFullYear(),
            current.getMonth(),
            current.getDate(),
            newHour,
            newMinute
        )
        emit('update:modelValue', newDate)
        return
    }

    // Chưa có ngày → tạo ngày hôm nay (VN) với giờ/phút mới
    const now = new Date()
    // Lấy giờ VN tránh lệch múi
    const vnNow = new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Ho_Chi_Minh' }))
    const newDate = createLocalDate(
        vnNow.getFullYear(),
        vnNow.getMonth(),
        vnNow.getDate(),
        newHour,
        newMinute
    )
    emit('update:modelValue', newDate)
}

const onHourChange = (e: Event) => {
    let val = parseInt((e.target as HTMLInputElement).value) || 0
    if (isTimeDisabled.value && normalizedModelValue.value) {
        const minHour = effectiveMinDate.value!.getHours()
        val = Math.max(minHour, val)
    }
    val = Math.max(0, Math.min(23, val))
    updateTimeOnly(val, timeMinute.value)
}

const onMinuteChange = (e: Event) => {
    let val = parseInt((e.target as HTMLInputElement).value) || 0
    if (isTimeDisabled.value && normalizedModelValue.value) {
        const minHour = effectiveMinDate.value!.getHours()
        const minMinute = effectiveMinDate.value!.getMinutes()
        if (timeHour.value === minHour) {
            val = Math.max(minMinute, val)
        }
    }
    val = Math.max(0, Math.min(59, val))
    updateTimeOnly(timeHour.value, val)
}

const adjustHour = (delta: number) => {
    let newHour = (timeHour.value + delta + 24) % 24
    if (isTimeDisabled.value && normalizedModelValue.value) {
        const minHour = effectiveMinDate.value!.getHours()
        if (newHour < minHour) newHour = minHour
    }
    updateTimeOnly(newHour, timeMinute.value)
}

const adjustMinute = (delta: number) => {
    let newMinute = (timeMinute.value + delta + 60) % 60
    if (isTimeDisabled.value && normalizedModelValue.value) {
        const minHour = effectiveMinDate.value!.getHours()
        const minMinute = effectiveMinDate.value!.getMinutes()
        if (timeHour.value === minHour && newMinute < minMinute) {
            newMinute = minMinute
        }
    }
    updateTimeOnly(timeHour.value, newMinute)
}

const confirmSelection = () => {
    showCalendar.value = false
}

const clearValue = () => {
    emit('update:modelValue', null)
    showCalendar.value = false
}

const onFocusOut = (e: FocusEvent) => {
    const target = e.relatedTarget as HTMLElement | null
    if (target && (e.currentTarget as HTMLElement).contains(target)) return
    emit('blur')
}
</script>

<template>
    <div ref="containerRef" class="relative" @focusout="onFocusOut">
        <!-- Input -->
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
                <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24"
                    stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
            </button>
        </div>

        <!-- Calendar Dropdown -->
        <div v-if="showCalendar" :class="['absolute top-full left-0 z-70 mt-1 border rounded-lg shadow-xl p-3',
            mode === 'datetime' ? 'min-w-72' : 'min-w-64',
            'max-w-[calc(100vw-2rem)]', , th.dropdown]">

            <div :class="mode === 'datetime' ? 'flex gap-3' : ''">
                <!-- Calendar -->
                <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between mb-2">
                        <div class="flex items-center gap-0.5">
                            <button @click.stop="prevYear"
                                :class="['w-6 h-6 flex items-center justify-center rounded', th.navBtn]">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
                                </svg>
                            </button>
                            <button @click.stop="prevMonth"
                                :class="['w-6 h-6 flex items-center justify-center rounded', th.navBtn]">
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
                            <button @click.stop="nextMonth"
                                :class="['w-6 h-6 flex items-center justify-center rounded', th.navBtn]">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5"
                                        d="M9 5l7 7-7 7" />
                                </svg>
                            </button>
                            <button @click.stop="nextYear"
                                :class="['w-6 h-6 flex items-center justify-center rounded', th.navBtn]">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M13 5l7 7-7 7m-8-14l7 7-7 7" />
                                </svg>
                            </button>
                        </div>
                    </div>

                    <div class="grid grid-cols-7 mb-1">
                        <span v-for="day in DAY_NAMES" :key="day"
                            :class="['text-center text-[10px] font-medium py-1', th.dayHeader]">
                            {{ day }}
                        </span>
                    </div>

                    <div class="grid grid-cols-7 gap-y-0.5">
                        <button v-for="(day, idx) in calendarDays" :key="idx" type="button"
                            :disabled="!day || isDayDisabled(day)" @click.stop="selectDay(day)" :class="[
                                'relative h-7 w-full rounded text-[12px] transition-colors',
                                !day || isDayDisabled(day) ? 'text-slate-300 cursor-not-allowed pointer-events-none' :
                                    isSelectedDay(day) ? [th.selectedDay, 'font-medium'] : [th.dayBtn, 'cursor-pointer']
                            ]">
                            {{ day || '' }}
                            <span v-if="isTodayDay(day) && !isSelectedDay(day)"
                                :class="['absolute bottom-0.5 left-1/2 -translate-x-1/2 w-1 h-1 rounded-full', th.todayDot]" />
                        </button>
                    </div>
                </div>

                <!-- Time Picker -->
                <template v-if="mode === 'datetime'">
                    <div :class="['border-l', th.timeDivider]" />
                    <div class="flex flex-col items-center justify-center min-w-22.5">
                        <div class="flex items-center gap-1">
                            <div class="flex flex-col items-center gap-1">
                                <button @click.stop="adjustHour(1)"
                                    :class="['w-8 h-6 flex items-center justify-center rounded text-xs', th.timeBtn]">▲</button>
                                <input type="number" :value="String(timeHour).padStart(2, '0')"
                                    :class="['w-14 text-center text-base font-mono font-semibold border rounded px-1 py-2', th.timeInput, th.timeValue]"
                                    @change="onHourChange"
                                    @wheel="e => { e.preventDefault(); adjustHour(e.deltaY < 0 ? 1 : -1) }" min="0"
                                    max="23" />
                                <button @click.stop="adjustHour(-1)"
                                    :class="['w-8 h-6 flex items-center justify-center rounded text-xs', th.timeBtn]">▼</button>
                            </div>
                            <span :class="['text-lg font-bold', th.timeValue]">:</span>
                            <div class="flex flex-col items-center gap-1">
                                <button @click.stop="adjustMinute(5)"
                                    :class="['w-8 h-6 flex items-center justify-center rounded text-xs', th.timeBtn]">▲</button>
                                <input type="number" :value="String(timeMinute).padStart(2, '0')"
                                    :class="['w-14 text-center text-base font-mono font-semibold border rounded px-1 py-2', th.timeInput, th.timeValue]"
                                    @change="onMinuteChange"
                                    @wheel="e => { e.preventDefault(); adjustMinute(e.deltaY < 0 ? 5 : -5) }" min="0"
                                    max="59" />
                                <button @click.stop="adjustMinute(-5)"
                                    :class="['w-8 h-6 flex items-center justify-center rounded text-xs', th.timeBtn]">▼</button>
                            </div>
                        </div>
                        <p :class="['text-center text-[10px] mt-2', th.timeLabel]">Nhập · cuộn · ±5 phút</p>
                    </div>
                </template>
            </div>

            <!-- Footer -->
            <div :class="['flex justify-between items-center mt-3 pt-3 border-t', th.timeDivider]">
                <button @click.stop="clearValue"
                    :class="['px-3 py-1.5 text-xs rounded transition-colors', th.dayBtn, 'hover:bg-red-50 hover:text-red-500']">
                    Xóa
                </button>
                <button @click.stop="confirmSelection"
                    :class="['px-4 py-1.5 text-xs font-medium rounded transition-colors', th.selectedDay]">
                    Xác nhận
                </button>
            </div>
        </div>
    </div>
</template>