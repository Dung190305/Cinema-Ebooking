import { ref, readonly, watch, computed, type Ref, isRef } from 'vue'
import { showtimeApi } from '@/api/showtime.api'
import { movieApi } from '@/api/movie.api'
import { roomApi } from '@/api/room.api'
import type { ShowtimeResponse, CreateShowtimeRequest, UpdateShowtimeRequest } from '@/types/showtime'
import type { RoomType } from '@/types/room'

interface ApiRejected {
  fieldErrors: Record<string, string>
  globalErrors: string[]
  message: string
}

export function useShowtime(cinemaIdInput: Ref<number | null> | number | null) {
  // Chuẩn hóa thành Ref<number | null>
  const cinemaIdRef = isRef(cinemaIdInput) ? cinemaIdInput : ref(cinemaIdInput)
  const currentCinemaId = computed(() => cinemaIdRef.value)

  const showtimes = ref<ShowtimeResponse[]>([])
  const isLoading = ref(false)
  const fieldErrors = ref<Record<string, string>>({})
  const globalErrors = ref<string[]>([])

  const currentPage = ref(0)
  const totalPages = ref(0)
  const totalItems = ref(0)
  const pageSize = 8

  const filterRoomId = ref<number | undefined>(undefined)
  const filterStatus = ref<string | undefined>(undefined)

  const nextPageCache = ref<ShowtimeResponse[]>([])
  const nextPageDirty = ref(false)

  // Cache options
  const roomOptionsCache = ref<{ id: number; label: string }[]>([])

  function handleError(err: unknown) {
    const e = err as ApiRejected
    fieldErrors.value = e.fieldErrors ?? {}
    if (e.globalErrors?.length) {
      globalErrors.value = e.globalErrors
    } else if (!Object.values(fieldErrors.value).some(Boolean)) {
      globalErrors.value = [e.message ?? 'Đã có lỗi xảy ra']
    } else {
      globalErrors.value = []
    }
  }

  function clearErrors() {
    fieldErrors.value = {}
    globalErrors.value = []
  }

  async function prefetchNextPage(page: number) {
    const cid = currentCinemaId.value
    if (!cid || isNaN(cid) || page >= totalPages.value) {
      nextPageCache.value = []
      return
    }
    try {
      const res = await showtimeApi.getPublicShowtimes({
        page,
        size: pageSize,
        sort: 'startTime,desc',
        cinemaId: cid,
        roomId: filterRoomId.value,
        status: filterStatus.value,
      })
      nextPageCache.value = res.content
      nextPageDirty.value = false
    } catch {
      nextPageCache.value = []
    }
  }

  async function fetchList(page = 0) {
    const cid = currentCinemaId.value
    isLoading.value = true
    clearErrors()
    try {
      const res = await showtimeApi.getPublicShowtimes({
        page,
        size: pageSize,
        sort: 'startTime,desc',
        cinemaId: cid ?? undefined,
        roomId: filterRoomId.value,
        status: filterStatus.value,
      })
      showtimes.value = res.content
      currentPage.value = res.page.number
      totalPages.value = res.page.totalPages
      totalItems.value = res.page.totalElements
      nextPageDirty.value = false
      prefetchNextPage(page + 1)
    } catch (err) {
      handleError(err)
    } finally {
      isLoading.value = false
    }
  }

  async function goToPage(page: number) {
    const isNextPage = page === currentPage.value + 1
    if (isNextPage && !nextPageDirty.value && nextPageCache.value.length > 0) {
      showtimes.value = nextPageCache.value
      currentPage.value = page
      nextPageCache.value = []
      prefetchNextPage(page + 1)
    } else {
      await fetchList(page)
    }
  }

  function setFilters(roomId?: number, status?: string) {
    filterRoomId.value = roomId
    filterStatus.value = status
    fetchList(0)
  }

  async function create(body: Omit<CreateShowtimeRequest, 'cinemaId'>): Promise<boolean> {
    clearErrors()
    const cid = currentCinemaId.value
    if (!cid) return false
    try {
      const created = await showtimeApi.create({ ...body, cinemaId: cid })
      showtimes.value.unshift(created)
      totalItems.value++
      totalPages.value = Math.ceil(totalItems.value / pageSize)
      if (showtimes.value.length > pageSize) {
        showtimes.value.pop()
      }
      nextPageCache.value = []
      prefetchNextPage(currentPage.value + 1)
      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

  async function cancel(item: ShowtimeResponse): Promise<boolean> {
    clearErrors()
    try {
      const updated = await showtimeApi.cancel(item.id)
      const idx = showtimes.value.findIndex(s => s.id === item.id)
      if (idx !== -1) showtimes.value[idx] = updated
      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

  // ── Option loaders ─────────────────────────────────────────
  async function loadMovies() {
    const res = await movieApi.getList({ page: 0, size: 50 })
    return res.content.map(m => ({ id: m.id, label: `${m.title} (${m.duration} phút)` }))
  }

  async function loadRooms(cid?: number) {
    const cinema = cid ?? currentCinemaId.value
    if (!cinema) return []
    const res = await roomApi.getListByCinema(cinema, 0, 30)
    const options = res.content.map(r => ({ id: r.id, label: r.name }))
    roomOptionsCache.value = options
    return options
  }

   async function loadRoomsByFormat(deps: Record<string, unknown>): Promise<{ id: number; label: string }[]> {
    const cid = currentCinemaId.value
    if (!cid) return []

    const formatId = Number(deps.formatId)
    if (!formatId) return []

    const typeMap: Record<number, RoomType> = {
      1: 'TYPE_2D',
      2: 'TYPE_3D',
      3: 'IMAX',
    }
    const targetType = typeMap[formatId]
    if (!targetType) return []

    try {
      const res = await roomApi.getListByCinema(cid, 0, 30)  // lấy toàn bộ phòng của rạp
      return res.content
        .filter(r => r.roomType === targetType)
        .map(r => ({ id: r.id, label: r.name }))
    } catch {
      return []
    }
  }

  // Khi cinemaId thay đổi, reset filter và load lại
  watch(currentCinemaId, () => {
    filterRoomId.value = undefined
    filterStatus.value = undefined
    nextPageDirty.value = true
    fetchList(0) // gọi sau khi cinemaId thật sự thay đổi
  })

  return {
    showtimes: readonly(showtimes),
    isLoading: readonly(isLoading),
    fieldErrors: readonly(fieldErrors),
    globalErrors: readonly(globalErrors),
    currentPage: readonly(currentPage),
    totalPages: readonly(totalPages),
    totalItems: readonly(totalItems),
    pageSize,
    fetchList,
    goToPage,
    setFilters,
    create,
    cancel,
    loadMovies,
    loadRooms,
    loadRoomsByFormat,
    roomOptionsCache,
  }
}