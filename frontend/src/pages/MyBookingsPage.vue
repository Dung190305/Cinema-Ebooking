<!-- pages/MyBookingsPage.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useBookingFilters } from '@/composables/useBookingFilters'
import type { BookingStatus } from '@/types/booking.types'
import { formatDateTimeVN } from '@/utils/dateFormat'
import CalendarPicker from '@/components/ui/calendar/CalendarPicker.vue'
import BookingDetailModal from '@/components/booking/BookingDetailModal.vue'

const {
    loading,
    bookings,
    pageable,
    status,
    fromDate,
    toDate,
    fetchBookings,
    handlePageChange,
    handleFilter,
    clearFilters,
} = useBookingFilters()

const statusOptions = [
    { value: '', label: 'Tất cả' },
    { value: 'PENDING', label: 'Chờ thanh toán' },
    { value: 'CONFIRMED', label: 'Đã thanh toán' },
    { value: 'CANCELLED', label: 'Đã hủy' },
]

// ── Modal state ──────────────────────────────────────────────────────────────
const selectedBookingId = ref<number | null>(null)

function openDetail(bookingId: number) {
    selectedBookingId.value = bookingId
}

function closeDetail() {
    selectedBookingId.value = null
}

// ── Status helpers ───────────────────────────────────────────────────────────
const getStatusBadgeClass = (status: BookingStatus) => {
    switch (status) {
        case 'CONFIRMED': return 'bg-green-100 text-green-800'
        case 'PENDING': return 'bg-yellow-100 text-yellow-800'
        case 'CANCELLED': return 'bg-red-100 text-red-800'
        default: return 'bg-gray-100 text-gray-800'
    }
}

const getStatusText = (status: BookingStatus) => {
    switch (status) {
        case 'CONFIRMED': return 'Đã thanh toán'
        case 'PENDING': return 'Chờ thanh toán'
        case 'CANCELLED': return 'Đã hủy'
        default: return status
    }
}

onMounted(() => {
    fetchBookings()
})
</script>

