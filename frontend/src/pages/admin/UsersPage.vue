<template>
    <div class="flex flex-col gap-6 py-6">
        <div class="flex flex-col gap-2 pr-6">
            <!-- Breadcrumb -->
            <div class="flex items-center text-sm">
                <span class="text-text-admin-primary font-medium">Users</span>
            </div>

            <!-- Header -->
            <div class="flex items-center justify-between">
                <div>
                    <h1 class="text-lg font-semibold text-text-admin-primary">
                        Người dùng
                    </h1>
                    <p class="text-sm text-text-admin-tertiary">
                        {{ totalItems }} người dùng
                    </p>
                </div>
            </div>
        </div>

        <!-- Global errors -->
        <div v-if="globalErrors.length" class="pr-6 rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- ── Search + Filter Bar ─────────────────────────────────────────── -->
        <div class="pr-6 flex flex-col gap-3">
            <div class="flex items-center gap-3">
                <!-- Search inputs -->
                <div class="relative flex-1">
                    <Search class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-slate-400 pointer-events-none" />
                    <input
                        v-model="searchName"
                        type="text"
                        placeholder="Tìm theo tên..."
                        class="w-full rounded-lg border border-border-admin-subtle bg-white pl-9 pr-3 py-2 text-sm text-slate-900 outline-none transition focus:border-accent focus:ring-1 focus:ring-slate-100 placeholder:text-slate-400"
                        @keyup.enter="applySearch"
                    />
                </div>

                <div class="relative flex-1">
                    <Mail class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-slate-400 pointer-events-none" />
                    <input
                        v-model="searchEmail"
                        type="email"
                        placeholder="Tìm theo email..."
                        class="w-full rounded-lg border border-border-admin-subtle bg-white pl-9 pr-3 py-2 text-sm text-slate-900 outline-none transition focus:border-accent focus:ring-1 focus:ring-slate-100 placeholder:text-slate-400"
                        @keyup.enter="applySearch"
                    />
                </div>

                <!-- Role filter -->
                <select
                    v-model="filterRole"
                    class="rounded-lg border border-border-admin-subtle bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-accent focus:ring-1 focus:ring-slate-100 cursor-pointer"
                >
                    <option value="">Tất cả vai trò</option>
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                </select>

                <!-- Status filter -->
                <select
                    v-model="filterStatus"
                    class="rounded-lg border border-border-admin-subtle bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-accent focus:ring-1 focus:ring-slate-100 cursor-pointer"
                >
                    <option value="">Tất cả trạng thái</option>
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="INACTIVE">INACTIVE</option>
                </select>

                <!-- Search button -->
                <button
                    class="flex items-center gap-1.5 rounded-lg bg-accent px-4 py-2 text-sm font-medium text-text-on-accent transition-colors hover:bg-accent/90"
                    @click="applySearch"
                >
                    <Search class="size-4" />
                    Tìm kiếm
                </button>

                <!-- Clear filters -->
                <button
                    v-if="hasActiveFilters"
                    class="flex items-center gap-1.5 rounded-lg border border-slate-200 px-4 py-2 text-sm text-slate-600 transition-colors hover:bg-slate-50"
                    @click="clearAllFilters"
                >
                    <X class="size-4" />
                    Xóa lọc
                </button>
            </div>
        </div>

        <!-- ── Table ──────────────────────────────────────────────────────── -->
        <DataTable
            :rows="users"
            :columns="columns"
            :fieldErrors="fieldErrors"
            :showDelete="false"
            :showCreate="false"
            :showSave="false"
            @save="() => {}"
        >
            <!-- Avatar cell -->
            <template #cell-avatarUrl="{ value, item }">
                <div class="flex items-center gap-3">
                    <div
                        class="flex size-8 shrink-0 items-center justify-center rounded-full text-xs font-semibold text-white"
                        :style="{ backgroundColor: getAvatarColor(String(item.fullName)) }"
                    >
                        {{ getInitials(String(item.fullName)) }}
                    </div>
                    <span class="truncate text-sm font-medium text-slate-900">{{ item.fullName }}</span>
                </div>
            </template>

            <!-- Phone number cell -->
            <template #cell-phoneNumber="{ value }">
                <span class="text-sm text-slate-600">{{ value || '—' }}</span>
            </template>

            <!-- Role cell -->
            <template #cell-role="{ value }">
                <span
                    class="inline-block rounded-full px-2.5 py-0.5 text-xs font-medium"
                    :class="value === 'ADMIN'
                        ? 'bg-purple-100 text-purple-700'
                        : 'bg-blue-100 text-blue-700'"
                >
                    {{ value }}
                </span>
            </template>

            <!-- Status cell -->
            <template #cell-status="{ value }">
                <span
                    class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium"
                    :class="value === 'ACTIVE'
                        ? 'bg-green-100 text-green-700'
                        : value === 'INACTIVE'
                        ? 'bg-gray-100 text-gray-500'
                        : 'bg-red-100 text-red-700'"
                >
                    <span class="size-1.5 rounded-full" :class="value === 'ACTIVE' ? 'bg-green-500' : value === 'INACTIVE' ? 'bg-gray-400' : 'bg-red-500'" />
                    {{ value }}
                </span>
            </template>

            <!-- CreatedAt cell -->
            <template #cell-createdAt="{ value }">
                <span class="text-sm text-slate-600">{{ formatDate(String(value)) }}</span>
            </template>

            <!-- Detail actions -->
            <template #detail-actions="{ item, close }">
                <button
                    class="flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 py-2.5 text-sm text-slate-600 transition-colors hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900"
                    @click="goToDetail(item.id)"
                >
                    <Eye class="size-4" />
                    Xem chi tiết
                </button>
            </template>
        </DataTable>

        <!-- ── Pagination ─────────────────────────────────────────────────── -->
        <div v-if="totalPages > 1" class="flex justify-center gap-1.5 pr-6">
            <button
                class="flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm transition-colors"
                :class="currentPage === 0
                    ? 'text-slate-300 cursor-not-allowed'
                    : 'text-text-admin-secondary hover:bg-slate-100'"
                :disabled="currentPage === 0"
                @click="goToPage(currentPage - 1)"
            >
                <ChevronLeft class="size-4" />
            </button>

            <button
                v-for="page in visiblePages"
                :key="page"
                class="rounded-lg px-3 py-1.5 text-sm transition-colors"
                :class="page === currentPage
                    ? 'bg-accent text-text-on-accent font-medium'
                    : 'text-text-admin-secondary hover:bg-slate-100'"
                @click="goToPage(page)"
            >
                {{ page + 1 }}
            </button>

            <button
                class="flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm transition-colors"
                :class="currentPage === totalPages - 1
                    ? 'text-slate-300 cursor-not-allowed'
                    : 'text-text-admin-secondary hover:bg-slate-100'"
                :disabled="currentPage === totalPages - 1"
                @click="goToPage(currentPage + 1)"
            >
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
    isLoading,
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

