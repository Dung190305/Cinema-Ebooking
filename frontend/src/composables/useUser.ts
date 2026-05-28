// src/composables/useUser.ts
import { ref, readonly } from 'vue'
import { userAdminApi } from '@/api/user-admin.api'
import type {
  UserResponse,
  UserDetailResponse,
  UserFilterParams,
} from '@/types/user.types'

interface ApiRejected {
  fieldErrors: Record<string, string>
  globalErrors: string[]
  message: string
}

interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function useUser() {
  // ── List state ───────────────────────────────────────────────────────────────
  const users = ref<UserResponse[]>([])
  const isLoading = ref(false)
  const fieldErrors = ref<Record<string, string>>({})
  const globalErrors = ref<string[]>([])

  const currentPage = ref(0)
  const totalPages = ref(0)
  const totalItems = ref(0)
  const pageSize = 8

  // ── Active filter state ──────────────────────────────────────────────────────
  const activeFilter = ref<UserFilterParams>({})

  // ── Prefetch state ───────────────────────────────────────────────────────────
  const nextPageCache = ref<UserResponse[]>([])
  const nextPageDirty = ref(false)

  // ── Detail state ─────────────────────────────────────────────────────────────
  const selectedUser = ref<UserDetailResponse | null>(null)
  const isLoadingDetail = ref(false)

  // ── Action state ─────────────────────────────────────────────────────────────
  const isActionLoading = ref(false)

  // ── Helpers ──────────────────────────────────────────────────────────────────
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

  // ── Core fetch ───────────────────────────────────────────────────────────────
  async function fetchList(page = 0, filter: UserFilterParams = {}) {
    isLoading.value = true
    clearErrors()
    try {
      const res = await userAdminApi.getList({
        page,
        size: pageSize,
        fullName: filter.fullName || undefined,
        email: filter.email || undefined,
        role: filter.role || undefined,
        status: filter.status || undefined,
      })
      users.value = res.content
      currentPage.value = res.number
      totalPages.value = res.totalPages
      totalItems.value = res.totalElements
      activeFilter.value = { ...filter }
      nextPageDirty.value = false

      prefetchNextPage(page + 1, filter)
    } catch (err) {
      handleError(err)
    } finally {
      isLoading.value = false
    }
  }

  async function prefetchNextPage(page: number, filter: UserFilterParams) {
    if (page >= totalPages.value) {
      nextPageCache.value = []
      return
    }
    try {
      const res = await userAdminApi.getList({
        page,
        size: pageSize,
        fullName: filter.fullName || undefined,
        email: filter.email || undefined,
        role: filter.role || undefined,
        status: filter.status || undefined,
      })
      nextPageCache.value = res.content
      nextPageDirty.value = false
    } catch {
      nextPageCache.value = []
    }
  }

  // ── Navigate ──────────────────────────────────────────────────────────────────
  async function goToPage(page: number) {
    if (page < 0 || page >= totalPages.value) return

    const isNextPage = page === currentPage.value + 1
    const filter = activeFilter.value

    if (isNextPage && !nextPageDirty.value && nextPageCache.value.length > 0) {
      users.value = nextPageCache.value
      currentPage.value = page
      nextPageCache.value = []
      prefetchNextPage(page + 1, filter)
    } else {
      await fetchList(page, filter)
    }
  }

  // ── Search + filter ──────────────────────────────────────────────────────────
  async function search(filter: UserFilterParams) {
    await fetchList(0, filter)
  }

  async function clearFilters() {
    await fetchList(0, {})
  }

  // ── Detail ───────────────────────────────────────────────────────────────────
  async function fetchUserDetail(id: number) {
    isLoadingDetail.value = true
    clearErrors()
    try {
      selectedUser.value = await userAdminApi.getById(id)
    } catch (err) {
      handleError(err)
      selectedUser.value = null
    } finally {
      isLoadingDetail.value = false
    }
  }

  // ── Status actions ───────────────────────────────────────────────────────────
  async function activateUser(id: number): Promise<boolean> {
    isActionLoading.value = true
    clearErrors()
    try {
      await userAdminApi.activate(id)
      updateUserInList(id, { status: 'ACTIVE' })
      if (selectedUser.value?.id === id) {
        selectedUser.value = { ...selectedUser.value, status: 'ACTIVE' }
      }
      return true
    } catch (err) {
      handleError(err)
      return false
    } finally {
      isActionLoading.value = false
    }
  }

  async function deactivateUser(id: number): Promise<boolean> {
    isActionLoading.value = true
    clearErrors()
    try {
      await userAdminApi.deactivate(id)
      updateUserInList(id, { status: 'INACTIVE' })
      if (selectedUser.value?.id === id) {
        selectedUser.value = { ...selectedUser.value, status: 'INACTIVE' }
      }
      return true
    } catch (err) {
      handleError(err)
      return false
    } finally {
      isActionLoading.value = false
    }
  }

  function updateUserInList(id: number, patch: Partial<UserResponse>) {
    const idx = users.value.findIndex(u => u.id === id)
    if (idx !== -1) {
      users.value[idx] = { ...users.value[idx], ...patch }
    }
  }

  return {
    users: readonly(users),
    isLoading: readonly(isLoading),
    fieldErrors: readonly(fieldErrors),
    globalErrors: readonly(globalErrors),
    currentPage: readonly(currentPage),
    totalPages: readonly(totalPages),
    totalItems: readonly(totalItems),
    pageSize,
    activeFilter: readonly(activeFilter),
    selectedUser: readonly(selectedUser),
    isLoadingDetail: readonly(isLoadingDetail),
    isActionLoading: readonly(isActionLoading),

    fetchList,
    search,
    clearFilters,
    goToPage,
    fetchUserDetail,
    activateUser,
    deactivateUser,
  }
}
