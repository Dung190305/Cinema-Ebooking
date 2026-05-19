<template>
  <div class="flex flex-col gap-6 py-6">
    <div class="flex flex-col gap-2 pr-6">
      <div class="flex items-center text-sm">
        <span class="font-medium text-text-admin-primary">Promotions</span>
      </div>

      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 class="text-lg font-semibold text-text-admin-primary">Mã giảm giá</h1>
          <p class="text-sm text-text-admin-tertiary">{{ totalItems }} coupon</p>
        </div>

        <div class="flex flex-col gap-2 sm:flex-row sm:items-center">
          <select v-model="selectedType"
            class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100">
            <option value="ALL">Tất cả loại</option>
            <option value="PERCENT">Giảm theo %</option>
            <option value="FIXED">Giảm cố định</option>
          </select>

          <select v-model="selectedStatus"
            class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100">
            <option v-for="status in statusOptions" :key="status.value" :value="status.value">
              {{ status.label }}
            </option>
          </select>

          <select v-model="usageFilter"
            class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100">
            <option value="ALL">Tất cả lượt dùng</option>
            <option value="UNUSED">Hết lượt sử dụng</option>
          </select>
        </div>
      </div>
    </div>

    <div v-if="globalErrors.length" class="mr-6 rounded-lg border border-red-100 bg-red-50 p-4">
      <p v-for="err in globalErrors" :key="err" class="text-sm text-red-600">{{ err }}</p>
    </div>

    <div v-if="isLoading && !coupons.length" class="space-y-2 pr-6">
      <div v-for="i in 5" :key="i" class="h-12 animate-pulse rounded-xl bg-slate-100" />
    </div>

    <DataTable :rows="filteredCoupons" :columns="columns" createLabel="Thêm coupon" :fieldErrors="fieldErrors"
      :showDelete="false" @create="showCreate = true" @save="handleSave">
      <template #detail-actions="{ item, close }">
        <button v-if="canActivate(item)"
          class="flex w-full items-center justify-center gap-2 rounded-lg border border-emerald-200 py-2.5 text-sm font-medium text-emerald-700 transition-colors hover:border-emerald-300 hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="actionCouponId === item.id" @click="handleActivate(item, close)">
          <Power class="size-4" />
          Kích hoạt coupon
        </button>

        <button v-if="canDisable(item)"
          class="flex w-full items-center justify-center gap-2 rounded-lg border border-rose-200 py-2.5 text-sm font-medium text-rose-700 transition-colors hover:border-rose-300 hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="actionCouponId === item.id" @click="handleDisable(item, close)">
          <CircleOff class="size-4" />
          Vô hiệu hóa coupon
        </button>
      </template>
    </DataTable>

    <CreateModal v-model="showCreate" title="Thêm coupon" submitLabel="Tạo" size="lg" :columns="columns"
      :isLoading="isLoading" :fieldErrors="fieldErrors" @submit="handleCreate" />

    <ConfirmDialog v-model="showActionConfirm" :title="confirmAction.type === 'activate'
      ? 'Kích hoạt coupon'
      : 'Vô hiệu hóa coupon'
      " :description="confirmAction.type === 'activate'
        ? 'Bạn có chắc muốn kích hoạt coupon này không?'
        : 'Bạn có chắc muốn vô hiệu hóa coupon này không?'
        " :confirmLabel="confirmAction.type === 'activate'
          ? 'Kích hoạt'
          : 'Vô hiệu hóa'
          " :danger="confirmAction.type === 'disable'" @confirm="handleConfirmAction" />

    <div v-if="totalPages > 1" class="flex justify-center gap-1.5 pr-6">
      <button v-for="page in totalPages" :key="page" class="rounded-lg px-3 py-1.5 text-sm transition-colors" :class="currentPage === page - 1
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
import { CircleOff, Power } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import CreateModal from '@/components/common/table/subcomponents/CreateModal.vue'
import ConfirmDialog from '@/components/common/table/subcomponents/ConfirmDialog.vue'
import { useCoupon } from '@/composables/useCoupon'
import type { ColumnDef } from '@/components/common/table/types/table'
import type { CouponResponse, CouponStatus, CreateCouponRequest } from '@/types/coupon.types'

type UsageFilter = 'ALL' | 'UNUSED'
type CouponStatusFilter = CouponStatus | 'ALL'

const {
  coupons,
  filteredCoupons,
  isLoading,
  fieldErrors,
  globalErrors,
  currentPage,
  totalPages,
  totalItems,
  selectedType,
  selectedStatus,
  onlyUnused,
  fetchList,
  goToPage,
  create,
  update,
  updateDraft,
  activate,
  disable,
} = useCoupon()

const showCreate = ref(false)
const actionCouponId = ref<number | null>(null)

const statusOptions: { value: CouponStatusFilter; label: string }[] = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'DRAFT', label: 'Bản nháp' },
  { value: 'ACTIVE', label: 'Đang hoạt động' },
  { value: 'DISABLED', label: 'Đã vô hiệu' },
  { value: 'EXPIRED', label: 'Hết hạn' },
  { value: 'OUT_OF_STOCK', label: 'Hết lượt' },
]