// ── Search + Filter state ─────────────────────────────────────────────────────
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
        role: (filterRole.value as any) || undefined,
        status: (filterStatus.value as any) || undefined,
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

// ── Column definitions ────────────────────────────────────────────────────────
const columns: ColumnDef<UserResponse>[] = [
    {
        key: 'avatarUrl',
        label: 'Họ tên',
        type: 'text',
        width: '220px',
    },
    {
        key: 'email',
        label: 'Email',
        type: 'text',
        width: '240px',
    },
    {
        key: 'phoneNumber',
        label: 'Số điện thoại',
        type: 'text',
        width: '150px',
    },
    {
        key: 'role',
        label: 'Vai trò',
        type: 'enum',
        width: '100px',
    },
    {
        key: 'status',
        label: 'Trạng thái',
        type: 'enum',
        width: '110px',
    },
    {
        key: 'createdAt',
        label: 'Ngày tạo',
        type: 'text',
        width: '140px',
    },
]

// ── Helpers ───────────────────────────────────────────────────────────────────
function formatDate(iso: string): string {
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
    '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e',
    '#f97316', '#eab308', '#22c55e', '#14b8a6',
    '#06b6d4', '#3b82f6', '#a855f7', '#f59e0b',
]

function getAvatarColor(name: string): string {
    const index = name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
    return AVATAR_COLORS[index % AVATAR_COLORS.length]
}

function getInitials(name: string): string {
    const parts = name.trim().split(/\s+/)
    if (parts.length >= 2) {
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    }
    return name.slice(0, 2).toUpperCase()
}

function goToDetail(id: number | string) {
    router.push(`/admin/users/${id}`)
}

// ── Visible page buttons (show max 7) ────────────────────────────────────────
const visiblePages = computed(() => {
    const total = totalPages.value
    const cur = currentPage.value
    if (total <= 7) {
        return Array.from({ length: total }, (_, i) => i)
    }
    if (cur < 4) {
        return [0, 1, 2, 3, 4, 5, total - 1]
    }
    if (cur > total - 5) {
        return [0, total - 6, total - 5, total - 4, total - 3, total - 2, total - 1]
    }
    return [0, cur - 2, cur - 1, cur, cur + 1, cur + 2, total - 1]
})

onMounted(() => fetchList(0))
</script>
