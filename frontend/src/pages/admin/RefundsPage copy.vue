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
          <h1 class="text-lg font-semibold text-text-admin-primary">Quản lý hoàn tiền</h1>
          <p class="text-sm text-text-admin-tertiary">
            {{ totalItems }} yêu cầu hoàn tiền trong hệ thống
          </p>
        </div>

        <button
          class="inline-flex items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-medium text-text-on-accent transition hover:opacity-90"
          @click="fetchRefunds(currentPage)"
        >
          <RefreshCw class="size-4" />
          Làm mới
        </button>
      </div>

      <!-- Create refund box -->
      <div class="rounded-xl border border-slate-100 bg-white p-4">
        <div class="mb-4 flex items-center justify-between gap-3">
          <div>
            <h2 class="text-sm font-semibold text-slate-800">Tạo yêu cầu hoàn tiền</h2>
            <p class="mt-1 text-xs text-slate-500">
              Nhập bookingId đã thanh toán để tính số tiền hoàn và tạo yêu cầu refund.
            </p>
          </div>
        </div>

        <div class="grid gap-3 lg:grid-cols-[160px_1fr_auto_auto]">
          <input
            v-model="createBookingId"
            type="number"
            min="1"
            placeholder="Booking ID"
            class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
          />

          <input
            v-model="createReason"
            type="text"
            maxlength="500"
            placeholder="Lý do hoàn tiền"
            class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
          />

          <button
            class="inline-flex items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="isActionLoading || !createBookingId"
            @click="calculateRefund"
          >
            <Calculator class="size-4" />
            Tính tiền
          </button>

          <button
            class="inline-flex items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-medium text-text-on-accent transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="isActionLoading || !createBookingId"
            @click="createRefund"
          >
            <Plus class="size-4" />
            Tạo refund
          </button>
        </div>

        <div
          v-if="calculation"
          class="mt-4 grid gap-3 rounded-lg bg-slate-50 p-4 text-sm sm:grid-cols-4"
        >
          <InfoBox label="Tiền gốc" :value="formatCurrency(calculation.originalAmount)" />
          <InfoBox label="Tỷ lệ hoàn" :value="formatPercent(calculation.refundPercentage)" />
          <InfoBox label="Tiền hoàn" :value="formatCurrency(calculation.refundAmount)" strong />
          <InfoBox label="Thông báo" :value="calculation.message" />
        </div>
      </div>

      <!-- Filters -->
      <div class="grid gap-2 sm:grid-cols-2 lg:grid-cols-5">
        <select
          v-model="selectedStatus"
          class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="REQUESTED">Chờ xử lý</option>
          <option value="APPROVED">Đã duyệt</option>
          <option value="REJECTED">Từ chối</option>
          <option value="COMPLETED">Hoàn tất</option>
          <option value="CANCELLED">Khách hủy yêu cầu</option>
        </select>

        <input
          v-model="searchKeyword"
          type="text"
          placeholder="Tìm theo refundId hoặc bookingId"
          class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
        />

        <button
          class="inline-flex items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-medium text-text-on-accent transition hover:opacity-90"
          @click="fetchRefunds(0)"
        >
          <Search class="size-4" />
          Tìm
        </button>

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

    <!-- Table -->
    <div v-else class="pr-6">
      <div class="overflow-hidden rounded-xl border border-slate-100 bg-white">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-100 bg-slate-50">
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Refund ID
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Booking ID
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Tiền gốc
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Tiền hoàn
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Tỷ lệ
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Trạng thái
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Ngày yêu cầu
              </th>
              <th class="px-4 py-3 text-right text-xs font-medium text-text-admin-secondary">
                Hành động
              </th>
            </tr>
          </thead>

          <tbody class="divide-y divide-slate-50">
            <tr v-if="filteredRefunds.length === 0">
              <td colspan="8" class="py-12 text-center text-sm text-slate-400">
                Chưa có dữ liệu hoàn tiền
              </td>
            </tr>

            <tr
              v-for="refund in filteredRefunds"
              :key="refund.id"
              class="transition hover:bg-slate-50"
            >
              <td class="px-4 py-3 font-medium text-slate-800">#{{ refund.id }}</td>

              <td class="px-4 py-3 text-slate-600">#{{ refund.bookingId }}</td>

              <td class="px-4 py-3 text-slate-600">
                {{ formatCurrency(refund.originalAmount) }}
              </td>

              <td class="px-4 py-3 font-medium text-slate-800">
                {{ formatCurrency(refund.refundAmount) }}
              </td>

              <td class="px-4 py-3 text-slate-600">
                {{ formatPercent(refund.refundPercentage) }}
              </td>

              <td class="px-4 py-3">
                <span
                  class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium"
                  :class="statusClass(refund.status)"
                >
                  {{ statusLabel(refund.status) }}
                </span>
              </td>

              <td class="px-4 py-3 text-slate-500">
                {{ formatDateTime(refund.requestedAt) }}
              </td>

              <td class="px-4 py-3">
                <div class="flex justify-end gap-2">
                  <button
                    class="inline-flex items-center justify-center rounded-lg border border-slate-200 p-2 text-slate-600 transition hover:bg-slate-100"
                    title="Xem chi tiết"
                    @click="openDetail(refund)"
                  >
                    <Eye class="size-4" />
                  </button>

                  <button
                    v-if="refund.status === 'REQUESTED'"
                    class="inline-flex items-center justify-center rounded-lg border border-emerald-200 p-2 text-emerald-600 transition hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-60"
                    :disabled="isActionLoading"
                    title="Duyệt refund"
                    @click="approveRefund(refund.id)"
                  >
                    <CheckCircle class="size-4" />
                  </button>

                  <button
                    v-if="refund.status === 'REQUESTED'"
                    class="inline-flex items-center justify-center rounded-lg border border-red-200 p-2 text-red-600 transition hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-60"
                    :disabled="isActionLoading"
                    title="Từ chối refund"
                    @click="rejectRefund(refund.id)"
                  >
                    <XCircle class="size-4" />
                  </button>

                  <button
                    v-if="refund.status === 'APPROVED'"
                    class="inline-flex items-center justify-center rounded-lg border border-blue-200 p-2 text-blue-600 transition hover:bg-blue-50 disabled:cursor-not-allowed disabled:opacity-60"
                    :disabled="isActionLoading"
                    title="Hoàn tất refund"
                    @click="completeRefund(refund.id)"
                  >
                    <BadgeCheck class="size-4" />
                  </button>

                  <button
                    v-if="refund.status === 'REQUESTED'"
                    class="inline-flex items-center justify-center rounded-lg border border-slate-200 p-2 text-slate-500 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                    :disabled="isActionLoading"
                    title="Hủy yêu cầu refund"
                    @click="cancelRefund(refund.id)"
                  >
                    <Ban class="size-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="mt-4 flex justify-center gap-1.5">
        <button
          v-for="page in totalPages"
          :key="page"
          class="rounded-lg px-3 py-1.5 text-sm transition-colors"
          :class="
            currentPage === page - 1
              ? 'bg-accent font-medium text-text-on-accent'
              : 'text-text-admin-secondary hover:bg-slate-100'
          "
          @click="fetchRefunds(page - 1)"
        >
          {{ page }}
        </button>
      </div>
    </div>

    <!-- Detail Drawer -->
    <Teleport to="body">
      <div v-if="selectedRefund" class="fixed inset-0 z-40 bg-black/20" @click="closeDetail" />

      <div
        v-if="selectedRefund"
        class="fixed inset-y-0 right-0 z-50 flex w-full max-w-lg flex-col bg-white shadow-2xl"
      >
        <div class="flex items-center justify-between border-b border-slate-100 p-5">
          <div>
            <h2 class="text-base font-semibold text-slate-900">
              Chi tiết refund #{{ selectedRefund.id }}
            </h2>
            <p class="text-sm text-slate-500">Booking #{{ selectedRefund.bookingId }}</p>
          </div>

          <button
            class="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
            @click="closeDetail"
          >
            <X class="size-5" />
          </button>
        </div>

        <div class="flex-1 space-y-5 overflow-y-auto p-5">
          <div class="rounded-xl border border-slate-100 p-4">
            <h3 class="mb-3 text-sm font-semibold text-slate-800">Thông tin hoàn tiền</h3>

            <div class="space-y-2 text-sm">
              <InfoRow label="Tiền gốc" :value="formatCurrency(selectedRefund.originalAmount)" />
              <InfoRow label="Tỷ lệ hoàn" :value="formatPercent(selectedRefund.refundPercentage)" />
              <InfoRow
                label="Số tiền hoàn"
                :value="formatCurrency(selectedRefund.refundAmount)"
                strong
              />
              <InfoRow label="Trạng thái" :value="statusLabel(selectedRefund.status)" />
              <InfoRow label="Ngày yêu cầu" :value="formatDateTime(selectedRefund.requestedAt)" />
              <InfoRow label="Ngày xử lý" :value="formatDateTime(selectedRefund.processedAt)" />
            </div>
          </div>

          <div class="rounded-xl border border-slate-100 p-4">
            <h3 class="mb-3 text-sm font-semibold text-slate-800">Ghi chú</h3>

            <div class="space-y-3 text-sm">
              <div>
                <p class="mb-1 text-xs font-medium text-slate-400">Lý do khách hàng</p>
                <p class="rounded-lg bg-slate-50 p-3 text-slate-700">
                  {{ selectedRefund.reason || '—' }}
                </p>
              </div>

              <div>
                <p class="mb-1 text-xs font-medium text-slate-400">Ghi chú admin</p>
                <p class="rounded-lg bg-slate-50 p-3 text-slate-700">
                  {{ selectedRefund.adminNote || '—' }}
                </p>
              </div>
            </div>
          </div>

          <div class="rounded-xl border border-slate-100 p-4">
            <h3 class="mb-3 text-sm font-semibold text-slate-800">Hành động</h3>

            <div class="grid gap-2">
              <button
                v-if="selectedRefund.status === 'REQUESTED'"
                class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="isActionLoading"
                @click="approveRefund(selectedRefund.id)"
              >
                Duyệt refund
              </button>

              <button
                v-if="selectedRefund.status === 'REQUESTED'"
                class="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="isActionLoading"
                @click="rejectRefund(selectedRefund.id)"
              >
                Từ chối refund
              </button>

              <button
                v-if="selectedRefund.status === 'APPROVED'"
                class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="isActionLoading"
                @click="completeRefund(selectedRefund.id)"
              >
                Hoàn tất refund
              </button>

              <p
                v-if="!['REQUESTED', 'APPROVED'].includes(selectedRefund.status)"
                class="rounded-lg bg-slate-50 p-3 text-center text-xs text-slate-500"
              >
                Refund này đã được xử lý xong, không còn hành động tiếp theo.
              </p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import {
  BadgeCheck,
  Ban,
  Calculator,
  CheckCircle,
  Eye,
  Plus,
  RefreshCw,
  RotateCcw,
  Search,
  X,
  XCircle,
} from 'lucide-vue-next'
import { refundApi } from '@/api/refund.api'
import type { RefundCalculationResponse, RefundResponse, RefundStatus } from '@/types/refund.types'

