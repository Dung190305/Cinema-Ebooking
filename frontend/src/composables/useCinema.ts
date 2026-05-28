import { ref, readonly } from 'vue'
import { cinemaApi } from '../api/cinema.api'
import type { CinemaResponse, CreateCinemaRequest, UpdateCinemaRequest } from '../types/cinema'

interface ApiRejected {
  fieldErrors: Record<string, string>
  globalErrors: string[]
  message: string
}

export function useCinema() {

  // ── State ──────────────────────────────────────────────────────────────────
  const cinemas     = ref<CinemaResponse[]>([])
  const isLoading   = ref(false)
  const fieldErrors   = ref<Record<string, string>>({})
  const globalErrors  = ref<string[]>([])

  const currentPage = ref(0)
  const totalPages  = ref(0)
  const totalItems  = ref(0)
  const pageSize    = 8

  // ── Prefetch state ─────────────────────────────────────────────────────────
  // nextPageCache: data của trang kế, được fetch sẵn trong background
  const nextPageCache = ref<CinemaResponse[]>([])

  // nextPageDirty: true khi cache thiếu item do bị fill lên trang hiện tại
  // → phải refetch khi user navigate tới trang đó
  const nextPageDirty = ref(false)

  // ── Helpers ────────────────────────────────────────────────────────────────
  function handleError(err: unknown) {
    const e = err as ApiRejected
    fieldErrors.value  = e.fieldErrors  ?? {}
    if (e.globalErrors?.length) {
      globalErrors.value = e.globalErrors
    } else if (!Object.values(fieldErrors.value).some(Boolean)) {
      globalErrors.value = [e.message ?? 'Đã có lỗi xảy ra'] 
    } else {
      globalErrors.value = []
    }
  }

  function clearErrors() {
    fieldErrors.value  = {}
    globalErrors.value = []
  }

  // Fetch trang kế trong background — không block UI, không set isLoading
  async function prefetchNextPage(page: number) {
    if (page >= totalPages.value) {
      nextPageCache.value = []
      return
    }
    try {
      const res = await cinemaApi.getList(page, pageSize)
      nextPageCache.value = res.content
      nextPageDirty.value = false
    } catch {
      nextPageCache.value = []
    }
  }

  // ── Core fetch ─────────────────────────────────────────────────────────────
  async function fetchList(page = 0) {
    isLoading.value = true
    clearErrors()
    try {
      const res = await cinemaApi.getList(page, pageSize)
      cinemas.value     = res.content
      currentPage.value = res.page.number
      totalPages.value  = res.page.totalPages
      totalItems.value  = res.page.totalElements
      nextPageDirty.value = false

      // Prefetch trang kế ngay sau khi fetch xong — chạy background
      prefetchNextPage(page + 1)
    } catch (err) {
      handleError(err)
    } finally {
      isLoading.value = false
    }
  }

  // ── Navigate ───────────────────────────────────────────────────────────────
  // Dùng hàm này thay vì gọi fetchList trực tiếp từ pagination UI
  async function goToPage(page: number) {
    const isNextPage = page === currentPage.value + 1

    if (isNextPage && !nextPageDirty.value && nextPageCache.value.length > 0) {
      // Cache còn sạch → dùng luôn, 0 API call
      cinemas.value     = nextPageCache.value
      currentPage.value = page
      nextPageCache.value = []

      // Prefetch trang kế tiếp theo background
      prefetchNextPage(page + 1)
    } else {
      // Trang trước / cache dirty / không có cache → fetch bình thường
      await fetchList(page)
    }
  }

  // ── Create ─────────────────────────────────────────────────────────────────
  async function create(body: CreateCinemaRequest): Promise<boolean> {
    clearErrors()
    try {
      const created = await cinemaApi.create(body)

      cinemas.value.unshift(created)
      totalItems.value++
      totalPages.value = Math.ceil(totalItems.value / pageSize)

      // Trang hiện tại tràn → pop item cuối (nó đã dời sang trang sau)
      if (cinemas.value.length > pageSize) {
        cinemas.value.pop()
      }

      // Cache trang sau không còn đúng thứ tự → invalidate và prefetch lại
      nextPageCache.value = []
      prefetchNextPage(currentPage.value + 1)

      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

  // ── Save ───────────────────────────────────────────────────────────────────
  async function save(item: CinemaResponse): Promise<boolean> {
    clearErrors()
    const body: UpdateCinemaRequest = {
      name: item.name, address: item.address,
      city: item.city, status: item.status,
    }
    try {
      const updated = await cinemaApi.update(item.id, body)
      const idx = cinemas.value.findIndex((c) => c.id === updated.id)
      if (idx !== -1) cinemas.value[idx] = updated
      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

  // ── Remove ─────────────────────────────────────────────────────────────────
  async function remove(item: CinemaResponse): Promise<boolean> {
    clearErrors()
    try {
      await cinemaApi.delete(item.id)

      cinemas.value = cinemas.value.filter((c) => c.id !== item.id)
      totalItems.value--
      const newTotalPages = Math.ceil(totalItems.value / pageSize) || 1
      totalPages.value = newTotalPages

      if (nextPageCache.value.length > 0) {
        // Lấy item đầu của trang sau fill vào cuối trang hiện tại
        const [fillItem, ...rest] = nextPageCache.value
        cinemas.value.push(fillItem)
        nextPageCache.value = rest

        // Cache giờ thiếu 1 item so với backend
        // → đánh dấu dirty để khi navigate sẽ refetch thay vì dùng cache
        nextPageDirty.value = true

      } else if (currentPage.value >= newTotalPages) {
        // Đang ở trang cuối và trang này đã hết item → về trang trước
        await fetchList(newTotalPages - 1)
      }

      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

  // ── Fetch All (for filter dropdown) ─────────────────────────────────────────
  async function fetchAll(): Promise<CinemaResponse[]> {
    try {
      const res = await cinemaApi.getAll(100)
      return res.content
    } catch {
      return []
    }
  }

  function setCinemas(data: CinemaResponse[]) {
    cinemas.value = data
  }

  return {
    cinemas:      readonly(cinemas),
    isLoading:    readonly(isLoading),
    fieldErrors:  readonly(fieldErrors),
    globalErrors: readonly(globalErrors),
    currentPage:  readonly(currentPage),
    totalPages:   readonly(totalPages),
    totalItems:   readonly(totalItems),
    pageSize,

    fetchList,
    setCinemas,
    fetchAll,  // ← for dashboard filter dropdown
    goToPage,   // ← dùng cái này cho pagination buttons
    create,
    save,
    remove,
  }
}