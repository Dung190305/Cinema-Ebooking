<template>
    <div class="flex flex-col gap-6 py-6 pr-6">

        <div class="flex items-center gap-3">
            <button
                class="flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-1.5 text-sm text-slate-600 transition-colors hover:bg-slate-50 hover:text-slate-900"
                @click="router.back()"
            >
                <ArrowLeft class="size-4" />
                Quay lại
            </button>
            <span class="text-sm text-text-admin-tertiary">Users</span>
            <span class="text-sm text-text-admin-tertiary">/</span>
            <span class="text-sm text-text-admin-primary font-medium">
                {{ user?.fullName ?? '—' }}
            </span>
        </div>

        <div v-if="globalErrors.length" class="rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <div v-if="isLoadingDetail" class="flex flex-col gap-6 animate-pulse">
            <div class="h-48 rounded-xl bg-slate-100" />
            <div class="h-40 rounded-xl bg-slate-100" />
        </div>

        <div v-else-if="!user" class="flex flex-col items-center justify-center py-20 text-slate-400">
            <UserX class="size-16 mb-4" />
            <p class="text-lg font-medium">Không tìm thấy người dùng</p>
        </div>

        <template v-else>
            <div class="rounded-xl border border-slate-100 bg-white p-6 shadow-sm">
                <div class="flex items-start gap-5">
                    <div
                        class="flex size-20 shrink-0 items-center justify-center rounded-full text-xl font-bold text-white"
                        :style="{ backgroundColor: avatarColor }"
                    >
                        {{ initials }}
                    </div>

                    <div class="flex-1 min-w-0">
                        <div class="flex items-start justify-between gap-4">
                            <div>
                                <h1 class="text-xl font-semibold text-slate-900">{{ user.fullName }}</h1>
                                <p class="mt-1 text-sm text-slate-500">{{ user.email }}</p>
                                <p v-if="user.phoneNumber" class="mt-0.5 text-sm text-slate-400">
                                    {{ user.phoneNumber }}
                                </p>
                            </div>

                            <div class="flex items-center gap-2 shrink-0">
                                <span
                                    class="inline-block rounded-full px-3 py-1 text-xs font-semibold"
                                    :class="user.role === 'ADMIN'
                                        ? 'bg-purple-100 text-purple-700'
                                        : 'bg-blue-100 text-blue-700'"
                                >
                                    {{ user.role }}
                                </span>
                                <span
                                    class="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold"
                                    :class="statusClass"
                                >
                                    <span class="size-1.5 rounded-full" :class="statusDotClass" />
                                    {{ user.status }}
                                </span>
                            </div>
                        </div>

                        <div class="mt-5 grid grid-cols-4 gap-4">
                            <div class="rounded-lg bg-slate-50 p-3 text-center">
                                <p class="text-xs text-slate-400">Đã đặt</p>
                                <p class="mt-1 text-lg font-bold text-slate-800">
                                    {{ user.bookings?.totalBookings ?? 0 }}
                                </p>
                            </div>
                            <div class="rounded-lg bg-slate-50 p-3 text-center">
                                <p class="text-xs text-slate-400">Tổng chi tiêu</p>
                                <p class="mt-1 text-lg font-bold text-slate-800">
                                    {{ user.bookings?.totalSpent ?? '—' }}
                                </p>
                            </div>
                            <div class="rounded-lg bg-slate-50 p-3 text-center">
                                <p class="text-xs text-slate-400">Điểm hiện có</p>
                                <p class="mt-1 text-lg font-bold text-slate-800">
                                    {{ formatPoints(user.loyalty?.currentPoints) }}
                                </p>
                            </div>
                            <div class="rounded-lg bg-slate-50 p-3 text-center">
                                <p class="text-xs text-slate-400">Hạng thành viên</p>
                                <p class="mt-1 text-lg font-bold text-slate-800">
                                    {{ user.loyalty?.membershipTierName ?? '—' }}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="w-full rounded-xl border border-slate-100 bg-white p-6 shadow-sm">
                <h2 class="mb-4 text-sm font-semibold text-slate-700 uppercase tracking-wide">
                    Đặt vé gần nhất
                </h2>

                <template v-if="user.bookings?.latestBooking">
                    <div class="flex items-start gap-6">
                        <div class="flex size-16 shrink-0 items-center justify-center rounded-lg bg-slate-100">
                            <Film class="size-8 text-slate-300" />
                        </div>

                        <div class="flex-1 min-w-0">
                            <h3 class="text-base font-semibold text-slate-900">
                                {{ user.bookings.latestBooking.movieTitle }}
                            </h3>
                            <p class="mt-1 text-sm text-slate-500">
                                {{ user.bookings.latestBooking.cinemaName }}
                                · {{ user.bookings.latestBooking.roomName }}
                            </p>
                            <p class="mt-0.5 text-sm text-slate-400">
                                {{ user.bookings.latestBooking.showtimeStartTime }}
                            </p>
                            <p v-if="user.bookings.latestBooking.bookedAt" class="mt-0.5 text-xs text-slate-400">
                                Đặt lúc: {{ user.bookings.latestBooking.bookedAt }}
                            </p>

                            <div class="mt-3 flex items-center gap-4">
                                <span class="text-sm font-medium text-slate-700">
                                    {{ user.bookings.latestBooking.finalAmount }}
                                </span>
                                <span
                                    class="inline-block rounded-full px-2.5 py-0.5 text-xs font-medium"
                                    :class="bookingStatusClass"
                                >
                                    {{ user.bookings.latestBooking.status }}
                                </span>
                                <span class="text-xs text-slate-400">
                                    Mã: {{ user.bookings.latestBooking.bookingCode }}
                                </span>
                            </div>
                        </div>
                    </div>
                </template>
                <template v-else>
                    <div class="flex flex-col items-center justify-center py-8 text-slate-400">
                        <ClipboardList class="size-10 mb-2 opacity-40" />
                        <p class="text-sm">Chưa có lịch sử đặt vé</p>
                    </div>
                </template>
            </div>

            <div class="flex items-center gap-3">
                <button
                    v-if="user.status === 'ACTIVE'"
                    class="flex items-center gap-2 rounded-lg border border-orange-200 bg-orange-50 px-5 py-2.5 text-sm font-medium text-orange-700 transition-colors hover:bg-orange-100"
                    :disabled="isActionLoading"
                    @click="openConfirmModal('DEACTIVATE')"
                >
                    <Ban class="size-4" />
                    Vô hiệu hóa
                </button>
                <button
                    v-else-if="user.status === 'INACTIVE'"
                    class="flex items-center gap-2 rounded-lg border border-green-200 bg-green-50 px-5 py-2.5 text-sm font-medium text-green-700 transition-colors hover:bg-green-100"
                    :disabled="isActionLoading"
                    @click="openConfirmModal('ACTIVATE')"
                >
                    <CheckCircle class="size-4" />
                    Kích hoạt
                </button>

                <span v-if="isActionLoading" class="flex items-center gap-2 text-sm text-slate-400">
                    <span class="size-4 animate-spin rounded-full border-2 border-slate-200 border-t-slate-500" />
                    Đang xử lý...
                </span>
            </div>
        </template>

        <div 
            v-if="showModal" 
            class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 transition-opacity"
            @click.self="closeModal"
        >
            <div class="w-full max-w-md transform rounded-xl bg-white p-6 shadow-xl transition-all animate-in fade-in zoom-in-95 duration-200">
                <h3 class="text-lg font-semibold text-slate-900">
                    {{ modalContent.title }}
                </h3>
                <p class="mt-2 text-sm text-slate-500">
                    {{ modalContent.message }}
                </p>

                <div class="mt-6 flex justify-end gap-3">
                    <button
                        type="button"
                        class="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                        @click="closeModal"
                    >
                        Hủy bỏ
                    </button>
                    <button
                        type="button"
                        class="rounded-lg px-4 py-2 text-sm font-medium text-white shadow-sm"
                        :class="modalContent.confirmClass"
                        @click="handleConfirmAction"
                    >
                        Xác nhận
                    </button>
                </div>
            </div>
        </div>

    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, UserX, Film, ClipboardList, Ban, CheckCircle } from 'lucide-vue-next'
