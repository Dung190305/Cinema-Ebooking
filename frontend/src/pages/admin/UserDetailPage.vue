<template>
    <div class="flex flex-col gap-6 py-6 pr-6">

        <!-- ── Back button ───────────────────────────────────────────────── -->
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

        <!-- ── Global errors ────────────────────────────────────────────── -->
        <div v-if="globalErrors.length" class="rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- ── Loading skeleton ──────────────────────────────────────────── -->
        <div v-if="isLoadingDetail" class="flex flex-col gap-6 animate-pulse">
            <div class="h-48 rounded-xl bg-slate-100" />
            <div class="grid grid-cols-2 gap-6">
                <div class="h-64 rounded-xl bg-slate-100" />
                <div class="h-64 rounded-xl bg-slate-100" />
            </div>
        </div>

        <!-- ── Not found ────────────────────────────────────────────────── -->
        <div v-else-if="!user" class="flex flex-col items-center justify-center py-20 text-slate-400">
            <UserX class="size-16 mb-4" />
            <p class="text-lg font-medium">Không tìm thấy người dùng</p>
        </div>

        <!-- ── User detail content ─────────────────────────────────────── -->
        <template v-else>
            <!-- Header card: avatar + name + role + status -->
            <div class="rounded-xl border border-slate-100 bg-white p-6 shadow-sm">
                <div class="flex items-start gap-5">
                    <!-- Avatar -->
                    <div
                        class="flex size-20 shrink-0 items-center justify-center rounded-full text-xl font-bold text-white"
                        :style="{ backgroundColor: avatarColor }"
                    >
                        {{ initials }}
                    </div>

                    <!-- Info -->
                    <div class="flex-1 min-w-0">
                        <div class="flex items-start justify-between gap-4">
                            <div>
                                <h1 class="text-xl font-semibold text-slate-900">{{ user.fullName }}</h1>
                                <p class="mt-1 text-sm text-slate-500">{{ user.email }}</p>
                                <p v-if="user.phoneNumber" class="mt-0.5 text-sm text-slate-400">
                                    {{ user.phoneNumber }}
                                </p>
                            </div>

                            <!-- Role + Status badges -->
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

                        <!-- Stats row -->
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

            <!-- Two-column grid -->
            <div class="grid grid-cols-2 gap-6">

                <!-- ── Profile info ─────────────────────────────────────── -->
                <div class="rounded-xl border border-slate-100 bg-white p-6 shadow-sm">
                    <h2 class="mb-4 text-sm font-semibold text-slate-700 uppercase tracking-wide">
                        Thông tin cá nhân
                    </h2>
                    <dl class="flex flex-col gap-3">
                        <InfoRow label="Họ và tên" :value="user.fullName" />
                        <InfoRow label="Email" :value="user.email" />
                        <InfoRow label="Số điện thoại" :value="user.phoneNumber ?? '—'" />
                        <InfoRow label="Ngày sinh" :value="formatDate(user.dateOfBirth)" />
                        <InfoRow label="Giới tính" :value="formatGender(user.gender)" />
                        <InfoRow label="Ngày tham gia" :value="formatDateTime(user.createdAt)" />
                    </dl>
                </div>

                <!-- ── Loyalty account ───────────────────────────────────── -->
                <div class="rounded-xl border border-slate-100 bg-white p-6 shadow-sm">
                    <h2 class="mb-4 text-sm font-semibold text-slate-700 uppercase tracking-wide">
                        Tài khoản tích điểm
                    </h2>

                    <template v-if="user.loyalty">
                        <dl class="flex flex-col gap-3">
                            <InfoRow label="Mã thẻ" :value="user.loyalty.loyaltyNumber ?? '—'" />
                            <InfoRow label="Hạng thành viên" :value="user.loyalty.membershipTierName ?? '—'" />
                            <InfoRow label="Điểm tích lũy" :value="formatPoints(user.loyalty.currentPoints)" />
                            <InfoRow label="Tổng điểm từng có" :value="formatPoints(user.loyalty.lifetimePoints)" />
                            <InfoRow label="Tổng chi tiêu" :value="user.loyalty.totalSpending ?? '—'" />
                            <InfoRow label="Trạng thái" :value="user.loyalty.status ?? '—'" />
                        </dl>
                    </template>
                    <template v-else>
                        <div class="flex flex-col items-center justify-center py-8 text-slate-400">
                            <GiftIcon class="size-10 mb-2 opacity-40" />
                            <p class="text-sm">Chưa có tài khoản tích điểm</p>
                        </div>
                    </template>
                </div>

                <!-- ── Latest booking ────────────────────────────────────── -->
                <div class="col-span-2 rounded-xl border border-slate-100 bg-white p-6 shadow-sm">
                    <h2 class="mb-4 text-sm font-semibold text-slate-700 uppercase tracking-wide">
                        Đặt vé gần nhất
                    </h2>

                    <template v-if="user.bookings?.latestBooking">
                        <div class="flex items-start gap-6">
                            <!-- Movie poster placeholder -->
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
            </div>

            <!-- ── Action buttons ─────────────────────────────────────────── -->
            <div class="flex items-center gap-3">
                <button
                    v-if="user.status === 'ACTIVE'"
                    class="flex items-center gap-2 rounded-lg border border-orange-200 bg-orange-50 px-5 py-2.5 text-sm font-medium text-orange-700 transition-colors hover:bg-orange-100"
                    :disabled="isActionLoading"
                    @click="handleDeactivate"
                >
                    <Ban class="size-4" />
                    Vô hiệu hóa
                </button>
                <button
                    v-else-if="user.status === 'INACTIVE'"
                    class="flex items-center gap-2 rounded-lg border border-green-200 bg-green-50 px-5 py-2.5 text-sm font-medium text-green-700 transition-colors hover:bg-green-100"
                    :disabled="isActionLoading"
                    @click="handleActivate"
                >
                    <CheckCircle class="size-4" />
                    Kích hoạt
                </button>

                <!-- Loading indicator -->
                <span v-if="isActionLoading" class="flex items-center gap-2 text-sm text-slate-400">
                    <span class="size-4 animate-spin rounded-full border-2 border-slate-200 border-t-slate-500" />
                    Đang xử lý...
                </span>
            </div>
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, UserX, Film, ClipboardList, Ban, CheckCircle } from 'lucide-vue-next'
import GiftIcon from '@/components/ui/icon/GiftIcon.vue'
import { useUser } from '@/composables/useUser'
import type { UserDetailResponse } from '@/types/user.types'

