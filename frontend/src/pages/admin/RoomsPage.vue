<template>
    <div class="flex flex-col gap-6 py-6">

        <div class="flex flex-col gap-2 pr-6">
            <!-- Breadcrumb -->
            <div class="flex items-center gap-2 text-sm">
                <button class="text-text-admin-tertiary transition-colors hover:text-text-admin-primary"
                    @click="router.push('/admin/cinemas')">
                    Cinemas
                </button>
                <ChevronRight class="size-3.5 text-text-admin-tertiary" />
                <span class="text-text-admin-primary font-medium">{{ cinemaName }}</span>
                <ChevronRight class="size-3.5 text-text-admin-tertiary" />
                <span class="text-text-admin-tertiary">Rooms</span>
            </div>

            <!-- Header -->
            <div class="flex items-center justify-between">
                <div>
                    <h1 class="text-lg font-semibold text-text-admin-primary">
                        Phòng chiếu
                    </h1>
                    <p class="text-sm text-text-admin-tertiary">
                        {{ totalItems }} phòng
                    </p>
                </div>
            </div>
        </div>

        <!-- Global errors -->
        <div v-if="globalErrors.length" class="pr-6 rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- Loading skeleton (lần đầu fetch) -->
        <div v-if="isLoading && !rooms.length" class="pr-6 space-y-2">
            <div v-for="i in 5" :key="i" class="h-12 animate-pulse rounded-xl bg-slate-100" />
        </div>

        <!-- Table -->
        <DataTable :rows="rooms" :columns="columns" createLabel="Thêm phòng" :fieldErrors="fieldErrors"
            @create="showCreate = true" :show-delete="false" @save="handleSave" @row-select="selectedRoom = $event">

            <template #detail-actions="{ item }">
                <button
                    class="flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 py-2.5 text-sm text-slate-600 transition-colors hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900"
                    @click="router.push(`/admin/cinemas/${cinemaId}/rooms/${item.id}/layout`)">
                    <LayoutGrid class="size-4" />
                    Quản lý layout
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
        <CreateModal v-model="showCreate" title="Thêm phòng chiếu" submitLabel="Tạo phòng" :columns="columns"
            :isLoading="isCreating" :fieldErrors="fieldErrors" @submit="handleCreate" />

        <!--
            TODO: Layout modal
            <RoomLayoutModal v-model="showLayoutModal" :room="selectedRoom" />
        -->

    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronRight, LayoutGrid } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import { useRoom } from '@/composables/useRoom'
import { cinemaApi } from '@/api/cinema.api'
import type { RoomResponse, CreateRoomRequest } from '@/types/room'
import type { ColumnDef } from '@/components/common/table/types/table'

// ── Route context ─────────────────────────────────────────────────────────────
const route = useRoute()
const router = useRouter()
const cinemaId = Number(route.params.cinemaId)

// ── Cinema name cho breadcrumb ────────────────────────────────────────────────
const cinemaName = ref('...')
cinemaApi.getById(cinemaId)
    .then((cinema) => { cinemaName.value = cinema.name })
    .catch(() => { cinemaName.value = `Cinema #${cinemaId}` })

// ── Composable ────────────────────────────────────────────────────────────────
const {
    rooms, isLoading, fieldErrors, globalErrors,
    currentPage, totalPages, totalItems,
    fetchList, goToPage, create, save, remove,
} = useRoom(cinemaId)

// ── UI state ──────────────────────────────────────────────────────────────────
const showCreate = ref(false)
const isCreating = ref(false)


const columns: ColumnDef<RoomResponse>[] = [
    {
        key: 'id',
        label: 'ID',
        type: 'number',
        readonly: true,
        hideInCreate: true,
        hideInTable: true
    },
    {
        key: 'name',
        label: 'Tên phòng',
        type: 'text',
        width: '400px'
    },
    {
        key: 'roomType',
        label: 'Loại phòng',
        type: 'enum',
        readonlyInEdit: true,
        options: [
            { value: 'TYPE_2D', label: '2D' },
            { value: 'TYPE_3D', label: '3D' },
            { value: 'IMAX', label: 'IMAX' }
        ],
        width: '200px'
    },

    {
        key: 'numberOfRows',
        label: 'Số hàng',
        type: 'number',
        readonlyInEdit: true,
        hideInTable: true
    },
    {
        key: 'numberOfCols',
        label: 'Số cột',
        type: 'number',
        readonlyInEdit: true,
        hideInTable: true
    },
    {
        key: 'totalSeats',
        label: 'Tổng ghế',
        type: 'number',
        readonly: true,
        hideInCreate: true,
        width: '200px'
    },
    {
        key: 'status',
        label: 'Trạng thái',
        type: 'enum',
        options: [
            { value: 'ACTIVE', label: 'Hoạt động' },
            { value: 'INACTIVE', label: 'Không hoạt động' },
            { value: 'MAINTENANCE', label: 'Bảo trì' }
        ],
        hideInCreate: true,
        width: 'auto'
    },
    {
        key: 'cinemaId',
        label: 'Cinema ID',
        type: 'number',
        readonly: true,
        hideInCreate: true,
        hideInTable: true
    },
]

// ── Handlers ──────────────────────────────────────────────────────────────────
async function handleCreate(draft: Record<string, unknown>) {
    isCreating.value = true
    try {
        const ok = await create({
            name: String(draft.name ?? ''),
            roomType: draft.roomType as CreateRoomRequest['roomType'],
            numberOfRows: Number(draft.numberOfRows),
            numberOfCols: Number(draft.numberOfCols),
        })
        if (ok) showCreate.value = false
    } finally {
        isCreating.value = false
    }
}

async function handleSave(item: RoomResponse, done: () => void) {
    const ok = await save(item)
    if (ok) done()
}

async function handleDelete(item: RoomResponse) {
    await remove(item)
}


onMounted(() => fetchList(0))
</script>