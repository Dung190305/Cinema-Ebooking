<template>
    <div class="flex flex-wrap gap-4 items-end p-4 rounded-lg">
        <!-- Thành phố -->
        <div class="flex-1 min-w-37.5">
            <label class="block text-caption text-text-secondary mb-1">Thành phố</label>
            <select :value="selectedCity" @change="handleCityChange"
                class="w-full p-2 border border-border-default rounded-md bg-bg-base text-text-primary">
                <option value="">Tất cả</option>
                <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
            </select>
        </div>

        <!-- Rạp -->
        <div class="flex-1 min-w-45">
            <label class="block text-caption text-text-secondary mb-1">Rạp</label>
            <select :value="selectedCinemaId ?? ''" @change="handleCinemaChange"
                class="w-full p-2 border border-border-default rounded-md bg-bg-base text-text-primary">
                <option value="">Tất cả rạp</option>
                <option v-for="cinema in cinemas" :key="cinema.id" :value="cinema.id">
                    {{ cinema.name }}
                </option>
            </select>
        </div>

        <!-- Ngày chiếu – dùng CalendarPicker -->
        <div class="flex-1 min-w-37.5">
            <label class="block text-caption text-text-secondary mb-1">Ngày chiếu</label>
            <CalendarPicker :model-value="internalDate" mode="date" variant="web" :min-date="new Date()"
                @update:model-value="onDateChanged" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import CalendarPicker from '@/components/ui/calendar/CalendarPicker.vue'
import { dateToISOString } from '@/utils/dateFormat'
import type { CinemaResponse } from '@/types/cinema'

const props = defineProps<{
    cities: string[]
    cinemas: CinemaResponse[]
    selectedCity: string
    selectedCinemaId: number | null
    selectedDate: string // vẫn là string 'yyyy-MM-dd' hoặc ''
}>()

const emit = defineEmits<{
    'update:selectedCity': [value: string]
    'update:selectedCinemaId': [value: number | null]
    'update:selectedDate': [value: string]
}>()

// --- Rạp & Thành phố handlers (giữ nguyên) ---
const handleCityChange = (event: Event) => {
    const target = event.target as HTMLSelectElement
    emit('update:selectedCity', target.value)
}

const handleCinemaChange = (event: Event) => {
    const target = event.target as HTMLSelectElement
    const value = target.value === '' ? null : Number(target.value)
    emit('update:selectedCinemaId', value)
}

const internalDate = ref<Date | null>(null)

watch(
    () => props.selectedDate,
    (newVal) => {
        if (!newVal) {
            internalDate.value = null
            return
        }
        // Parse YYYY-MM-DD an toàn
        const [year, month, day] = newVal.split('-').map(Number)
        const parsed = new Date(year, month - 1, day)
        internalDate.value = isNaN(parsed.getTime()) ? null : parsed
    },
    { immediate: true }
)

const onDateChanged = (value: Date | null) => {
    internalDate.value = value
    if (!value) {
        emit('update:selectedDate', '') // xóa bộ lọc => hiển thị tất cả
        return
    }
    // Gửi chuỗi YYYY-MM-DD
    emit('update:selectedDate', dateToISOString(value, false))
}
</script>