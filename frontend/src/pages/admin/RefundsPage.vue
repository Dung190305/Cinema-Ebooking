<template>
  <div class="flex flex-col gap-6 py-6">
    <!-- Header -->
    <div class="flex flex-col gap-4 pr-6">
      <div class="flex items-center text-sm">
        <span class="font-medium text-text-admin-primary">Operations</span>
        <span class="mx-2 text-text-admin-tertiary">/</span>
        <span class="text-text-admin-tertiary">Refunds</span>
      </div>

      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 class="text-lg font-semibold text-text-admin-primary">
            Quản lý hoàn tiền
          </h1>
          <p class="text-sm text-text-admin-tertiary">
            {{ totalItems }} yêu cầu hoàn tiền trong hệ thống
          </p>
        </div>

        <button
          class="inline-flex items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-medium text-text-on-accent transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="isLoading"
          @click="fetchRefunds"
        >
          <RefreshCw class="size-4" />
          Làm mới
        </button>
      </div>

      <!-- Stats -->
      <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <div class="rounded-xl border border-slate-100 bg-white p-4 shadow-sm">
          <div class="mb-3 flex items-center justify-between">
            <div class="rounded-lg bg-blue-50 p-2 text-blue-600">
              <FileText class="size-4" />
            </div>
            <span class="rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-600">
              Hôm nay
            </span>
          </div>
          <p class="text-xs font-medium text-text-admin-tertiary">Yêu cầu hôm nay</p>
          <p class="mt-1 text-2xl font-semibold text-text-admin-primary">{{ stats.todayRequests }}</p>
          <p class="mt-1 text-xs text-text-admin-tertiary">Refund được tạo trong ngày</p>
        </div>

        <div class="rounded-xl border border-slate-100 bg-white p-4 shadow-sm">
          <div class="mb-3 flex items-center justify-between">
            <div class="rounded-lg bg-yellow-50 p-2 text-yellow-700">
              <Clock3 class="size-4" />
            </div>
            <span class="rounded-full bg-yellow-50 px-2.5 py-0.5 text-xs font-medium text-yellow-700">
              Cần xử lý
            </span>
          </div>
          <p class="text-xs font-medium text-text-admin-tertiary">Chờ duyệt</p>
          <p class="mt-1 text-2xl font-semibold text-yellow-700">{{ stats.pendingCount }}</p>
          <p class="mt-1 text-xs text-text-admin-tertiary">Đang chờ admin xử lý</p>
        </div>

        <div class="rounded-xl border border-slate-100 bg-white p-4 shadow-sm">
          <div class="mb-3 flex items-center justify-between">
            <div class="rounded-lg bg-emerald-50 p-2 text-emerald-600">
              <Banknote class="size-4" />
            </div>
            <span class="rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
              Tháng này
            </span>
          </div>
          <p class="text-xs font-medium text-text-admin-tertiary">Đã hoàn trong tháng</p>
          <p class="mt-1 text-2xl font-semibold text-text-admin-primary">
            {{ formatCompactCurrency(stats.monthCompletedAmount) }}
          </p>
          <p class="mt-1 text-xs text-text-admin-tertiary">{{ stats.monthCompletedCount }} giao dịch</p>
        </div>

        <div class="rounded-xl border border-slate-100 bg-white p-4 shadow-sm">
          <div class="mb-3 flex items-center justify-between">
            <div class="rounded-lg bg-purple-50 p-2 text-purple-600">
              <Percent class="size-4" />
            </div>
            <span class="rounded-full bg-purple-50 px-2.5 py-0.5 text-xs font-medium text-purple-700">
              Tổng quan
            </span>
          </div>
          <p class="text-xs font-medium text-text-admin-tertiary">Tỷ lệ duyệt</p>
          <p class="mt-1 text-2xl font-semibold text-text-admin-primary">{{ stats.approvalRate }}%</p>
          <p class="mt-1 text-xs text-text-admin-tertiary">Đã duyệt / tổng yêu cầu</p>
        </div>
      </div>

      <!-- Filters -->
      <div class="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
        <select
          v-model="selectedStatus"
          class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
          @change="fetchRefunds(0)"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="REQUESTED">Chờ xử lý</option>
          <option value="APPROVED">Đã duyệt</option>
          <option value="REJECTED">Từ chối</option>
          <option value="COMPLETED">Hoàn tất</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>

        <button
          class="inline-flex items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
          @click="resetFilters"
        >
          <RotateCcw class="size-4" />
          Đặt lại
        </button>
      </div>
    </div>

    <!-- Error -->
    <div
      v-if="globalError"
      class="mr-6 rounded-lg border border-red-100 bg-red-50 p-4 text-sm text-red-600"
    >
      {{ globalError }}
    </div>

    <!-- Success -->
    <div
      v-if="successMessage"
      class="mr-6 rounded-lg border border-emerald-100 bg-emerald-50 p-4 text-sm text-emerald-700"
    >
      {{ successMessage }}
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="space-y-2 pr-6">
      <div v-for="i in 5" :key="i" class="h-12 animate-pulse rounded-xl bg-slate-100" />
    </div>

    <!-- DataTable -->
    <DataTable
      v-else
      :rows="refunds"
      :columns="columns"
      :show-create="false"
      :show-delete="false"
      :show-save="false"
      :pagination="pagination"
      @page-change="onPageChange"
    >
      <template #cell-cinemaName="{ value }">
        <span class="inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-600">
          <Building2 class="size-3.5" />
          {{ value || '—' }}
        </span>
      </template>

      <template #cell-originalAmount="{ value }">
        <span class="text-slate-600">{{ formatCurrency(value as number) }}</span>
      </template>

      <template #cell-refundAmount="{ value }">
        <span class="font-semibold text-slate-900">{{ formatCurrency(value as number) }}</span>
      </template>

      <template #cell-refundPercentage="{ value }">
        <span class="text-slate-700">{{ Number(value || 0) }}%</span>
      </template>

      <template #cell-status="{ value }">
        <span
          class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium"
          :class="statusClass(value as RefundStatus)"
        >
          {{ statusLabel(value as RefundStatus) }}
        </span>
      </template>

      <template #cell-showtimeStartTime="{ value }">
        <span class="text-slate-600">{{ formatDateTime(value as string) }}</span>
      </template>

      <template #detail-actions="{ item, close }">
        <div class="flex flex-col gap-3">
          <div class="rounded-lg bg-slate-50 p-3 space-y-2">
            <div class="flex items-center justify-between text-sm">
              <span class="text-slate-500">Tiền gốc</span>
              <span class="font-medium text-slate-800">{{ formatCurrency(Number(item.originalAmount || 0)) }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-slate-500">Tiền hoàn</span>
              <span class="font-semibold text-blue-700">{{ formatCurrency(Number(item.refundAmount || 0)) }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-slate-500">Trạng thái</span>
              <span
                class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium"
                :class="statusClass(item.status as RefundStatus)"
              >
                {{ statusLabel(item.status as RefundStatus) }}
              </span>
            </div>
            <div v-if="item.reason" class="flex items-start justify-between text-sm gap-2">
              <span class="text-slate-500 shrink-0">Lý do</span>
              <span class="text-right text-slate-700 text-xs">{{ item.reason }}</span>
            </div>
            <div v-if="item.adminNote" class="flex items-start justify-between text-sm gap-2">
              <span class="text-slate-500 shrink-0">Ghi chú</span>
              <span class="text-right text-slate-700 text-xs">{{ item.adminNote }}</span>
            </div>
          </div>

          <button
            v-if="item.status === 'REQUESTED'"
            class="w-full rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="isActionLoading"
            @click="approveRefund(Number(item.id), close)"
          >
            Duyệt yêu cầu
          </button>

          <button
            v-if="item.status === 'REQUESTED'"
            class="w-full rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="isActionLoading"
            @click="rejectRefund(Number(item.id), close)"
          >
            Từ chối
          </button>

          <button
            v-if="item.status === 'APPROVED'"
            class="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="isActionLoading"
            @click="completeRefund(Number(item.id), close)"
          >
            Hoàn tất refund
          </button>

          <p
            v-if="!['REQUESTED', 'APPROVED'].includes(String(item.status))"
            class="rounded-lg bg-slate-50 p-3 text-center text-xs text-slate-500"
          >
            Refund này đã được xử lý xong.
          </p>
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Banknote, Building2, Clock3, FileText, Percent, RefreshCw, RotateCcw } from 'lucide-vue-next'
import DataTable from '@/components/common/table/DataTable.vue'
import type { ColumnDef } from '@/components/common/table/types/table'
import { refundApi } from '@/api/refund.api'
import type { RefundResponse, RefundStatus } from '@/types/refund.types'

// ── Refs ──────────────────────────────────────────────────────────────────────
const refunds = ref<RefundResponse[]>([])
const isLoading = ref(false)
const isActionLoading = ref(false)
const globalError = ref('')
const successMessage = ref('')

const selectedStatus = ref<string>('')
const currentPage = ref(0)
const totalPages = ref(0)
const totalItems = ref(0)

// ── Pagination ────────────────────────────────────────────────────────────────
const pagination = computed(() => ({
  currentPage: currentPage.value,
  totalPages: totalPages.value,
  totalItems: totalItems.value,
}))

function onPageChange(page: number) {
  currentPage.value = page
  fetchRefunds(page)
}

// ── Columns ──────────────────────────────────────────────────────────────────
const columns: ColumnDef<RefundResponse>[] = [
  {
    key: 'id',
    label: 'Refund ID',
    type: 'number',
    readonly: true,
    width: '100px',
  },
  {
    key: 'bookingCode',
    label: 'Mã đặt vé',
    type: 'text',
    readonly: true,
    width: '140px',
  },
  {
    key: 'movieTitle',
    label: 'Phim',
    type: 'text',
    readonly: true,
    width: '180px',
  },
  {
    key: 'cinemaName',
    label: 'Rạp',
    type: 'text',
    readonly: true,
    width: '140px',
  },
  {
    key: 'roomName',
    label: 'Phòng',
    type: 'text',
    readonly: true,
    width: '100px',
  },
  {
    key: 'showtimeStartTime',
    label: 'Suất chiếu',
    type: 'datetime',
    readonly: true,
    width: '160px',
  },
  {
    key: 'originalAmount',
    label: 'Tiền gốc',
    type: 'currency',
    readonly: true,
    width: '120px',
  },
  {
    key: 'refundAmount',
    label: 'Tiền hoàn',
    type: 'currency',
    readonly: true,
    width: '120px',
  },
  {
    key: 'refundPercentage',
    label: 'Tỷ lệ',
    type: 'number',
    readonly: true,
    width: '80px',
    displayFn: (value) => `${Number(value || 0)}%`,
  },
  {
    key: 'status',
    label: 'Trạng thái',
    type: 'enum',
    readonly: true,
    width: '130px',
    options: [
      { value: 'REQUESTED', label: 'Chờ xử lý' },
      { value: 'APPROVED', label: 'Đã duyệt' },
      { value: 'REJECTED', label: 'Từ chối' },
      { value: 'COMPLETED', label: 'Hoàn tất' },
      { value: 'CANCELLED', label: 'Đã hủy' },
    ],
  },
  {
    key: 'requestedAt',
    label: 'Ngày yêu cầu',
    type: 'datetime',
    readonly: true,
    width: '160px',
  },
]

// ── Stats ─────────────────────────────────────────────────────────────────────
const stats = computed(() => {
  const today = new Date()
  const all = refunds.value

  const todayRequests = all.filter(r => isSameDate(r.requestedAt, today)).length

  const pendingCount = all.filter(r => r.status === 'REQUESTED').length

  const completedThisMonth = all.filter(r =>
    r.status === 'COMPLETED' && isInCurrentMonth(r.processedAt || r.requestedAt),
  )

  const approvedCount = all.filter(r =>
    r.status === 'APPROVED' || r.status === 'COMPLETED',
  ).length

  const approvalRate = all.length === 0
    ? 0
    : Math.round((approvedCount / all.length) * 100)

  return {
    todayRequests,
    pendingCount,
    monthCompletedAmount: completedThisMonth.reduce(
      (sum, r) => sum + Number(r.refundAmount || 0), 0,
    ),
    monthCompletedCount: completedThisMonth.length,
    approvalRate,
  }
})

// ── API calls ─────────────────────────────────────────────────────────────────
async function fetchRefunds(page = 0) {
  isLoading.value = true
  clearMessages()
  currentPage.value = page

  try {
    const res = await refundApi.getAdminList({
      status: selectedStatus.value || undefined,
      page,
      size: 20,
      sort: 'requestedAt,desc',
    })

    refunds.value = res.content || []
    currentPage.value = res.page?.number ?? 0
    totalPages.value = res.page?.totalPages ?? 0
    totalItems.value = res.page?.totalElements ?? 0
  } catch (error: unknown) {
    refunds.value = []
    globalError.value = getErrorMessage(error, 'Không thể tải danh sách hoàn tiền')
  } finally {
    isLoading.value = false
  }
}

async function approveRefund(id: number, close?: () => void) {
  const adminNote = globalThis.prompt(
    'Nhập ghi chú duyệt refund:',
    'Yêu cầu hợp lệ, đồng ý hoàn tiền.',
  )
  if (adminNote === null) return
  await processRefundAction(
    () => refundApi.approve(id, { adminNote }),
    'Đã duyệt yêu cầu hoàn tiền',
    close,
  )
}

async function rejectRefund(id: number, close?: () => void) {
  const adminNote = globalThis.prompt('Nhập lý do từ chối refund:', 'Yêu cầu không hợp lệ.')
  if (adminNote === null) return
  await processRefundAction(
    () => refundApi.reject(id, { adminNote }),
    'Đã từ chối yêu cầu hoàn tiền',
    close,
  )
}

async function completeRefund(id: number, close?: () => void) {
  const ok = globalThis.confirm(
    'Bạn chắc chắn muốn hoàn tất refund này? Booking sẽ chuyển sang CANCELLED.',
  )
  if (!ok) return

  const adminNote = globalThis.prompt(
    'Nhập ghi chú hoàn tất refund:',
    'Đã hoàn tất xử lý hoàn tiền.',
  )
  if (adminNote === null) return

  await processRefundAction(
    () => refundApi.complete(id, { adminNote }),
    'Đã hoàn tất hoàn tiền',
    close,
  )
}

async function processRefundAction(
  action: () => Promise<RefundResponse>,
  message: string,
  close?: () => void,
) {
  isActionLoading.value = true
  clearMessages()
  try {
    await action()
    successMessage.value = message
    close?.()
    await fetchRefunds(currentPage.value)
  } catch (error: unknown) {
    globalError.value = getErrorMessage(error, 'Không thể xử lý refund')
  } finally {
    isActionLoading.value = false
  }
}

// ── Filters ───────────────────────────────────────────────────────────────────
function resetFilters() {
  selectedStatus.value = ''
  fetchRefunds(0)
}

// ── Helpers ──────────────────────────────────────────────────────────────────
function clearMessages() {
  globalError.value = ''
  successMessage.value = ''
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    return String((error as { message: unknown }).message || fallback)
  }
  return fallback
}

