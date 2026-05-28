<script setup lang="ts">
import { inject, onMounted, ref, computed } from 'vue'
import type { BookingState } from '@/composables/useBooking'
import type { CinemaResponse } from '@/types/cinema'
import { useCinema } from '@/composables/useCinema'

const emit = defineEmits(['next'])
const booking = inject<BookingState>('booking')!

// Sử dụng composable useCinema
const { cinemas, isLoading, fetchAll, setCinemas } = useCinema()

// State lọc thành phố
const selectedCity = ref('')

// Trích xuất danh sách thành phố duy nhất từ cinemas
const cities = computed(() => {
    const citySet = new Set(cinemas.value.map(c => c.city).filter(Boolean))
    return Array.from(citySet).sort()
})

// Lọc rạp theo thành phố
const filteredCinemas = computed(() => {
    if (!selectedCity.value) return cinemas.value
    return cinemas.value.filter(c => c.city === selectedCity.value)
})

onMounted(async () => {
    // Reset toàn bộ booking state khi quay lại bước 1
    booking.selectedCinema.value = null
    booking.selectedMovie.value = null
    booking.selectedShowtime.value = null
    booking.selectedSeats.value = []
    booking.selectedCombos.value = []
    booking.appliedCoupon.value = null
    const data = await fetchAll()
    setCinemas(data)
})

function selectCinema(cinema: CinemaResponse) {
    booking.selectedCinema.value = cinema
    booking.selectedMovie.value = null
    booking.selectedShowtime.value = null
    booking.selectedSeats.value = []
    emit('next')
}

function validateAndNext() {
    if (!booking.selectedCinema.value) {
        alert('Vui lòng chọn rạp')
        return false
    }
    emit('next')
    return true
}

defineExpose({ next: validateAndNext })
</script>

<template>
    <div>
        <h2 class="text-title mb-6">Chọn rạp chiếu phim</h2>

        <!-- Loading -->
        <div v-if="isLoading" class="flex justify-center py-12">
            <div class="animate-spin h-8 w-8 border-4 border-accent border-t-transparent rounded-full"></div>
        </div>

        <template v-else>
            <!-- Chọn Thành Phố -->
            <div class="mb-8" v-if="cities.length">
                <p class="text-caption text-text-secondary mb-3">Thành phố</p>
                <div class="flex flex-wrap gap-2">
                    <button class="px-5 py-2.5 border rounded-2xl text-sm font-medium transition-all" :class="!selectedCity
                        ? 'border-accent bg-accent/10 text-accent font-semibold'
                        : 'border-border-default text-text-secondary hover:border-accent'" @click="selectedCity = ''">
                        Tất cả thành phố
                    </button>

                    <button v-for="city in cities" :key="city"
                        class="px-5 py-2.5 border rounded-2xl text-sm font-medium transition-all whitespace-nowrap"
                        :class="selectedCity === city
                            ? 'border-accent bg-accent/10 text-accent font-semibold'
                            : 'border-border-default text-text-secondary hover:border-accent'"
                        @click="selectedCity = city">
                        {{ city }}
                    </button>
                </div>
            </div>

            <!-- Danh sách Rạp -->
            <div v-if="filteredCinemas.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <button v-for="cinema in filteredCinemas" :key="cinema.id"
                    class="group bg-bg-surface border border-border-default hover:border-accent hover:shadow-xl rounded-3xl p-6 text-left transition-all duration-300 flex flex-col h-full"
                    @click="selectCinema(cinema)">
                    <div class="flex justify-between items-start mb-4">
                        <div class="flex-1">
                            <p class="text-body font-semibold group-hover:text-accent transition-colors">
                                {{ cinema.name }}
                            </p>
                            <p class="text-caption text-text-secondary mt-1 line-clamp-2">
                                {{ cinema.address }}
                            </p>
                        </div>
                        <div
                            class="ml-4 text-accent opacity-0 group-hover:opacity-100 transition-all text-2xl shrink-0">
                            →
                        </div>
                    </div>
                    <div
                        class="mt-auto pt-4 border-t border-border-subtle flex items-center justify-between text-xs text-text-tertiary">
                        <span>🎥 Phim đang chiếu</span>
                        <span class="flex items-center gap-1">
                            <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
                            Sẵn sàng
                        </span>
                    </div>
                </button>
            </div>

            <!-- Không tìm thấy rạp -->
            <div v-else class="text-center py-20">
                <div class="text-5xl mb-4">🏙️</div>
                <p class="text-body text-text-secondary">Không tìm thấy rạp nào</p>
                <p class="text-caption text-text-tertiary mt-1">
                    Vui lòng thử chọn thành phố khác
                </p>
            </div>
        </template>
    </div>
</template>