const refunds = ref<RefundResponse[]>([])
const selectedRefund = ref<RefundResponse | null>(null)
const calculation = ref<RefundCalculationResponse | null>(null)

const isLoading = ref(false)
const isActionLoading = ref(false)
const globalError = ref('')
const successMessage = ref('')

const selectedStatus = ref<RefundStatus | ''>('')
const searchKeyword = ref('')

const createBookingId = ref<number | null>(null)
const createReason = ref('')

const currentPage = ref(0)
const totalPages = ref(0)
const totalItems = ref(0)

const pageSize = 8

const InfoRow = defineComponent({
  props: {
    label: {
      type: String,
      required: true,
    },
    value: {
      type: String,
      required: true,
    },
    strong: {
      type: Boolean,
      default: false,
    },
  },

  setup(props) {
    return () =>
      h('div', { class: 'flex justify-between gap-4' }, [
        h('span', { class: 'text-slate-400' }, props.label),
        h(
          'span',
          {
            class: props.strong
              ? 'text-right font-semibold text-slate-900'
              : 'text-right text-slate-700',
          },
          props.value || '—',
        ),
      ])
  },
})

const InfoBox = defineComponent({
  props: {
    label: {
      type: String,
      required: true,
    },
    value: {
      type: String,
      required: true,
    },
    strong: {
      type: Boolean,
      default: false,
    },
  },

  setup(props) {
    return () =>
      h('div', { class: 'min-w-0' }, [
        h('p', { class: 'text-xs text-slate-400' }, props.label),
        h(
          'p',
          {
            class: props.strong
              ? 'mt-1 truncate text-sm font-semibold text-slate-900'
              : 'mt-1 truncate text-sm text-slate-700',
            title: props.value,
          },
          props.value || '—',
        ),
      ])
  },
})