function formatCurrency(value?: number | string | null) {
  return `${new Intl.NumberFormat('vi-VN').format(Number(value || 0))} ₫`
}

function formatCompactCurrency(value?: number | null) {
  const amount = Number(value || 0)
  if (amount >= 1_000_000_000) return `${(amount / 1_000_000_000).toFixed(1)}B ₫`
  if (amount >= 1_000_000) return `${(amount / 1_000_000).toFixed(1)}M ₫`
  return `${new Intl.NumberFormat('vi-VN').format(amount)} ₫`
}

function formatDateTime(value?: string | null) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleString('vi-VN', {
    hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric',
  })
}

function statusLabel(status: RefundStatus) {
  switch (status) {
    case 'REQUESTED': return 'Chờ xử lý'
    case 'APPROVED': return 'Đã duyệt'
    case 'REJECTED': return 'Từ chối'
    case 'COMPLETED': return 'Hoàn tất'
    case 'CANCELLED': return 'Đã hủy'
    default: return status
  }
}

function statusClass(status: RefundStatus) {
  switch (status) {
    case 'REQUESTED': return 'bg-yellow-100 text-yellow-800'
    case 'APPROVED': return 'bg-blue-100 text-blue-700'
    case 'REJECTED': return 'bg-red-100 text-red-700'
    case 'COMPLETED': return 'bg-emerald-100 text-emerald-700'
    case 'CANCELLED': return 'bg-slate-100 text-slate-600'
    default: return 'bg-slate-100 text-slate-600'
  }
}

function isSameDate(value: string | null | undefined, date: Date) {
  if (!value) return false
  const d = new Date(value)
  return d.getFullYear() === date.getFullYear()
    && d.getMonth() === date.getMonth()
    && d.getDate() === date.getDate()
}

function isInCurrentMonth(value?: string | null) {
  if (!value) return false
  const now = new Date()
  const d = new Date(value)
  return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth()
}

// ── Lifecycle ─────────────────────────────────────────────────────────────────
onMounted(() => fetchRefunds(0))
</script>
