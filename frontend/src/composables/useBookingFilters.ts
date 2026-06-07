// composables/useBookingFilters.ts
import { ref, reactive, toRefs } from 'vue'
import { bookingApi } from '@/api/booking.api'
import type { BookingListItemResponse, BookingStatus } from '@/types/booking.types'
import type { NestedPage } from '@/types/common.types'

export function useBookingFilters() {
  const loading = ref(false)
  const bookings = ref<BookingListItemResponse[]>([])
  const pageable = reactive({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0,
  })

  // Filter states – using Date objects for CalendarPicker binding
  const status = ref<BookingStatus | ''>('CONFIRMED')
  const fromDate = ref<Date | null>(null)
  const toDate = ref<Date | null>(null)

  // Helper to format Date to YYYY-MM-DD for API
  const formatDateForApi = (date: Date | null): string | undefined => {
    if (!date) return undefined
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const fetchBookings = async () => {
    loading.value = true
    try {
      const res = await bookingApi.getMyHistory({
        status: status.value || undefined,
        fromDate: formatDateForApi(fromDate.value),
        toDate: formatDateForApi(toDate.value),
        page: pageable.page,
        size: pageable.size,
      })
      bookings.value = res.content
      pageable.totalPages = res.totalPages
      pageable.totalElements = res.totalElements
    } catch (error) {
      console.error('Lỗi tải lịch sử đặt vé:', error)
    } finally {
      loading.value = false
    }
  }

  const handlePageChange = (newPage: number) => {
    pageable.page = newPage
    fetchBookings()
  }

  const handleFilter = () => {
    pageable.page = 0
    fetchBookings()
  }

  const clearFilters = () => {
    status.value = 'CONFIRMED'
    fromDate.value = null
    toDate.value = null
    handleFilter()
  }

  return {
    loading,
    bookings,
    pageable: toRefs(pageable), 
    status,
    fromDate,
    toDate,
    fetchBookings,
    handlePageChange,
    handleFilter,
    clearFilters,
  }
}