import { useUser } from '@/composables/useUser'

const route = useRoute()
const router = useRouter()

const {
    selectedUser: user,
    isLoadingDetail,
    globalErrors,
    isActionLoading,
    fetchUserDetail,
    activateUser,
    deactivateUser,
} = useUser()

const userId = computed(() => Number(route.params.id))

// Popup States
const showModal = ref(false)
const pendingAction = ref<'ACTIVATE' | 'DEACTIVATE' | null>(null)

const modalContent = computed(() => {
    if (pendingAction.value === 'DEACTIVATE') {
        return {
            title: 'Xác nhận vô hiệu hóa',
            message: `Bạn có chắc chắn muốn vô hiệu hóa người dùng "${user.value?.fullName}" không? Tài khoản này sẽ không thể đăng nhập vào hệ thống.`,
            confirmClass: 'bg-orange-600 hover:bg-orange-700'
        }
    }
    return {
        title: 'Xác nhận kích hoạt',
        message: `Bạn có chắc chắn muốn kích hoạt lại người dùng "${user.value?.fullName}" không?`,
        confirmClass: 'bg-green-600 hover:bg-green-700'
    }
})

// Avatar & Status Computeds
const AVATAR_COLORS = [
    '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e',
    '#f97316', '#eab308', '#22c55e', '#14b8a6',
    '#06b6d4', '#3b82f6', '#a855f7', '#f59e0b',
]
const avatarColor = computed(() => {
    const name = user.value?.fullName ?? ''
    const idx = name.split('').reduce((a, c) => a + c.charCodeAt(0), 0)
    return AVATAR_COLORS[idx % AVATAR_COLORS.length]
})
const initials = computed(() => {
    const name = user.value?.fullName ?? ''
    const parts = name.trim().split(/\s+/)
    if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    return name.slice(0, 2).toUpperCase()
})

