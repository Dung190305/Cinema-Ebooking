<template>
    <div class="flex flex-col gap-6 py-6">
        <!-- Header -->
        <div class="flex items-center justify-between gap-4 pr-6">
            <div>
                <h1 class="text-lg font-semibold text-text-admin-primary">Quản lý suất chiếu</h1>
                <p class="text-sm text-text-admin-tertiary">{{ totalItems }} suất chiếu</p>
            </div>
            <!-- Bộ lọc -->

            <div class="flex items-center gap-3">
                <select v-model="selectedCinemaId"
                    class="rounded-lg border border-border-admin-default px-3 py-2 text-sm text-text-admin-primary">
                    <option v-for="cinema in cinemaOptions" :key="cinema.value" :value="cinema.value">
                        {{ cinema.label }}
                    </option>
                </select>
                <select v-model="selectedRoomId"
                    class="rounded-lg border border-border-admin-default px-3 py-2 text-sm text-text-admin-primary"
                    @change="applyFilter">
                    <option :value="undefined">Tất cả phòng</option>
                    <option v-for="room in roomOptions" :key="room.value" :value="room.value">
                        {{ room.label }}
                    </option>
                </select>

                <select v-model="selectedStatus"
                    class="rounded-lg border border-border-admin-default px-3 py-2 text-sm text-text-admin-primary"
                    @change="applyFilter">
                    <option :value="undefined">Tất cả trạng thái</option>
                    <option value="SCHEDULED">Sắp chiếu</option>
                    <option value="ONGOING">Đang chiếu</option>
                    <option value="FINISHED">Đã kết thúc</option>
                    <option value="CANCELLED">Đã hủy</option>
                </select>
            </div>
        </div>

        <div v-if="!isLoadingCinemas && cinemaOptions.length === 0"
            class="pr-6 rounded-lg bg-yellow-50 border border-yellow-100 p-4">
            <p class="text-sm text-yellow-700">Chưa có rạp nào trong hệ thống. Vui lòng tạo rạp trước khi quản lý suất
                chiếu.</p>
        </div>

        <!-- Global errors -->
        <div v-if="globalErrors.length" class="pr-6 rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- Loading skeleton -->
        <div v-if="isLoading && !showtimes.length" class="pr-6 space-y-2">
            <div v-for="i in 5" :key="i" class="h-12 animate-pulse rounded-xl bg-slate-100" />
        </div>

        <!-- Data Table -->
        <DataTable :rows="showtimes" :columns="columns" createLabel="Thêm suất chiếu" :fieldErrors="fieldErrors"
            @create="openCreateModal" :showCreate="canCreate" :showDelete="false" :showSave="false">

            <template #detail-actions="{ item }">
                <button v-if="item.status !== 'CANCELLED' && item.status !== 'FINISHED'"
                    class="flex w-full items-center justify-center gap-2 rounded-lg border border-red-200 py-2.5 text-sm text-red-600 hover:bg-red-50"
                    @click="handleCancel(item)">
                    <XCircle class="size-4" />
                    Hủy suất chiếu
                </button>
                <button
                    class="flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 py-2.5 text-sm text-slate-600 hover:bg-slate-50"
                    @click="viewSeatMap(item)">
                    <Armchair class="size-4" />
                    Xem sơ đồ ghế
                </button>
            </template>

            <!-- Custom cell for status (show badge) -->
            <template #cell-status="{ value }">
                <span class="inline-flex rounded-full px-2 py-0.5 text-xs font-medium" :class="{
                    'bg-yellow-100 text-yellow-800': value === 'SCHEDULED',
                    'bg-blue-100 text-blue-800': value === 'ONGOING',
                    'bg-green-100 text-green-800': value === 'FINISHED',
                    'bg-red-100 text-red-800': value === 'CANCELLED',
                }">
                    {{ statusLabel(value) }}
                </span>
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

        <!-- Create Modal -->
        <CreateModal v-model="showCreateModal" title="Thêm suất chiếu" submitLabel="Tạo suất chiếu" :columns="columns"
            :isLoading="isLoading" :fieldErrors="fieldErrors" size="xl" :onFieldBlur="onFieldBlur"
            @submit="handleCreate">
            <template #extra="{ draft }">
                <SeatMapPreview :roomId="draft?.roomId ? Number(draft.roomId) : null"
                    :startDate="draft?.startTime ? String(draft.startTime) : undefined" />
            </template>
        </CreateModal>

        <SeatMapDialog v-model:isOpen="isSeatMapOpen" :showtimeId="seatMapShowtimeId" />

    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, readonly } from 'vue';