const filteredRefunds = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return refunds.value.filter((refund) => {
    const matchStatus = selectedStatus.value ? refund.status === selectedStatus.value : true

    const matchKeyword = keyword
      ? String(refund.id).includes(keyword) || String(refund.bookingId).includes(keyword)
      : true

    return matchStatus && matchKeyword
  })
})

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error) return error.message

  if (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    typeof (error as { message?: unknown }).message === 'string'
  ) {
    return (error as { message: string }).message
  }

  return fallback
}

function clearMessages() {
  globalError.value = ''
  successMessage.value = ''
}

async function fetchRefunds(page = 0) {
  isLoading.value = true
  clearMessages()

  try {
    const res = await refundApi.getAdminList({
      page,
      size: pageSize,
      sort: 'requestedAt,desc',
    })

    refunds.value = res.content || []
    currentPage.value = res.page?.number ?? 0
    totalPages.value = res.page?.totalPages ?? 0
    totalItems.value = res.page?.totalElements ?? 0
  } catch (error: unknown) {
    refunds.value = []
    currentPage.value = 0
    totalPages.value = 0
    totalItems.value = 0
    globalError.value = getErrorMessage(error, 'Không thể tải danh sách refund')
  } finally {
    isLoading.value = false
  }
}

function resetFilters() {
  selectedStatus.value = ''
  searchKeyword.value = ''
  fetchRefunds(0)
}