const statusClass = computed(() => {
    switch (user.value?.status) {
        case 'ACTIVE': return 'bg-green-100 text-green-700'
        case 'INACTIVE': return 'bg-gray-100 text-gray-500'
        case 'BANNED': return 'bg-red-100 text-red-700'
        default: return 'bg-gray-100 text-gray-500'
    }
})
const statusDotClass = computed(() => {
    switch (user.value?.status) {
        case 'ACTIVE': return 'bg-green-500'
        case 'INACTIVE': return 'bg-gray-400'
        case 'BANNED': return 'bg-red-500'
        default: return 'bg-gray-400'
    }
})
const bookingStatusClass = computed(() => {
    const s = user.value?.bookings?.latestBooking?.status
    switch (s) {
        case 'PAID': return 'bg-green-100 text-green-700'
        case 'CANCELLED': return 'bg-red-100 text-red-700'
        case 'PENDING': return 'bg-yellow-100 text-yellow-700'
        default: return 'bg-slate-100 text-slate-600'
    }
})

function formatPoints(value: number | null): string {
    if (value == null) return '—'
    return new Intl.NumberFormat('vi-VN').format(value) + ' điểm'
}

// Modal Handlers
function openConfirmModal(action: 'ACTIVATE' | 'DEACTIVATE') {
    pendingAction.value = action
    showModal.value = true
}

function closeModal() {
    showModal.value = false
    pendingAction.value = null
}

async function handleConfirmAction() {
    if (!user.value || !pendingAction.value) return
    
    const actionToRun = pendingAction.value
    closeModal() // Ẩn popup ngay khi bấm xác nhận để có trải nghiệm mượt mà

    let ok = false
    if (actionToRun === 'ACTIVATE') {
        ok = await activateUser(user.value.id)
    } else if (actionToRun === 'DEACTIVATE') {
        ok = await deactivateUser(user.value.id)
    }

    if (ok) {
        await fetchUserDetail(user.value.id)
    }
}

onMounted(() => {
    fetchUserDetail(userId.value)
})
</script>