import { useRoute } from 'vue-router'
import { Armchair, XCircle } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import { useShowtime } from '@/composables/useShowtime'
import { cinemaApi } from '@/api/cinema.api'
import type { ShowtimeResponse, CreateShowtimeRequest } from '@/types/showtime'
import type { ColumnDef } from '@/components/common/table/types/table'
import SeatMapPreview from '@/components/showtime/SeatMapPreview.vue'
import SeatMapDialog from '@/components/showtime/SeatMapDialog.vue';
import { dateToISOString, parseISODate } from '@/utils/dateFormat'


const route = useRoute()
const initialCinemaId = route.params.cinemaId ? Number(route.params.cinemaId) : null
const selectedCinemaId = ref<number | null>(isNaN(initialCinemaId) ? null : initialCinemaId)

const selectedRoomId = ref<number | undefined>(undefined)
const selectedStatus = ref<string | undefined>(undefined)

const roomOptions = ref<{ value: number; label: string }[]>([])
const cinemaOptions = ref<{ value: number; label: string }[]>([])

const showCreateModal = ref(false)
const isLoadingCinemas = ref(true)

const seatMapShowtimeId = ref<number | null>(null)
const isSeatMapOpen = ref(false)

const {
    showtimes, isLoading, fieldErrors, globalErrors,
    currentPage, totalPages, totalItems,
    fetchList, goToPage, setFilters, create, save, cancel,
    loadMovies, loadRooms, loadRoomsByFormat
} = useShowtime(selectedCinemaId)


const canCreate = computed(() => cinemaOptions.value.length > 0 && selectedCinemaId.value != null)

async function loadCinemaOptions() {
    try {
        const res = await cinemaApi.getList({ page: 0, size: 50 })
        cinemaOptions.value = res.content.map(c => ({ value: c.id, label: c.name }))

        if (cinemaOptions.value.length === 0) return

        // Xác định rạp ban đầu: từ route params hoặc rạp đầu tiên
        const routeCinemaId = route.params.cinemaId ? Number(route.params.cinemaId) : null
        const defaultCinema = (routeCinemaId && cinemaOptions.value.some(c => c.value === routeCinemaId))
            ? routeCinemaId
            : cinemaOptions.value[0].value

        selectedCinemaId.value = defaultCinema
    } catch {
        cinemaOptions.value = []
    } finally {
        isLoadingCinemas.value = false
    }
}

watch(selectedCinemaId, async (newId) => {
    if (newId && !isNaN(newId)) {
        const rooms = await loadRooms(newId) // loadRooms nhận tham số cid
        roomOptions.value = rooms.map(r => ({ value: r.id, label: r.label }))
        selectedRoomId.value = undefined
        setFilters(selectedRoomId.value, selectedStatus.value)
    } else {
        roomOptions.value = []
        selectedRoomId.value = undefined
        // Gọi fetch lại với cinemaId = null (tuỳ composable, thường sẽ không gửi tham số cinemaId)
        setFilters(undefined, selectedStatus.value)
    }
})


function applyFilter() {
    setFilters(selectedRoomId.value, selectedStatus.value)
}

// Các options
const formatStaticOptions = [
    { value: 1, label: '2D' },
    { value: 2, label: '3D' },
    { value: 3, label: 'IMAX' },
]

const languageOptions = [
    { value: 'EN', label: 'English' },
    { value: 'VI', label: 'Vietnamese' },
    { value: 'JA', label: 'Japanese' },
    { value: 'KO', label: 'Korean' },
]

