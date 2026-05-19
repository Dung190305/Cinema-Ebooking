<template>
    <div class="flex flex-col gap-6 py-6">
        <!-- Breadcrumb -->
        <div class="flex flex-col gap-2 pr-6">
            <div class="flex items-center gap-2 text-sm">
                <button class="text-text-admin-tertiary transition-colors hover:text-text-admin-primary"
                    @click="router.push('/admin/promotions/loyalty/tiers')">
                    Loyalty
                </button>
                <ChevronRight class="size-3.5 text-text-admin-tertiary" />
                <span class="text-text-admin-primary font-medium">{{ tierName }}</span>
                <ChevronRight class="size-3.5 text-text-admin-tertiary" />
                <span class="text-text-admin-tertiary">Cấu hình tích điểm</span>
            </div>

            <div class="flex items-center justify-between">
                <div>
                    <h1 class="text-lg font-semibold text-text-admin-primary">Cấu hình tích điểm</h1>
                    <p class="text-sm text-text-admin-tertiary">{{ rules.length }} quy tắc</p>
                </div>
            </div>
        </div>

        <!-- Global errors -->
        <div v-if="globalErrors.length" class="pr-6 rounded-lg bg-red-50 border border-red-100 p-4">
            <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
        </div>

        <!-- Loading skeleton -->
        <div v-if="isLoading && !rules.length" class="pr-6 space-y-2">
            <div v-for="i in 5" :key="i" class="h-12 animate-pulse rounded-xl bg-slate-100" />
        </div>

        <!-- Table -->
        <DataTable :rows="rules" :columns="columns" createLabel="Thêm quy tắc" :fieldErrors="fieldErrors"
            @create="showCreate = true" :showDelete="false" @save="handleSave" />

        <!-- Create modal -->
        <CreateModal v-model="showCreate" title="Thêm quy tắc tích điểm" :columns="columns" :isLoading="isLoading"
            :fieldErrors="fieldErrors" @submit="handleCreate" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronRight } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import { useEarningRule } from '@/composables/useEarningRule'
import { membershipTierApi } from '@/api/membership-tier.api'
import type { EarningRuleResponse, CreateEarningRuleRequest } from '@/types/earning-rule'
import type { ColumnDef } from '@/components/common/table/types/table'

const route = useRoute()
const router = useRouter()
const tierId = Number(route.params.tierId)

const { rules, isLoading, fieldErrors, globalErrors, fetchAll, create, save, remove } =
    useEarningRule(tierId)

const showCreate = ref(false)

// Lấy tên tier cho breadcrumb
const tierName = ref('...')
membershipTierApi.getById(tierId)
    .then((tier) => { tierName.value = tier.name })
    .catch(() => { tierName.value = `Tier #${tierId}` })

const columns: ColumnDef<EarningRuleResponse>[] = [
    { key: 'id', label: 'ID', type: 'number', readonly: true, hideInCreate: true, hideInTable: true },
    { key: 'tierId', label: 'Tier ID', type: 'number', readonly: true, hideInCreate: false, hideInTable: true, default: tierId },
    {
        key: 'earningType',
        label: 'Loại',
        type: 'enum',
        options: [
            { value: 'TICKET', label: 'Vé' },
            { value: 'CONCESSION', label: 'Đồ ăn' },
        ],
        width: '150px',
    },
    { key: 'multiplier', label: 'Tỉ lệ (%)', type: 'number', width: '120px' },
    { key: 'description', label: 'Mô tả', type: 'text', hideInTable: true },
    { key: 'active', label: 'Kích hoạt', type: 'boolean', width: '100px', hideInCreate: true },
    { key: 'priority', label: 'Ưu tiên', type: 'number', width: '100px' },
]

async function handleCreate(draft: Record<string, unknown>) {
    const payload: CreateEarningRuleRequest = {
        tierId: tierId,
        earningType: String(draft.earningType || 'TICKET'),
        multiplier: Number(draft.multiplier || 0),
        fixedPoints: Number(null),
        description: String(draft.description || ''),
        conditions: String(null),
        active: Boolean(true),
        priority: Number(draft.priority || 0),
    }
    const ok = await create(payload)
    if (ok) showCreate.value = false
}

async function handleSave(item: EarningRuleResponse, done: () => void) {
    const ok = await save(item)
    if (ok) done()
}

onMounted(() => fetchAll())
</script>