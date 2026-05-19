<template>
    <div class="flex flex-col gap-6 py-6">
        <div class="flex flex-col gap-2 pr-6">
            <div class="flex items-center text-sm">
                <span class="font-medium text-text-admin-primary">Promotions</span>
            </div>

            <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
                <div>
                    <h1 class="text-lg font-semibold text-text-admin-primary">Combo</h1>
                    <p class="text-sm text-text-admin-tertiary">{{ totalItems }} combo</p>
                </div>

                <div class="flex flex-col gap-2 sm:flex-row sm:items-center">
                    <input v-model="searchQuery" type="text" placeholder="Tìm kiếm theo tên..."
                        class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100" />

                    <select v-model="selectedStatus"
                        class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100">
                        <option value="ALL">Tất cả trạng thái</option>
                        <option value="ACTIVE">Đang hoạt động</option>
                        <option value="INACTIVE">Tạm ngưng</option>
                        <option value="OUT_OF_STOCK">Hết hàng</option>
                    </select>
                </div>
            </div>
        </div>

        <div v-if="globalErrors.length" class="mr-6 rounded-lg border border-red-100 bg-red-50 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <div v-if="isLoading && !combos.length" class="space-y-2 pr-6">
            <div v-for="i in 5" :key="i" class="h-12 animate-pulse rounded-xl bg-slate-100" />
        </div>

        <DataTable :rows="filteredCombos" :columns="columns" createLabel="Thêm combo" :fieldErrors="fieldErrors"
            :showDelete="true" @create="showCreate = true" @delete="handleDeleteRequest" @save="handleSave">
            <template #detail-actions="{ item, close }">
                <button v-if="canActivate(item)"
                    class="flex w-full items-center justify-center gap-2 rounded-lg border border-emerald-200 py-2.5 text-sm font-medium text-emerald-700 transition-colors hover:border-emerald-300 hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-50"
                    :disabled="actionComboId === item.id" @click="handleActivate(item, close)">
                    <Power class="size-4" />
                    Kích hoạt combo
                </button>

                <button v-if="canDeactivate(item)"
                    class="flex w-full items-center justify-center gap-2 rounded-lg border border-rose-200 py-2.5 text-sm font-medium text-rose-700 transition-colors hover:border-rose-300 hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-50"
                    :disabled="actionComboId === item.id" @click="handleDeactivate(item, close)">
                    <CircleOff class="size-4" />
                    Vô hiệu hóa combo
                </button>
            </template>
        </DataTable>

        <CreateModal v-model="showCreate" title="Thêm combo" submitLabel="Tạo" size="lg" :columns="columns"
            :isLoading="isLoading" :fieldErrors="fieldErrors" @submit="handleCreate" />

        <ConfirmDialog v-model="showActionConfirm" :title="confirmActionTitle" :description="confirmActionDescription"
            :confirmLabel="confirmActionLabel" :danger="confirmAction.type === 'delete'"
            @confirm="handleConfirmAction" />

        <div v-if="totalPages > 1" class="flex justify-center gap-1.5 pr-6">
            <button v-for="page in totalPages" :key="page" class="rounded-lg px-3 py-1.5 text-sm transition-colors"
                :class="currentPage === page - 1
                    ? 'bg-accent font-medium text-text-on-accent'
                    : 'text-text-admin-secondary hover:bg-slate-100'
                    " @click="goToPage(page - 1)">
                {{ page }}
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Power, CircleOff, Trash2 } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import ConfirmDialog from '@/components/common/table/subcomponents/ConfirmDialog.vue'
import { useCombo } from '@/composables/useCombo'
import type { ColumnDef } from '@/components/common/table/types/table'
import type { ComboResponse, CreateComboRequest } from '@/types/combo.types'

const {
    combos,
    filteredCombos,
    isLoading,
    fieldErrors,
    globalErrors,
    currentPage,
    totalPages,
    totalItems,
    searchQuery,
    selectedStatus,
    fetchList,
    goToPage,
    create,
    update,
    deleteItem,
    activate,
    deactivate,
} = useCombo()

const showCreate = ref(false)
const actionComboId = ref<number | null>(null)