// Định nghĩa columns (sửa key 'id')
const columns: ColumnDef<ShowtimeResponse>[] = [
    { key: 'id', label: 'ID', type: 'number', readonly: true, hideInTable: true, hideInCreate: true, hideInEdit: true },
    {
        key: 'movieId',
        label: 'Phim',
        type: 'relation',
        readonlyInEdit: true,
        optionsLoader: loadMovies,  // dùng trực tiếp từ composable
        width: '200px'
    },
    {
        key: 'formatId',
        label: 'Định dạng',
        type: 'enum',
        readonlyInEdit: true,
        options: formatStaticOptions,
        width: '100px'
    },
    {
        key: 'roomId',
        label: 'Phòng',
        type: 'relation',
        readonlyInEdit: true,
        optionsLoader: loadRooms,
        dependentLoader: loadRoomsByFormat,          // dùng khi tạo mới, phụ thuộc formatId
        dependsOn: ['formatId'],
        width: '150px'
    },
    { key: 'startTime', label: 'Bắt đầu', type: 'datetime', width: '150px', readonlyInEdit: true, futureOnly: true, minDateOffset: 1 },
    { key: 'endTime', label: 'Kết thúc', type: 'datetime', width: '150px', readonlyInEdit: true, futureOnly: true, minDateOffset: 1 },
    { key: 'audioLanguage', label: 'Âm thanh', type: 'enum', options: languageOptions, width: '120px', readonlyInEdit: true },
    { key: 'subtitleLanguage', label: 'Phụ đề', type: 'enum', options: languageOptions, width: '120px', readonlyInEdit: true },
    {
        key: 'status',
        label: 'Trạng thái',
        type: 'enum',
        readonly: true,
        hideInCreate: true,
        options: [
            { value: 'SCHEDULED', label: 'Sắp chiếu' },
            { value: 'ONGOING', label: 'Đang chiếu' },
            { value: 'FINISHED', label: 'Đã kết thúc' },
            { value: 'CANCELLED', label: 'Đã hủy' },
        ],
    },
]

function statusLabel(status: string) {
    const map: Record<string, string> = {
        SCHEDULED: 'Sắp chiếu',
        ONGOING: 'Đang chiếu',
        FINISHED: 'Đã kết thúc',
        CANCELLED: 'Đã hủy',
    }
    return map[status] ?? status
}

function openCreateModal() {
    showCreateModal.value = true
}

function onFieldBlur(key: string, draft: Record<string, unknown>) {
    if (key !== 'startTime') return

    const val = draft.startTime
    let startDate: Date | null = null

    if (val instanceof Date) {
        startDate = val
    } else if (typeof val === 'string') {
        startDate = parseISODate(val)   // dùng chung tiện ích
    }

    if (!startDate || isNaN(startDate.getTime())) return

    const endEmpty =
        !draft.endTime ||
        (typeof draft.endTime === 'string' && draft.endTime.trim() === '') ||
        (draft.endTime instanceof Date && isNaN(draft.endTime.getTime()))

    if (endEmpty) {
        draft.endTime = dateToISOString(startDate, true)
    }
}

async function handleCreate(draft: Record<string, unknown>) {
    const ok = await create({
        movieId: Number(draft.movieId),
        roomId: Number(draft.roomId),
        formatId: Number(draft.formatId),
        startTime: String(draft.startTime),
        endTime: String(draft.endTime),
        audioLanguage: String(draft.audioLanguage),
        subtitleLanguage: String(draft.subtitleLanguage),
    })
    if (ok) showCreateModal.value = false
}

async function handleCancel(item: ShowtimeResponse) {
    if (item.status === 'CANCELLED') {
        alert('Suất chiếu đã bị hủy trước đó')
        return
    }
    if (confirm(`Hủy suất chiếu #${item.id}?`)) {
        await cancel(item)
    }
}

function viewSeatMap(showtime: ShowtimeResponse) {
    seatMapShowtimeId.value = showtime.id
    isSeatMapOpen.value = true
}

onMounted(async () => {
    await loadCinemaOptions()
})
</script>