async function calculateRefund() {
  if (!createBookingId.value) return

  isActionLoading.value = true
  clearMessages()
  calculation.value = null

  try {
    calculation.value = await refundApi.calculate(Number(createBookingId.value))
  } catch (error: unknown) {
    globalError.value = getErrorMessage(error, 'Không thể tính số tiền hoàn')
  } finally {
    isActionLoading.value = false
  }
}

async function createRefund() {
  if (!createBookingId.value) return

  isActionLoading.value = true
  clearMessages()

  try {
    const created = await refundApi.create({
      bookingId: Number(createBookingId.value),
      reason: createReason.value.trim() || null,
    })

    successMessage.value = `Đã tạo yêu cầu hoàn tiền #${created.id}`
    calculation.value = null
    createBookingId.value = null
    createReason.value = ''

    await fetchRefunds(0)
  } catch (error: unknown) {
    globalError.value = getErrorMessage(error, 'Không thể tạo yêu cầu hoàn tiền')
  } finally {
    isActionLoading.value = false
  }
}

function openDetail(refund: RefundResponse) {
  selectedRefund.value = refund
}

function closeDetail() {
  selectedRefund.value = null
}

async function approveRefund(id: number) {
  const adminNote = globalThis.prompt(
    'Nhập ghi chú duyệt refund:',
    'Yêu cầu hợp lệ, đồng ý hoàn tiền.',
  )

  if (adminNote === null) return

  await processRefundAction(
    () => refundApi.approve(id, { adminNote }),
    'Đã duyệt yêu cầu hoàn tiền',
  )
}

async function rejectRefund(id: number) {
  const adminNote = globalThis.prompt('Nhập lý do từ chối refund:', 'Yêu cầu không hợp lệ.')

  if (adminNote === null) return

  await processRefundAction(
    () => refundApi.reject(id, { adminNote }),
    'Đã từ chối yêu cầu hoàn tiền',
  )
}

async function completeRefund(id: number) {
  const ok = globalThis.confirm(
    'Bạn chắc chắn muốn hoàn tất refund này? Booking sẽ chuyển sang CANCELLED và ghế sẽ được giải phóng.',
  )

  if (!ok) return

  const adminNote = globalThis.prompt(
    'Nhập ghi chú hoàn tất refund:',
    'Đã hoàn tất xử lý hoàn tiền.',
  )

  if (adminNote === null) return

  await processRefundAction(() => refundApi.complete(id, { adminNote }), 'Đã hoàn tất hoàn tiền')
}

async function cancelRefund(id: number) {
  const ok = globalThis.confirm('Bạn chắc chắn muốn hủy yêu cầu refund này không?')

  if (!ok) return

  await processRefundAction(() => refundApi.cancel(id), 'Đã hủy yêu cầu hoàn tiền')
}

async function processRefundAction(action: () => Promise<RefundResponse>, message: string) {
  isActionLoading.value = true
  clearMessages()

  try {
    const updated = await action()

    successMessage.value = message

    if (selectedRefund.value?.id === updated.id) {
      selectedRefund.value = updated
    }

    await fetchRefunds(currentPage.value)
  } catch (error: unknown) {
    globalError.value = getErrorMessage(error, 'Không thể xử lý refund')
  } finally {
    isActionLoading.value = false
  }
}

function formatCurrency(value?: number | string | null) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0)) + ' ₫'
}

function formatPercent(value?: number | string | null) {
  if (value === null || value === undefined || value === '') return '0%'
  return Number(value) + '%'
}

function formatDateTime(value?: string | null) {
  if (!value) return '—'

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) return '—'

  return date.toLocaleString('vi-VN')
}

function statusLabel(status: RefundStatus) {
  switch (status) {
    case 'REQUESTED':
      return 'Chờ xử lý'
    case 'APPROVED':
      return 'Đã duyệt'
    case 'REJECTED':
      return 'Từ chối'
    case 'COMPLETED':
      return 'Hoàn tất'
    case 'CANCELLED':
      return 'Đã hủy'
    default:
      return status
  }
}

function statusClass(status: RefundStatus) {
  switch (status) {
    case 'REQUESTED':
      return 'bg-yellow-100 text-yellow-800'
    case 'APPROVED':
      return 'bg-blue-100 text-blue-800'
    case 'REJECTED':
      return 'bg-red-100 text-red-700'
    case 'COMPLETED':
      return 'bg-emerald-100 text-emerald-800'
    case 'CANCELLED':
      return 'bg-slate-100 text-slate-600'
    default:
      return 'bg-slate-100 text-slate-600'
  }
}

onMounted(() => {
  fetchRefunds(0)
})
</script>
