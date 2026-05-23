<template>
    <div class="flex flex-col gap-6 py-6 pr-6">

        <div class="flex flex-col gap-1.5">
            <div class="flex items-center text-xs font-medium text-text-admin-tertiary">
                <span>Users</span>
            </div>

            <div class="flex items-center justify-between">
                <div>
                    <h1 class="text-xl font-bold text-slate-900 tracking-tight">
                        Người dùng
                    </h1>
                    <p class="text-sm text-text-admin-tertiary mt-0.5">
                        Tổng số: <span class="font-semibold text-slate-700">{{ totalItems }}</span> người dùng
                    </p>
                </div>
            </div>
        </div>

        <div v-if="globalErrors.length"
            class="rounded-xl bg-red-50 border border-red-100 p-4 shadow-sm animate-in fade-in duration-300">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600 flex items-center gap-2">
                <span class="size-1.5 rounded-full bg-red-500 shrink-0" />
                {{ err }}
            </p>
        </div>

        <div class="flex flex-wrap items-center gap-3 bg-slate-50/50 p-3 rounded-xl border border-slate-100">
            <div class="relative flex-1 min-w-50">
                <Search class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-slate-400 pointer-events-none" />
                <input v-model.trim="searchName" type="text" placeholder="Tìm theo tên..."
                    class="w-full rounded-lg border border-slate-200 bg-white pl-9 pr-3 py-2 text-sm text-slate-900 outline-none transition focus:border-accent focus:ring-2 focus:ring-accent/5 placeholder:text-slate-400"
                    @keyup.enter="applySearch" />
            </div>

            <div class="relative flex-1 min-w-50">
                <Mail class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-slate-400 pointer-events-none" />
                <input v-model.trim="searchEmail" type="email" placeholder="Tìm theo email..."
                    class="w-full rounded-lg border border-slate-200 bg-white pl-9 pr-3 py-2 text-sm text-slate-900 outline-none transition focus:border-accent focus:ring-2 focus:ring-accent/5 placeholder:text-slate-400"
                    @keyup.enter="applySearch" />
            </div>

            <select v-model="filterRole"
                class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-accent focus:ring-2 focus:ring-accent/5 cursor-pointer min-w-35"
                @change="applySearch">
                <option value="">Tất cả vai trò</option>
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
            </select>

            <select v-model="filterStatus"
                class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-accent focus:ring-2 focus:ring-accent/5 cursor-pointer min-w-37.5"
                @change="applySearch">
                <option value="">Tất cả trạng thái</option>
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
                <option value="BANNED">BANNED</option>
            </select>

            <div class="flex items-center gap-2 ml-auto">
                <button
                    class="flex items-center gap-1.5 rounded-lg bg-accent px-4 py-2 text-sm font-medium text-text-on-accent transition-all shadow-sm hover:bg-accent/90 active:scale-[0.98]"
                    @click="applySearch">
                    <Search class="size-4" />
                    Tìm kiếm
                </button>

                <button v-if="hasActiveFilters"
                    class="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 hover:text-slate-900 shadow-xs"
                    @click="clearAllFilters">
                    <X class="size-4" />
                    Xóa lọc
                </button>
            </div>
        </div>

        <div class="relative overflow-hidden rounded-xl border border-slate-100 bg-white shadow-sm">
            <DataTable :rows="users" :columns="columns" :fieldErrors="fieldErrors" :showDelete="false"
                :showCreate="false" :showSave="false" @save="() => { }">
                <template #cell-fullName="{ item }">
                    <div class="flex items-center gap-3 max-w-50">
                        <div class="flex size-8 shrink-0 items-center justify-center rounded-full text-xs font-bold text-white shadow-xs"
                            :style="{ backgroundColor: getAvatarColor(item.fullName) }">
                            {{ getInitials(item.fullName) }}
                        </div>
                        <span class="truncate text-sm font-semibold text-slate-900">{{ item.fullName }}</span>
                    </div>
                </template>

                <template #cell-avatarUrl="{ value }">
                    <span class="text-sm text-slate-500 truncate block max-w-37.5">{{ value || '—' }}</span>
                </template>

                <template #cell-phoneNumber="{ value }">
                    <span class="text-sm font-medium text-slate-600">{{ value || '—' }}</span>
                </template>

                <template #cell-role="{ value }">
                    <span class="inline-block rounded-md px-2 py-0.5 text-xs font-semibold uppercase tracking-wider"
                        :class="value === 'ADMIN'
                            ? 'bg-purple-50 text-purple-700 border border-purple-100'
                            : 'bg-blue-50 text-blue-700 border border-blue-100'">
                        {{ value }}
                    </span>
                </template>

                <template #cell-status="{ value }">
                    <span class="inline-flex items-center gap-1.5 rounded-md px-2 py-0.5 text-xs font-semibold border"
                        :class="value === 'ACTIVE'
                            ? 'bg-green-50 border-green-100 text-green-700'
                            : value === 'INACTIVE'
                                ? 'bg-gray-50 border-gray-200 text-gray-500'
                                : 'bg-red-50 border-red-100 text-red-700'">
                        <span class="size-1.5 rounded-full"
                            :class="value === 'ACTIVE' ? 'bg-green-500' : value === 'INACTIVE' ? 'bg-gray-400' : 'bg-red-500'" />
                        {{ value }}
                    </span>
                </template>

                <template #cell-createdAt="{ value }">
                    <span class="text-sm text-slate-500">{{ formatDate(value) }}</span>
                </template>

                <template #detail-actions="{ item }">
                    <button
                        class="flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white py-2.5 text-sm font-medium text-slate-700 transition-all shadow-xs hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900"
                        @click="goToDetail(item.id)">
                        <Eye class="size-4 text-slate-500" />
                        Xem chi tiết
                    </button>
                </template>
            </DataTable>
        </div>

        <div v-if="totalPages > 1"
            class="flex justify-center items-center gap-1.5 mt-2 animate-in fade-in duration-300">
            <button
                class="flex items-center justify-center size-8 rounded-lg border border-slate-200 bg-white text-slate-600 transition-all hover:bg-slate-50 active:scale-95 disabled:opacity-40 disabled:cursor-not-allowed disabled:pointer-events-none"
                :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
                <ChevronLeft class="size-4" />
            </button>

            <button v-for="page in visiblePages" :key="page"
                class="min-w-8 h-8 rounded-lg text-sm font-medium transition-all active:scale-95" :class="page === currentPage
                    ? 'bg-accent text-text-on-accent shadow-xs font-semibold'
                    : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'" @click="goToPage(page)">
                {{ page + 1 }}
            </button>

            <button
                class="flex items-center justify-center size-8 rounded-lg border border-slate-200 bg-white text-slate-600 transition-all hover:bg-slate-50 active:scale-95 disabled:opacity-40 disabled:cursor-not-allowed disabled:pointer-events-none"
                :disabled="currentPage === totalPages - 1" @click="goToPage(currentPage + 1)">
                <ChevronRight class="size-4" />
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, X, Eye, ChevronLeft, ChevronRight, Mail } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import { useUser } from '@/composables/useUser'
import type { UserResponse } from '@/types/user.types'
import type { ColumnDef } from '@/components/common/table/types/table'