<template>
    <div class="min-h-screen bg-bg-base py-8 px-4 sm:px-6 lg:px-8">
        <div class="max-w-7xl mx-auto">
            <!-- Header -->
            <div class="mb-8">
                <h1 class="text-title text-text-primary">Vé của tôi</h1>
                <p class="text-body text-text-secondary mt-1">Quản lý và theo dõi lịch sử đặt vé xem phim</p>
            </div>

            <!-- Filters -->
            <div class="bg-bg-surface rounded-xl shadow-sm p-4 mb-6 border border-border-subtle">
                <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <div>
                        <label class="block text-caption text-text-secondary mb-1">Trạng thái</label>
                        <select v-model="status" @change="handleFilter"
                            class="w-full rounded-lg border-border-default bg-bg-base text-text-primary px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent">
                            <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}
                            </option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-caption text-text-secondary mb-1">Từ ngày</label>
                        <CalendarPicker v-model="fromDate" mode="date" variant="web"
                            @update:model-value="handleFilter" />
                    </div>
                    <div>
                        <label class="block text-caption text-text-secondary mb-1">Đến ngày</label>
                        <CalendarPicker v-model="toDate" mode="date" variant="web" @update:model-value="handleFilter" />
                    </div>
                </div>
                <div class="mt-4 flex justify-end">
                    <button @click="clearFilters"
                        class="px-4 py-2 text-caption text-accent hover:underline focus:outline-none">
                        Xóa bộ lọc
                    </button>
                </div>
            </div>

            <!-- Loading skeleton -->
            <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div v-for="i in 3" :key="i" class="bg-bg-surface rounded-xl shadow-sm p-4 border border-border-subtle">
                    <div class="animate-pulse space-y-3">
                        <div class="h-4 bg-gray-200 rounded w-3/4"></div>
                        <div class="h-4 bg-gray-200 rounded w-1/2"></div>
                        <div class="h-4 bg-gray-200 rounded w-full"></div>
                        <div class="h-8 bg-gray-200 rounded w-1/3"></div>
                    </div>
                </div>
            </div>

            <!-- Empty state -->
            <div v-else-if="bookings.length === 0"
                class="bg-bg-surface rounded-xl shadow-sm p-12 text-center border border-border-subtle">
                <p class="text-body text-text-secondary">Bạn chưa có đơn đặt vé nào.</p>
                <router-link to="/movies" class="inline-block mt-4 text-accent hover:underline">
                    Đặt vé ngay
                </router-link>
            </div>

            <!-- Bookings grid -->
            <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div v-for="booking in bookings" :key="booking.bookingId"
                    class="group bg-bg-surface rounded-xl shadow-sm overflow-hidden border border-border-subtle transition-all hover:shadow-md hover:-translate-y-1">
                    <div class="p-5">
                        <div class="flex justify-between items-start mb-3">
                            <div>
                                <span class="text-caption text-text-tertiary">Mã đơn</span>
                                <p class="text-body font-mono font-medium text-text-primary">{{ booking.bookingCode }}
                                </p>
                            </div>
                            <span
                                :class="['px-2 py-1 rounded-full text-xs font-medium', getStatusBadgeClass(booking.status)]">
                                {{ getStatusText(booking.status) }}
                            </span>
                        </div>
                        <div class="space-y-2">
                            <div>
                                <span class="text-caption text-text-tertiary">Phim</span>
                                <p class="text-body font-medium text-text-primary line-clamp-1">{{ booking.movieTitle }}
                                </p>
                            </div>
                            <div>
                                <span class="text-caption text-text-tertiary">Suất chiếu</span>
                                <p class="text-body text-text-primary">{{ formatDateTimeVN(booking.showtime) }}</p>
                            </div>
                            <div>
                                <span class="text-caption text-text-tertiary">Tổng tiền</span>
                                <p class="text-title text-accent font-bold">
                                    {{ new Intl.NumberFormat('vi-VN', {
                                        style: 'currency', currency: 'VND'
                                    }).format(booking.finalAmount) }}
                                </p>
                            </div>
                            <div
                                class="flex justify-between text-caption text-text-tertiary pt-2 border-t border-border-subtle">
                                <span>Đặt lúc: {{ formatDateTimeVN(booking.createdAt) }}</span>
                                <span v-if="booking.paidAt">Thanh toán: {{ formatDateTimeVN(booking.paidAt) }}</span>
                            </div>
                        </div>

                        <!-- Xem chi tiết → mở modal thay vì router-link -->
                        <div class="mt-4">
                            <button @click="openDetail(booking.bookingId)"
                                class="inline-flex items-center text-sm text-accent hover:underline focus:outline-none">
                                Xem chi tiết
                                <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M9 5l7 7-7 7" />
                                </svg>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Pagination -->
            <div v-if="!loading && pageable.totalPages.value > 1" class="mt-8 flex justify-center">
                <div class="flex gap-2">
                    <button @click="handlePageChange(pageable.page.value - 1)" :disabled="pageable.page.value === 0"
                        class="px-3 py-1 rounded-md border border-border-default text-text-primary disabled:opacity-50 disabled:cursor-not-allowed hover:bg-bg-surface">
                        Trước
                    </button>
                    <span class="px-3 py-1 text-text-secondary">
                        Trang {{ pageable.page.value + 1 }} / {{ pageable.totalPages.value }}
                    </span>
                    <button @click="handlePageChange(pageable.page.value + 1)"
                        :disabled="pageable.page.value + 1 >= pageable.totalPages.value"
                        class="px-3 py-1 rounded-md border border-border-default text-text-primary disabled:opacity-50 disabled:cursor-not-allowed hover:bg-bg-surface">
                        Sau
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Booking Detail Modal -->
    <BookingDetailModal :booking-id="selectedBookingId" @close="closeDetail" />
</template>