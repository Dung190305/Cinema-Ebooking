<template>
    <div class="flex flex-col gap-6 py-6">
        <!-- Breadcrumb & Header -->
        <div class="flex flex-col gap-2 pr-6">
            <div class="flex items-center text-sm">
                <span class="text-text-admin-primary font-medium">Loyalty</span>
            </div>
            <div class="flex items-center justify-between">
                <div>
                    <h1 class="text-lg font-semibold text-text-admin-primary">Hạng thành viên</h1>
                    <p class="text-sm text-text-admin-tertiary">{{ totalItems }} hạng</p>
                </div>
            </div>
        </div>

        <!-- Global errors -->
        <div v-if="globalErrors.length" class="pr-6 rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- Table -->
        <DataTable :rows="tiers" :columns="columns" :fieldErrors="fieldErrors" @save="handleSave" :show-create="false"
            :showDelete="false">
            <template #detail-actions="{ item }">
                <button
                    class="flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 py-2.5 text-sm text-slate-600 transition-colors hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900"
                    @click="router.push(`/admin/promotions/loyalty/tiers/${item.id}/rules`)">
                    <Settings class="size-4" />
                    Cấu hình tích điểm
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

    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Settings } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import { useMembershipTier } from '@/composables/useMembershipTier'
import type { MembershipTierResponse, CreateMembershipTierRequest } from '@/types/membership-tier'
import type { ColumnDef } from '@/components/common/table/types/table'

const router = useRouter()

const {
    tiers, isLoading, fieldErrors, globalErrors,
    currentPage, totalPages, totalItems,
    fetchList, goToPage, create, save, remove,
} = useMembershipTier()

const showCreate = ref(false)
const isCreating = ref(false)

const columns: ColumnDef<MembershipTierResponse>[] = [
    { key: 'id', label: 'ID', type: 'number', readonly: true, hideInCreate: true, hideInTable: true },
    { key: 'name', label: 'Tên hạng', type: 'text', readonly: true, width: '200px' },
    { key: 'minSpendingRequired', label: 'Chi tiêu tối thiểu', type: 'number', width: '180px' },
    { key: 'discountPercent', label: 'Giảm giá (%)', type: 'number', width: '150px' },
    { key: 'benefitsDescription', label: 'Mô tả quyền lợi', type: 'textarea', hideInTable: true },
    { key: 'tierLevel', label: 'Cấp độ', type: 'number', readonly: true, width: '100px' },
]

async function handleSave(item: MembershipTierResponse, done: () => void) {
    const ok = await save(item)
    if (ok) done()
}

onMounted(() => fetchList(0))
</script>