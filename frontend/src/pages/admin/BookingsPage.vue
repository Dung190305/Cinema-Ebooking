<template>
  <div class="flex flex-col gap-6 py-6">
    <!-- Header -->
    <div class="flex flex-col gap-4 pr-6">
      <div class="flex items-center text-sm">
        <span class="font-medium text-text-admin-primary">Operations</span>
        <span class="mx-2 text-text-admin-tertiary">/</span>
        <span class="text-text-admin-tertiary">Bookings</span>
      </div>

      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 class="text-lg font-semibold text-text-admin-primary">Quản lý booking</h1>
          <p class="text-sm text-text-admin-tertiary">{{ totalItems }} đơn đặt vé trong hệ thống</p>
        </div>
      </div>

      <!-- Filter -->
      <div class="grid gap-2 sm:grid-cols-2 lg:grid-cols-6">
        <!-- Movie filter -->
        <select
          v-model="selectedMovieId"
          class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
        >
          <option value="">Tất cả phim</option>

          <option v-for="movie in movies" :key="movie.id" :value="String(movie.id)">
            {{ movie.title }}
          </option>
        </select>

        <!-- Status filter -->
        <select
          v-model="selectedStatus"
          class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="PENDING">Chờ thanh toán</option>
          <option value="CONFIRMED">Đã xác nhận</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>

        <!-- From date -->
        <input
          v-model="fromDate"
          type="date"
          class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
        />

        <!-- To date -->
        <input
          v-model="toDate"
          type="date"
          class="rounded-lg border border-border-admin-default bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-accent focus:ring-2 focus:ring-slate-100"
        />

        <button
          class="inline-flex items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-medium text-text-on-accent transition hover:opacity-90"
          @click="fetchBookings(0)"
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
                Mã booking
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Phim
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Suất chiếu
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Tổng tiền
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Trạng thái
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Ngày tạo
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-text-admin-secondary">
                Hạn thanh toán
              </th>
              <th class="px-4 py-3 text-right text-xs font-medium text-text-admin-secondary">
                Hành động
              </th>
            </tr>
          </thead>

          <tbody class="divide-y divide-slate-50">
            <tr v-if="bookings.length === 0">
              <td colspan="8" class="py-12 text-center text-sm text-slate-400">
                Chưa có dữ liệu booking
              </td>
            </tr>

            <tr
              v-for="booking in bookings"
              :key="booking.bookingId"
              class="transition hover:bg-slate-50"
            >
              <td class="px-4 py-3 font-medium text-slate-800">
                {{ booking.bookingCode }}
              </td>

              <td class="px-4 py-3 text-slate-600">
                {{ booking.movieTitle || '—' }}
              </td>

              <td class="px-4 py-3 text-slate-600">
                {{ formatDateTime(booking.showtime) }}
              </td>

              <td class="px-4 py-3 font-medium text-slate-700">
                {{ formatCurrency(booking.finalAmount) }}
              </td>

              <td class="px-4 py-3">
                <span
                  class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium"
                  :class="statusClass(booking.status)"
                >
                  {{ statusLabel(booking.status) }}
                </span>
              </td>

              <td class="px-4 py-3 text-slate-500">
                {{ formatDateTime(booking.createdAt) }}
              </td>

              <td class="px-4 py-3">
                <span
                  class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium"
                  :class="paymentDeadlineClass(booking)"
                >
                  {{ paymentDeadlineText(booking) }}
                </span>
              </td>

              <td class="px-4 py-3">
                <div class="flex justify-end gap-2">
                  <button
                    class="inline-flex items-center justify-center rounded-lg border border-slate-200 p-2 text-slate-600 transition hover:bg-slate-100"
                    title="Xem chi tiết"
                    @click="openDetail(booking.bookingId)"
                  >
                    <Eye class="size-4" />
                  </button>

                  <button
                    v-if="booking.status === 'PENDING'"
                    class="inline-flex items-center justify-center rounded-lg border p-2 transition"
                    :class="
                      canCancelBooking(booking)
                        ? 'border-red-200 text-red-600 hover:bg-red-50'
                        : 'cursor-not-allowed border-slate-200 text-slate-300'
                    "
                    :disabled="!canCancelBooking(booking) || isActionLoading"
                    :title="cancelButtonTitle(booking)"
                    @click="cancelBooking(booking.bookingId)"
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
          @click="fetchBookings(page - 1)"
        >
          {{ page }}
        </button>
      </div>
    </div>

    <!-- Detail Drawer -->
    <Teleport to="body">
      <div v-if="selectedBooking" class="fixed inset-0 z-40 bg-black/20" @click="closeDetail" />

      <div
        v-if="selectedBooking"
        class="fixed inset-y-0 right-0 z-50 flex w-full max-w-xl flex-col bg-white shadow-2xl"
      >
        <!-- Drawer header -->
        <div class="flex items-center justify-between border-b border-slate-100 px-5 py-4">
          <div>
            <h2 class="text-sm font-semibold text-slate-900">Chi tiết booking</h2>
            <p class="text-xs text-slate-400">
              {{ selectedBooking.bookingCode }}
            </p>
          </div>

          <button
            class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
            @click="closeDetail"
          >
            <X class="size-5" />
          </button>
        </div>

        <!-- Drawer body -->
        <div class="flex-1 overflow-y-auto px-5 py-4">
          <div class="flex flex-col gap-5">
            <!-- Status -->
            <div class="rounded-xl border border-slate-100 bg-white p-4">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <p class="text-xs text-slate-400">Trạng thái booking</p>
                  <span
                    class="mt-1 inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium"
                    :class="statusClass(selectedBooking.status)"
                  >
                    {{ statusLabel(selectedBooking.status) }}
                  </span>
                </div>

                <div class="text-right">
                  <p class="text-xs text-slate-400">Tổng thanh toán</p>
                  <p class="mt-1 text-lg font-semibold text-slate-900">
                    {{ formatCurrency(selectedBooking.finalAmount) }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Payment deadline -->
            <div class="rounded-xl border border-slate-100 bg-white p-4">
              <h3 class="mb-3 text-sm font-semibold text-slate-800">Hạn thanh toán</h3>

              <span
                class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium"
                :class="paymentDeadlineClass(selectedBooking)"
              >
                {{ paymentDeadlineText(selectedBooking) }}
              </span>

              <p v-if="selectedBooking.status === 'PENDING'" class="mt-2 text-xs text-slate-400">
                Booking PENDING sẽ tự hủy sau 15 phút nếu chưa thanh toán. Nút hủy thủ công chỉ mở
                khi còn từ 5 phút trở xuống.
              </p>
            </div>

            <!-- Basic info -->
            <div class="rounded-xl border border-slate-100 bg-white p-4">
              <h3 class="mb-3 text-sm font-semibold text-slate-800">Thông tin chung</h3>

              <div class="space-y-2 text-sm">
                <InfoRow label="Booking ID" :value="String(selectedBooking.bookingId)" />
                <InfoRow label="User ID" :value="String(selectedBooking.userId)" />
                <InfoRow label="Phim" :value="selectedBooking.movieTitle || '—'" />
                <InfoRow label="Rạp" :value="selectedBooking.cinemaName || '—'" />
                <InfoRow label="Phòng" :value="selectedBooking.roomName || '—'" />
                <InfoRow
                  label="Suất chiếu"
                  :value="formatDateTime(selectedBooking.showtimeStartTime)"
                />
                <InfoRow label="Ngày tạo" :value="formatDateTime(selectedBooking.createdAt)" />
                <InfoRow label="Hết hạn" :value="formatDateTime(selectedBooking.expiredAt)" />
                <InfoRow label="Thanh toán lúc" :value="formatDateTime(selectedBooking.paidAt)" />
              </div>
            </div>

            <!-- Seats -->
            <div class="rounded-xl border border-slate-100 bg-white p-4">
              <h3 class="mb-3 text-sm font-semibold text-slate-800">Ghế đã đặt</h3>

              <div
                v-if="!selectedBooking.seats || selectedBooking.seats.length === 0"
                class="text-sm text-slate-400"
              >
                Không có thông tin ghế
              </div>

              <div v-else class="flex flex-wrap gap-2">
                <span
                  v-for="seat in selectedBooking.seats"
                  :key="seat.showtimeSeatId"
                  class="rounded-lg bg-slate-100 px-3 py-1.5 text-sm text-slate-700"
                >
                  {{ seat.seatName }}
                  <span class="text-slate-400">({{ seat.seatType }})</span>
                  -
                  {{ formatCurrency(seat.price) }}
                </span>
              </div>
            </div>

            <!-- Combos -->
            <div class="rounded-xl border border-slate-100 bg-white p-4">
              <h3 class="mb-3 text-sm font-semibold text-slate-800">Combo</h3>

              <div
                v-if="!selectedBooking.combos || selectedBooking.combos.length === 0"
                class="text-sm text-slate-400"
              >
                Không mua combo
              </div>

              <div v-else class="space-y-2">
                <div
                  v-for="combo in selectedBooking.combos"
                  :key="combo.comboId"
                  class="flex justify-between gap-4 text-sm"
                >
                  <span class="text-slate-600"> {{ combo.comboName }} x{{ combo.quantity }} </span>

                  <span class="font-medium text-slate-700">
                    {{ formatCurrency(combo.totalPrice) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Coupon -->
            <div class="rounded-xl border border-slate-100 bg-white p-4">
              <h3 class="mb-3 text-sm font-semibold text-slate-800">Coupon</h3>

              <div v-if="!selectedBooking.coupon" class="text-sm text-slate-400">
                Không dùng coupon
              </div>

              <div v-else class="space-y-2 text-sm">
                <InfoRow label="Mã coupon" :value="selectedBooking.coupon.code" />
                <InfoRow
                  label="Giá trị giảm"
                  :value="formatCurrency(selectedBooking.coupon.discountValue)"
                />
              </div>
            </div>

            <!-- Payment summary -->
            <div class="rounded-xl border border-slate-100 bg-white p-4">
              <h3 class="mb-3 text-sm font-semibold text-slate-800">Thanh toán</h3>

              <div class="space-y-2 text-sm">
                <InfoRow
                  label="Tiền vé"
                  :value="formatCurrency(selectedBooking.totalTicketPrice)"
                />
                <InfoRow
                  label="Tiền combo"
                  :value="formatCurrency(selectedBooking.totalComboPrice)"
                />
                <InfoRow
                  label="Giảm hạng thành viên"
                  :value="formatCurrency(selectedBooking.tierDiscountAmount)"
                />
                <InfoRow
                  label="Giảm coupon"
                  :value="formatCurrency(selectedBooking.couponDiscountAmount)"
                />
                <InfoRow
                  label="Tổng giảm"
                  :value="formatCurrency(selectedBooking.discountAmount)"
                />

                <div class="mt-2 border-t border-slate-100 pt-2">
                  <InfoRow
                    label="Tổng thanh toán"
                    :value="formatCurrency(selectedBooking.finalAmount)"
                    strong
                  />
                </div>
              </div>
            </div>

            <!-- Membership -->
            <div class="rounded-xl border border-slate-100 bg-white p-4">
              <h3 class="mb-3 text-sm font-semibold text-slate-800">Thành viên</h3>

              <div class="space-y-2 text-sm">
                <InfoRow label="Hạng" :value="selectedBooking.membershipTierName || 'Không có'" />
                <InfoRow
                  label="Giảm giá hạng"
                  :value="formatPercent(selectedBooking.membershipDiscountPercent)"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Drawer footer -->
        <div class="border-t border-slate-100 px-5 pb-8 pt-4">
          <button
            v-if="selectedBooking.status === 'PENDING'"
            class="w-full rounded-lg border py-2.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-60"
            :class="
              canCancelBooking(selectedBooking)
                ? 'border-red-200 text-red-600 hover:bg-red-50'
                : 'border-slate-200 text-slate-300'
            "
            :disabled="!canCancelBooking(selectedBooking) || isActionLoading"
            @click="cancelBooking(selectedBooking.bookingId)"
          >
            {{
              isActionLoading
                ? 'Đang xử lý...'
                : canCancelBooking(selectedBooking)
                  ? 'Hủy booking'
                  : cancelButtonTitle(selectedBooking)
            }}
          </button>

          <p v-else class="rounded-lg bg-slate-50 p-3 text-center text-xs text-slate-500">
            Booking này không ở trạng thái chờ thanh toán nên không thể hủy.
          </p>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { defineComponent, h, onMounted, onUnmounted, ref } from 'vue'
import { Ban, Eye, RotateCcw, Search, X } from 'lucide-vue-next'
import { bookingApi } from '@/api/booking.api'
import { movieApi } from '@/api/movie.api'
import type { MovieResponse } from '@/types/movie.types'
import type {
  BookingDetailResponse,
  BookingListItemResponse,
  BookingStatus,
} from '@/types/booking.types'

const bookings = ref<BookingListItemResponse[]>([])
const selectedBooking = ref<BookingDetailResponse | null>(null)

const movies = ref<MovieResponse[]>([])

const isLoading = ref(false)
const isActionLoading = ref(false)
const globalError = ref('')

const selectedMovieId = ref('')
const selectedStatus = ref<BookingStatus | ''>('')
const fromDate = ref('')
const toDate = ref('')

const currentPage = ref(0)
const totalPages = ref(0)
const totalItems = ref(0)

const pageSize = 10

const PAYMENT_EXPIRE_MINUTES = 15
const CANCEL_ENABLE_REMAINING_MINUTES = 5

const now = ref(new Date())
let timerId: ReturnType<typeof globalThis.setInterval> | undefined

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

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error) return error.message
  return fallback
}

function formatDateInput(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

function setDefaultDateRange() {
  const current = new Date()
  const firstDay = new Date(current.getFullYear(), current.getMonth(), 1)
  const lastDay = new Date(current.getFullYear(), current.getMonth() + 1, 0)

  fromDate.value = formatDateInput(firstDay)
  toDate.value = formatDateInput(lastDay)
}

async function fetchMovies() {
  try {
    const res = await movieApi.getList({
      page: 0,
      size: 100,
      status: 'NOW_SHOWING',
    })

    movies.value = res.content || []
  } catch (error: unknown) {
    globalError.value = getErrorMessage(error, 'Không thể tải danh sách phim')
  }
}

async function fetchBookings(page = 0) {
  isLoading.value = true
  globalError.value = ''

  try {
    const res = await bookingApi.getAdminList({
      movieId: selectedMovieId.value ? Number(selectedMovieId.value) : '',
      status: selectedStatus.value,
      fromDate: fromDate.value,
      toDate: toDate.value,
      page,
      size: pageSize,
      sort: 'createdAt,desc',
    })

    bookings.value = res.content || []
    currentPage.value = res.page?.number ?? 0
    totalPages.value = res.page?.totalPages ?? 0
    totalItems.value = res.page?.totalElements ?? 0
  } catch (error: unknown) {
    bookings.value = []
    currentPage.value = 0
    totalItems.value = 0
    totalPages.value = 0
    globalError.value = getErrorMessage(error, 'Không thể tải danh sách booking')
  } finally {
    isLoading.value = false
  }
}

async function resetFilters() {
  selectedMovieId.value = ''
  selectedStatus.value = ''
  setDefaultDateRange()
  await fetchBookings(0)
}

async function openDetail(id: number) {
  globalError.value = ''

  try {
    selectedBooking.value = await bookingApi.getById(id)
  } catch (error: unknown) {
    globalError.value = getErrorMessage(error, 'Không thể tải chi tiết booking')
  }
}

function closeDetail() {
  selectedBooking.value = null
}

function getBookingCreatedAtTime(booking: { createdAt?: string | null }) {
  if (!booking.createdAt) return null

  const date = new Date(booking.createdAt)

  if (Number.isNaN(date.getTime())) return null

  return date.getTime()
}

function getPaymentExpiredAtTime(booking: { createdAt?: string | null }) {
  const createdAtTime = getBookingCreatedAtTime(booking)

  if (!createdAtTime) return null

  return createdAtTime + PAYMENT_EXPIRE_MINUTES * 60_000
}

function getRemainingMsBeforeExpired(booking: { createdAt?: string | null }) {
  const expiredAtTime = getPaymentExpiredAtTime(booking)

  if (!expiredAtTime) return 0

  return expiredAtTime - now.value.getTime()
}

function getRemainingMinutesBeforeExpired(booking: { createdAt?: string | null }) {
  const remainingMs = getRemainingMsBeforeExpired(booking)

  return Math.ceil(remainingMs / 60_000)
}

function formatRemainingTime(remainingMs: number) {
  if (remainingMs <= 0) return '00:00'

  const totalSeconds = Math.floor(remainingMs / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60

  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

function paymentDeadlineText(booking: { status: BookingStatus; createdAt?: string | null }) {
  if (booking.status === 'CONFIRMED') {
    return 'Đã thanh toán'
  }

  if (booking.status === 'CANCELLED') {
    return 'Đã hủy'
  }

  if (booking.status !== 'PENDING') {
    return 'Không xác định'
  }

  const remainingMs = getRemainingMsBeforeExpired(booking)

  if (remainingMs <= 0) {
    return 'Đang chờ tự hủy'
  }

  return `Còn ${formatRemainingTime(remainingMs)}`
}

function paymentDeadlineClass(booking: { status: BookingStatus; createdAt?: string | null }) {
  if (booking.status === 'CONFIRMED') {
    return 'bg-emerald-100 text-emerald-800'
  }

  if (booking.status === 'CANCELLED') {
    return 'bg-slate-100 text-slate-600'
  }

  if (booking.status !== 'PENDING') {
    return 'bg-slate-100 text-slate-600'
  }

  const remainingMinutes = getRemainingMinutesBeforeExpired(booking)

  if (remainingMinutes <= 0) {
    return 'bg-red-100 text-red-700'
  }

  if (remainingMinutes <= CANCEL_ENABLE_REMAINING_MINUTES) {
    return 'bg-orange-100 text-orange-700'
  }

  return 'bg-yellow-100 text-yellow-800'
}

function canCancelBooking(booking: { status: BookingStatus; createdAt?: string | null }) {
  if (booking.status !== 'PENDING') return false

  const remainingMinutes = getRemainingMinutesBeforeExpired(booking)

  return remainingMinutes <= CANCEL_ENABLE_REMAINING_MINUTES
}

function cancelButtonTitle(booking: { status: BookingStatus; createdAt?: string | null }) {
  if (booking.status !== 'PENDING') {
    return 'Booking này không thể hủy'
  }

  const remainingMinutes = getRemainingMinutesBeforeExpired(booking)

  if (remainingMinutes <= 0) {
    return 'Booking đã quá hạn, có thể hủy thủ công'
  }

  if (remainingMinutes <= CANCEL_ENABLE_REMAINING_MINUTES) {
    return 'Có thể hủy booking'
  }

  return `Chỉ có thể hủy khi còn dưới ${CANCEL_ENABLE_REMAINING_MINUTES} phút`
}

async function cancelBooking(id: number) {
  const ok = globalThis.confirm('Bạn có chắc muốn hủy booking này không?')

  if (!ok) return

  isActionLoading.value = true
  globalError.value = ''

  try {
    await bookingApi.cancel(id)

    selectedBooking.value = null
    await fetchBookings(currentPage.value)
  } catch (error: unknown) {
    globalError.value = getErrorMessage(error, 'Không thể hủy booking')
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

function statusLabel(status: BookingStatus) {
  switch (status) {
    case 'PENDING':
      return 'Chờ thanh toán'
    case 'CONFIRMED':
      return 'Đã xác nhận'
    case 'CANCELLED':
      return 'Đã hủy'
    default:
      return status
  }
}

function statusClass(status: BookingStatus) {
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-800'
    case 'CONFIRMED':
      return 'bg-emerald-100 text-emerald-800'
    case 'CANCELLED':
      return 'bg-red-100 text-red-700'
    default:
      return 'bg-slate-100 text-slate-600'
  }
}

onMounted(async () => {
  setDefaultDateRange()
  await fetchMovies()
  await fetchBookings(0)

  timerId = globalThis.setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timerId) {
    globalThis.clearInterval(timerId)
  }
})
</script>