const router = useRouter()

const {
    users,
    fieldErrors,
    globalErrors,
    currentPage,
    totalPages,
    totalItems,
    search,
    clearFilters,
    goToPage,
    fetchList,
} = useUser()

// ── Search & Filter State ─────────────────────────────────────────────────────
const searchName = ref('')
const searchEmail = ref('')
const filterRole = ref('')
const filterStatus = ref('')

const hasActiveFilters = computed(() =>
    searchName.value !== '' ||
    searchEmail.value !== '' ||
    filterRole.value !== '' ||
    filterStatus.value !== ''
)

function buildFilter() {
    return {
        fullName: searchName.value || undefined,
        email: searchEmail.value || undefined,
        role: filterRole.value || undefined,
        status: filterStatus.value || undefined,
    }
}

async function applySearch() {
    await search(buildFilter())
}

async function clearAllFilters() {
    searchName.value = ''
    searchEmail.value = ''
    filterRole.value = ''
    filterStatus.value = ''
    await clearFilters()
}

// ── Column Definitions ────────────────────────────────────────────────────────
const columns: ColumnDef<UserResponse>[] = [
    { key: 'fullName', label: 'Họ tên', type: 'text', readonly: true, width: '220px', required: false },
    { key: 'avatarUrl', label: 'Avatar URL', type: 'text', readonly: true, hideInTable: true, required: false },
    { key: 'email', label: 'Email', type: 'text', readonly: true, width: '240px', required: false },
    { key: 'phoneNumber', label: 'Số điện thoại', type: 'text', readonly: true, width: '150px', required: false },
    {
        key: 'role',
        label: 'Vai trò',
        type: 'enum',
        readonly: true,
        width: '100px',
        required: false,
        options: [
            { value: 'USER', label: 'USER' },
            { value: 'ADMIN', label: 'ADMIN' },
        ],
    },
    {
        key: 'status',
        label: 'Trạng thái',
        type: 'enum',
        readonly: true,
        width: '120px',
        required: false,
        options: [
            { value: 'ACTIVE', label: 'ACTIVE' },
            { value: 'INACTIVE', label: 'INACTIVE' },
            { value: 'BANNED', label: 'BANNED' },
        ],
    },
    { key: 'createdAt', label: 'Ngày tạo', type: 'datetime', readonly: true, width: '140px', required: false },
]

// ── Helper Functions ──────────────────────────────────────────────────────────
function formatDate(iso: string | null | undefined): string {
    if (!iso) return '—'
    try {
        return new Date(iso).toLocaleDateString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        })
    } catch {
        return iso
    }
}

const AVATAR_COLORS = [
    '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#f97316',
    '#eab308', '#22c55e', '#14b8a6', '#06b6d4', '#3b82f6'
]

function getAvatarColor(name: string | null | undefined): string {
    if (!name) return '#64748b'
    const index = name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
    return AVATAR_COLORS[index % AVATAR_COLORS.length]
}

function getInitials(name: string | null | undefined): string {
    if (!name) return '??'
    const parts = name.trim().split(/\s+/)
    if (parts.length >= 2) {
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    }
    return name.slice(0, 2).toUpperCase()
}

function goToDetail(id: number | string) {
    router.push(`/admin/users/${id}`)
}

// ── Smart Pagination Logic ────────────────────────────────────────────────────
const visiblePages = computed(() => {
    const total = totalPages.value
    const current = currentPage.value
    const maxVisible = 7

    if (total <= maxVisible) {
        return Array.from({ length: total }, (_, i) => i)
    }

    let start = Math.max(0, current - Math.floor(maxVisible / 2))
    let end = start + maxVisible - 1

    if (end >= total) {
        end = total - 1
        start = Math.max(0, end - maxVisible + 1)
    }

    return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

onMounted(() => fetchList(0))
</script>