const usageFilter = computed<UsageFilter>({
  get: () => (onlyUnused.value ? 'UNUSED' : 'ALL'),
  set: (value) => {
    onlyUnused.value = value === 'UNUSED'
  },
})

const statusDisplayOptions = statusOptions.filter((option) => option.value !== 'ALL')

function isDraftCoupon(row?: unknown): boolean {

  const coupon = row as CouponResponse | undefined

  return coupon?.status === 'DRAFT'
}

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
    key: 'code',
    label: 'Mã',
    type: 'text',
    width: '250px',

    readonlyInEdit: (row) => !isDraftCoupon(row),
  },

  {
    key: 'type',
    label: 'Loại',
    type: 'enum',
    width: '100px',

    readonlyInEdit: (row) => !isDraftCoupon(row),

    options: [
      { value: 'PERCENT', label: 'Theo %' },
      { value: 'FIXED', label: 'Cố định' },
    ],
  },

  {
    key: 'value',
    label: 'Giá trị',
    type: 'number',                          // fallback mặc định
    typeResolver: (deps) =>                  // tính theo draft.type / row.type
      deps.type === 'FIXED' ? 'currency' : 'number',
    dependsOn: ['type'],
    width: '100px',

    readonlyInEdit: (row) => !isDraftCoupon(row),
  },

  {
    key: 'usageLimit',
    label: 'Giới hạn',
    type: 'number',
    width: '100px',
  },

  {
    key: 'remainingUsage',
    label: 'Còn lại',
    type: 'number',
    width: '100px',
    readonly: true,
    hideInCreate: true,
  },

  {
    key: 'perUserUsage',
    label: 'Mỗi user',
    type: 'number',
    width: '100px',

    readonlyInEdit: (row) => !isDraftCoupon(row),
  },
  {
    key: 'pointsToRedeem',
    label: 'Điểm đổi',
    type: 'number',
    hideInTable: true,

    readonlyInEdit: (row) => !isDraftCoupon(row),
  },

  {
    key: 'minimumBookingValue',
    label: 'Đơn tối thiểu',
    type: 'currency',
    hideInTable: true,

    readonlyInEdit: (row) => !isDraftCoupon(row),
  },

  {
    key: 'maximumDiscountAmount',
    label: 'Giảm tối đa',
    type: 'currency',
    hideInTable: true,

    readonlyInEdit: (row) => !isDraftCoupon(row),
  },

  {
    key: 'startDate',
    label: 'Bắt đầu',
    type: 'date',
    width: '130px',

    readonlyInEdit: (row) => !isDraftCoupon(row),
  },

  {
    key: 'endDate',
    label: 'Kết thúc',
    type: 'date',
    width: '130px',
  },

  {
    key: 'status',
    label: 'Trạng thái',
    type: 'enum',
    width: '150px',
    readonly: true,
    hideInCreate: true,
    options: statusDisplayOptions,
  },
])

async function handleCreate(draft: Record<string, unknown>) {
  const payload: CreateCouponRequest = {
    code: String(draft.code || '').trim(),
    type: draft.type === 'FIXED' ? 'FIXED' : 'PERCENT',
    value: Number(draft.value || 0),
    usageLimit: Number(draft.usageLimit || 0),
    perUserUsage: Number(draft.perUserUsage || 0),
    pointsToRedeem: Number(draft.pointsToRedeem || 0),
    minimumBookingValue: Number(draft.minimumBookingValue || 0),
    maximumDiscountAmount: Number(draft.maximumDiscountAmount || 0),
    startDate: String(draft.startDate || ''),
    endDate: String(draft.endDate || ''),
  }

  const ok = await create(payload)
  if (ok) showCreate.value = false
}

async function handleSave(
  item: CouponResponse,
  done: () => void,
) {

  const ok =
    item.status === 'DRAFT'
      ? await updateDraft(item)
      : await update(item)

  if (ok) {
    done()
  }
}

function toCoupon(item: Record<string, unknown>): CouponResponse {
  return item as unknown as CouponResponse
}

function canActivate(item: Record<string, unknown>) {
  const status = toCoupon(item).status
  return status === 'DRAFT' || status === 'DISABLED'
}

function canDisable(item: Record<string, unknown>) {
  return toCoupon(item).status === 'ACTIVE'
}

const confirmAction = ref<{
  type: 'activate' | 'disable'
  item: CouponResponse | null
  close?: () => void
}>({
  type: 'activate',
  item: null,
})

const showActionConfirm = ref(false)

function handleActivate(
  item: Record<string, unknown>,
  close: () => void,
) {

  confirmAction.value = {
    type: 'activate',
    item: toCoupon(item),
    close,
  }

  showActionConfirm.value = true
}

function handleDisable(
  item: Record<string, unknown>,
  close: () => void,
) {

  confirmAction.value = {
    type: 'disable',
    item: toCoupon(item),
    close,
  }

  showActionConfirm.value = true
}

async function handleConfirmAction() {

  const action = confirmAction.value
  const coupon = action.item

  if (!coupon) return

  actionCouponId.value = coupon.id

  try {

    const updated =
      action.type === 'activate'
        ? await activate(coupon)
        : await disable(coupon)

    if (updated) {

      Object.assign(coupon, updated)

      action.close?.()
    }

  } finally {
    actionCouponId.value = null
  }
}
onMounted(() => fetchList(0))
</script>
