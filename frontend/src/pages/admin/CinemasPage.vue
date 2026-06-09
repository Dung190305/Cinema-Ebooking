<template>
    <div class="flex flex-col gap-6 py-6">
        <div class="flex flex-col gap-2 pr-6">
            <!-- Breadcrumb -->
            <div class="flex items-center text-sm">
                <span class="text-text-admin-primary font-medium">
                    Cinemas
                </span>
            </div>

            <!-- Header -->
            <div class="flex items-center justify-between ">
                <div>
                    <h1 class="text-lg font-semibold text-text-admin-primary">
                        Rạp chiếu phim
                    </h1>
                    <p class="text-sm text-text-admin-tertiary">
                        {{ totalItems }} rạp
                    </p>
                </div>
            </div>
        </div>

        <!-- Global errors -->
        <div v-if="globalErrors.length" class="pr-6 rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- Table -->
        <DataTable :rows="cinemas" :columns="columns" createLabel="Thêm rạp" :fieldErrors="fieldErrors"
            @create="showCreate = true" @delete="handleDelete" @save="handleSave">

            <!--
                detail-actions slot: inject "Quản lý phòng" vào footer của DetailPanel.
                { item } là draft hiện tại (RowItem) — có id để navigate.
            -->
            <template #detail-actions="{ item }">
                <button
                    class="flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 py-2.5 text-sm text-slate-600 transition-colors hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900"
                    @click="router.push(`/admin/cinemas/${item.id}/rooms`)">
                    <LayoutGrid class="size-4" />
                    Quản lý phòng
                </button>
            </template>

        </DataTable>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex justify-center gap-1.5 pr-6">
            <button v-for="page in totalPages" :key="page" class="rounded-lg px-3 py-1.5 text-sm transition-colors"
                :class="currentPage === page - 1
                    ? 'bg-accent text-text-on-accent font-medium'
                    : 'text-text-admin-secondary hover:bg-slate-100'" @click="goToPage(page - 1)">
                {{ page }}
            </button>
        </div>

        <!-- Create modal -->
        <CreateModal v-model="showCreate" title="Thêm rạp chiếu phim" :columns="columns" :isLoading="isCreating"
            :fieldErrors="fieldErrors" @submit="handleCreate" />

    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { LayoutGrid } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import { useCinema } from '@/composables/useCinema'
import type { CinemaResponse, CreateCinemaRequest } from '@/types/cinema'
import type { ColumnDef } from '@/components/common/table/types/table'

const router = useRouter()


const {
    cinemas, isLoading, fieldErrors, globalErrors,
    currentPage, totalPages, totalItems,
    fetchList, goToPage, create, save, remove,
} = useCinema()

const showCreate = ref(false)
const isCreating = ref(false)

// ── Column definitions ────────────────────────────────────────────────────────
const columns: ColumnDef<CinemaResponse>[] = [
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
        label: 'Tên rạp',
        type: 'text',
        width: '400px'
    },
    {
        key: 'address',
        label: 'Địa chỉ',
        type: 'textarea',
        hideInTable: true
    },
    {
        key: 'city',
        label: 'Thành phố',
        type: 'text',
        width: '200px'
    },
    {
        key: 'status',
        label: 'Trạng thái',
        type: 'enum',
        options: [
            { value: 'ACTIVE', label: 'Hoạt động' },
            { value: 'INACTIVE', label: 'Không hoạt động' }
        ],
        hideInCreate: true,
        width: 'auto'
    },
]

// ── Handlers ──────────────────────────────────────────────────────────────────
async function handleCreate(draft: Record<string, unknown>) {
    isCreating.value = true
    try {
        const ok = await create(draft as unknown as CreateCinemaRequest)
        if (ok) showCreate.value = false
    } finally {
        isCreating.value = false
    }
}

async function handleSave(item: CinemaResponse, done: () => void) {
    const ok = await save(item)
    if (ok) done()
}

async function handleDelete(item: CinemaResponse) {
    await remove(item)
}

onMounted(() => fetchList(0))
</script>