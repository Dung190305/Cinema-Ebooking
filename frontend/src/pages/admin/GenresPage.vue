<template>
    <div class="flex flex-col gap-6 py-6">
        <div class="flex flex-col gap-2 pr-6">
            <!-- Breadcrumb -->
            <div class="flex items-center text-sm">
                <span class="text-text-admin-primary font-medium">
                    Thể loại
                </span>
            </div>

            <!-- Header -->
            <div class="flex items-center justify-between">
                <div>
                    <h1 class="text-lg font-semibold text-text-admin-primary">
                        Quản lý thể loại phim
                    </h1>
                    <p class="text-sm text-text-admin-tertiary">
                        {{ totalItems }} thể loại
                    </p>
                </div>
            </div>
        </div>

        <!-- Global errors -->
        <div v-if="globalErrors.length" class="pr-6 rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- Table -->
        <DataTable :rows="genres" :columns="columns" createLabel="Thêm thể loại" :fieldErrors="fieldErrors"
            @create="showCreate = true" @delete="handleDelete" @save="handleSave">
            <!-- Không cần slot detail-actions cho genre -->
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
        <CreateModal v-model="showCreate" title="Thêm thể loại phim" :columns="columns" :isLoading="isCreating"
            :fieldErrors="fieldErrors" @submit="handleCreate" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import { useGenre } from '@/composables/useGenre'
import type { GenreResponse, CreateGenreRequest } from '@/types/genre'
import type { ColumnDef } from '@/components/common/table/types/table'

const {
    genres,
    isLoading,
    fieldErrors,
    globalErrors,
    currentPage,
    totalPages,
    totalItems,
    fetchList,
    goToPage,
    create,
    save,
    remove,
} = useGenre()

const showCreate = ref(false)
const isCreating = ref(false)

const columns: ColumnDef<GenreResponse>[] = [
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
        label: 'Tên thể loại',
        type: 'text',
        width: 'auto'
    },
]

// ── Handlers ──────────────────────────────────────────────────────────────────
async function handleCreate(draft: Record<string, unknown>) {
    isCreating.value = true
    try {
        const ok = await create(draft as unknown as CreateGenreRequest)
        if (ok) showCreate.value = false
    } finally {
        isCreating.value = false
    }
}

async function handleSave(item: GenreResponse, done: () => void) {
    const ok = await save(item)
    if (ok) done()
}

async function handleDelete(item: GenreResponse) {
    await remove(item)
}

onMounted(() => fetchList(0))
</script>