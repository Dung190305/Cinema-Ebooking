<template>
    <div class="flex flex-col gap-6 py-6">
        <!-- Thay thế block header hiện tại -->
        <div class="flex flex-col gap-2 pr-6">
            <div class="flex items-center text-sm">
                <span class="text-text-admin-primary font-medium">Phim</span>
            </div>
            <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
                <div>
                    <h1 class="text-lg font-semibold text-text-admin-primary">Quản lý phim</h1>
                    <p class="text-sm text-text-admin-tertiary">{{ totalItems }} phim</p>
                </div>

                <!-- ✅ Filter dropdowns -->
                <div class="flex flex-col gap-2 sm:flex-row sm:items-center">
                    <select v-model="selectedStatus"
                        class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100">
                        <option value="ALL">Tất cả trạng thái</option>
                        <option value="COMING_SOON">Sắp chiếu</option>
                        <option value="NOW_SHOWING">Đang chiếu</option>
                        <option value="ENDED">Kết thúc</option>
                    </select>

                    <select v-model="selectedAgeRating"
                        class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100">
                        <option value="ALL">Tất cả độ tuổi</option>
                        <option value="P">P - Tất cả</option>
                        <option value="T13">T13</option>
                        <option value="T16">T16</option>
                        <option value="T18">T18</option>
                    </select>
                </div>
            </div>
        </div>

        <!-- Global errors -->
        <div v-if="globalErrors.length" class="pr-6 rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- Table -->
        <DataTable :rows="filteredMovies" :columns="allColumns" createLabel="Thêm phim" :fieldErrors="fieldErrors"
            @create="showCreate = true" :showDelete="false" @save="handleSave" />

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex justify-center gap-1.5 pr-6">
            <button v-for="page in totalPages" :key="page" class="rounded-lg px-3 py-1.5 text-sm transition-colors"
                :class="currentPage === page - 1
                    ? 'bg-accent text-text-on-accent font-medium'
                    : 'text-text-admin-secondary hover:bg-slate-100'" @click="goToPage(page - 1)">
                {{ page }}
            </button>
        </div>

        <!-- CreateModal -->
        <CreateModal v-model="showCreate" title="Thêm phim mới" :columns="createColumns" :isLoading="isLoading"
            :fieldErrors="fieldErrors" @submit="handleCreate" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import { useMovie } from '@/composables/useMovie'
import type { MovieResponse, CreateMovieRequest } from '@/types/movie'
import type { ColumnDef } from '@/components/common/table/types/table'

const {
    movies, genresList, isLoading, fieldErrors, globalErrors,
    currentPage, totalPages, totalItems,
    filteredMovies,
    selectedStatus,
    selectedAgeRating,
    fetchList, fetchGenres, goToPage, create, save, remove
} = useMovie()

const showCreate = ref(false)

const baseColumns: ColumnDef<MovieResponse>[] = [
    {
        key: 'id',
        label: 'ID',
        type: 'number',
        readonly: true,
        hideInCreate: true,
        hideInTable: true
    },
    {
        key: 'title',
        label: 'Tiêu đề',
        type: 'text',
        required: true,
        width: '180px'
    },
    {
        key: 'description',
        label: 'Mô tả',
        type: 'textarea',
        hideInTable: true
    },
    {
        key: 'duration',
        label: 'Thời lượng (phút)',
        type: 'number',
        required: true,
        width: '100px'
    },
    {
        key: 'ageRating',
        label: 'Độ tuổi',
        type: 'enum',
        options: [
            { value: 'P', label: 'P - Tất cả' },
            { value: 'T13', label: 'T13' },
            { value: 'T16', label: 'T16' },
            { value: 'T18', label: 'T18' }
        ],
        required: true,
        width: '120px'
    },
    {
        key: 'releaseDate',
        label: 'Ngày phát hành',
        type: 'date',
        required: true,
        width: '120px',
        futureOnly: false
    },
    {
        key: 'showingEndDate',
        label: 'Ngày kết thúc chiếu',
        type: 'date',
        required: false,          // nullable — phim đang chiếu chưa có end date
        width: '130px',
        futureOnly: false,
        hideInCreate: false       // cho phép nhập khi tạo phim
    },
    {
        key: 'status',
        label: 'Trạng thái',
        type: 'enum',
        options: [
            { value: 'COMING_SOON', label: 'Sắp chiếu' },
            { value: 'NOW_SHOWING', label: 'Đang chiếu' },
            { value: 'ENDED', label: 'Kết thúc' }
        ],
        hideInCreate: true,
        readonly: true,
        width: '120px'
    },
    {
        key: 'posterUrl',
        label: 'Poster URL',
        type: 'text',
        hideInTable: true
    },
    {
        key: 'bannerUrl',
        label: 'Banner URL',
        type: 'text',
        hideInTable: true
    },
    {
        key: 'director',
        label: 'Đạo diễn',
        type: 'text',
        width: '160px'
    },
    {
        key: 'actors',
        label: 'Diễn viên',
        type: 'text',
        hideInTable: true
    },
]
// Cột genres – không bao giờ trả về null, chỉ có options rỗng khi chưa load
const genresColumn = computed<ColumnDef<MovieResponse>>(() => ({
    key: 'genres',
    label: 'Thể loại',
    type: 'multiselect',
    options: genresList.value.map(g => ({ value: g.id, label: g.name })),
    required: true,
}))

const allColumns = computed(() => {
    const cols = [...baseColumns]
    cols.push(genresColumn.value)
    return cols
})

const createColumns = computed(() => {
    let cols = baseColumns.filter(c => !c.hideInCreate && !c.readonly)
    cols.push({ ...genresColumn.value, readonly: false })
    return cols
})

// ========== Handlers ==========
async function handleCreate(draft: Record<string, unknown>) {
    // draft.genres là number[] (do FieldRenderer checkbox trả về)
    const payload: CreateMovieRequest = {
        title: draft.title as string,
        description: draft.description as string,
        duration: Number(draft.duration),
        ageRating: draft.ageRating as any,
        releaseDate: draft.releaseDate as string,
        showingEndDate: (draft.showingEndDate as string) || null,
        posterUrl: draft.posterUrl as string,
        bannerUrl: draft.bannerUrl as string,
        director: draft.director as string,
        actors: draft.actors as string,
        genreIds: (draft.genres as number[]) || []
    }
    const ok = await create(payload)
    if (ok) showCreate.value = false
}

async function handleSave(item: MovieResponse, done: () => void) {
    const ok = await save(item as any)
    if (ok) done()
}

// ========== Lifecycle ==========
onMounted(async () => {
    await fetchGenres()   // BẮT BUỘC chạy trước để genresList có dữ liệu
    await fetchList(0)    // Sau đó mới fetch movies
})
</script>