// Inline InfoRow sub-component
const InfoRow = {
    props: { label: String, value: String },
    template: `
        <div class="flex items-start justify-between gap-4">
            <dt class="text-sm text-slate-400 min-w-[120px]">{{ label }}</dt>
            <dd class="text-sm font-medium text-slate-800 text-right">{{ value }}</dd>
        </div>
    `,
}

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

function formatDate(iso: string | null): string {
    if (!iso) return '—'
    try {
        return new Date(iso).toLocaleDateString('vi-VN', {
            day: '2-digit', month: '2-digit', year: 'numeric',
        })
    } catch { return iso }
}

function formatDateTime(iso: string | null): string {
    if (!iso) return '—'
    try {
        return new Date(iso).toLocaleString('vi-VN', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit',
        })
    } catch { return iso }
}

function formatGender(g: string | null): string {
    switch (g) {
        case 'MALE': return 'Nam'
        case 'FEMALE': return 'Nữ'
        case 'OTHER': return 'Khác'
        default: return '—'
    }
}

function formatPoints(value: number | null): string {
    if (value == null) return '—'
    return new Intl.NumberFormat('vi-VN').format(value) + ' điểm'
}

async function handleActivate() {
    if (!user.value) return
    const ok = await activateUser(user.value.id)
    if (ok) await fetchUserDetail(user.value.id)
}

async function handleDeactivate() {
    if (!user.value) return
    const ok = await deactivateUser(user.value.id)
    if (ok) await fetchUserDetail(user.value.id)
}

onMounted(() => {
    fetchUserDetail(userId.value)
})
</script>