// Cột cho DataTable
const columns = computed<ColumnDef[]>(() => [
    {
        key: 'id',
        label: 'ID',
        type: 'number',
        readonly: true,
        hideInCreate: true,
        hideInTable: true,
    },
    {
        key: 'name',
        label: 'Tên combo',
        type: 'text',
        width: '200px',
    },
    {
        key: 'description',
        label: 'Mô tả',
        type: 'text', // Có thể dùng textarea trong DataTable nếu hỗ trợ
        hideInTable: true,
    },
    {
        key: 'price',
        label: 'Giá bán',
        type: 'currency',
        width: '120px',
    },
    {
        key: 'originalPrice',
        label: 'Giá gốc',
        type: 'currency',
        readonlyInEdit: true,
        hideInTable: true,
    },
    {
        key: 'stock',
        label: 'Tồn kho',
        type: 'number',
        width: '100px',
    },
    {
        key: 'imageUrl',
        label: 'Link Ảnh',
        type: 'text', // Hoặc có thể custom render
        hideInTable: true,
    },
    {
        key: 'status',
        label: 'Trạng thái',
        type: 'enum',
        width: '150px',
        readonly: true,
        hideInCreate: true,
        options: [
            { value: 'ACTIVE', label: 'Đang hoạt động' },
            { value: 'INACTIVE', label: 'Tạm ngưng' },
            { value: 'OUT_OF_STOCK', label: 'Hết hàng' },
        ],
    },
])

// ─── Xử lý tạo mới ──────────────────────────────────────────────
async function handleCreate(draft: Record<string, unknown>) {
    const payload: CreateComboRequest = {
        name: String(draft.name || '').trim(),
        description: String(draft.description || '').trim(),
        price: Number(draft.price || 0),
        originalPrice: Number(draft.originalPrice || 0),
        stock: Number(draft.stock || 0),
        imageUrl: String(draft.imageUrl || '').trim(),
    }
    const ok = await create(payload)
    if (ok) showCreate.value = false
}

// ─── Xử lý lưu chỉnh sửa (inline edit) ────────────────────────────
async function handleSave(item: ComboResponse, done: () => void) {
    const ok = await update(item)
    if (ok) done()
}

// ─── Xác nhận hành động (activate / deactivate / delete) ──────────
const confirmAction = ref<{
    type: 'activate' | 'deactivate' | 'delete'
    item: ComboResponse | null
    close?: () => void
}>({
    type: 'activate',
    item: null,
})
const showActionConfirm = ref(false)

const confirmActionTitle = computed(() => {
    switch (confirmAction.value.type) {
        case 'activate': return 'Kích hoạt combo'
        case 'deactivate': return 'Vô hiệu hóa combo'
        case 'delete': return 'Xóa combo'
        default: return ''
    }
})
const confirmActionDescription = computed(() => {
    switch (confirmAction.value.type) {
        case 'activate': return 'Bạn có chắc muốn kích hoạt combo này?'
        case 'deactivate': return 'Bạn có chắc muốn vô hiệu hóa combo này?'
        case 'delete': return 'Bạn có chắc muốn xóa vĩnh viễn combo này? Hành động không thể hoàn tác.'
        default: return ''
    }
})
const confirmActionLabel = computed(() => {
    switch (confirmAction.value.type) {
        case 'activate': return 'Kích hoạt'
        case 'deactivate': return 'Vô hiệu'
        case 'delete': return 'Xóa'
        default: return ''
    }
})

function canActivate(item: Record<string, unknown>) {
    const status = (item as ComboResponse).displayStatus
    return status === 'INACTIVE'
}
function canDeactivate(item: Record<string, unknown>) {
    const status = (item as ComboResponse).displayStatus
    return status === 'ACTIVE'
}

function handleActivate(item: Record<string, unknown>, close: () => void) {
    confirmAction.value = { type: 'activate', item: item as ComboResponse, close }
    showActionConfirm.value = true
}
function handleDeactivate(item: Record<string, unknown>, close: () => void) {
    confirmAction.value = { type: 'deactivate', item: item as ComboResponse, close }
    showActionConfirm.value = true
}
function handleDeleteRequest(item: Record<string, unknown>, close?: () => void) {
    confirmAction.value = { type: 'delete', item: item as ComboResponse, close }
    showActionConfirm.value = true
}

async function handleConfirmAction() {
    const action = confirmAction.value
    const combo = action.item
    if (!combo) return

    actionComboId.value = combo.id

    try {
        let ok = false
        if (action.type === 'activate') {
            const updated = await activate(combo)
            ok = !!updated
            if (updated) Object.assign(combo, updated)
        } else if (action.type === 'deactivate') {
            const updated = await deactivate(combo)
            ok = !!updated
            if (updated) Object.assign(combo, updated)
        } else if (action.type === 'delete') {
            ok = await deleteItem(combo.id)
        }

        if (ok) {
            action.close?.()
        }
    } finally {
        actionComboId.value = null
    }
}

onMounted(() => fetchList(0))
</script>