// src/composables/useCombo.ts
import { computed, readonly, ref } from 'vue'
import { comboApi } from '@/api/combo.api'
import type {
  ComboResponse,
  ComboDisplayStatus,
  CreateComboRequest,
} from '@/types/combo.types'
import type { NestedPage } from '@/types/common.types'

interface ApiRejected {
  fieldErrors: Record<string, string>
  globalErrors: string[]
  message: string
}

export function useCombo() {
  const combos = ref<ComboResponse[]>([])
  const isLoading = ref(false)
  const fieldErrors = ref<Record<string, string>>({})
  const globalErrors = ref<string[]>([])

  const currentPage = ref(0)
  const totalPages = ref(0)
  const totalItems = ref(0)
  const pageSize = 10

  const searchQuery = ref('')
  const selectedStatus = ref<ComboDisplayStatus | 'ALL'>('ALL')

  const filteredCombos = computed(() =>
    combos.value.filter((combo) => {
      const matchesSearch =
        !searchQuery.value ||
        combo.name.toLowerCase().includes(searchQuery.value.toLowerCase())

      const matchesStatus =
        selectedStatus.value === 'ALL' ||
        combo.displayStatus === selectedStatus.value

      return matchesSearch && matchesStatus
    })
  )

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

  async function fetchList(page = 0) {
    isLoading.value = true
    clearErrors()
    try {
      const res = (await comboApi.getList(page, pageSize)) as unknown as NestedPage<ComboResponse>
      combos.value = res.content
      currentPage.value = res.page.number
      totalPages.value = res.page.totalPages
      totalItems.value = res.page.totalElements
    } catch (err) {
      handleError(err)
    } finally {
      isLoading.value = false
    }
  }

  async function goToPage(page: number) {
    await fetchList(page)
  }

  async function create(body: CreateComboRequest): Promise<boolean> {
    clearErrors()
    try {
      const created = (await comboApi.create(body)) as unknown as ComboResponse
      combos.value.unshift(created)
      totalItems.value += 1
      totalPages.value = Math.ceil(totalItems.value / pageSize)
      if (combos.value.length > pageSize) combos.value.pop()
      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

  async function refreshOne(id: number) {
    const fresh = (await comboApi.getById(id)) as unknown as ComboResponse
    const idx = combos.value.findIndex((combo) => combo.id === fresh.id)
    if (idx !== -1) combos.value[idx] = fresh
    return fresh
  }

  async function update(item: ComboResponse): Promise<boolean> {
    clearErrors()
    const body = {
      name: item.name,
      description: item.description,
      price: Number(item.price),
      stock: Number(item.stock),
      imageUrl: item.imageUrl,
      status: item.status,
    }
    try {
      await comboApi.update(item.id, body)
      await refreshOne(item.id)
      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

  async function deleteItem(id: number): Promise<boolean> {
    clearErrors()
    try {
      await comboApi.delete(id)
      // Xoá khỏi danh sách hiện tại và cập nhật phân trang
      combos.value = combos.value.filter((c) => c.id !== id)
      totalItems.value = Math.max(0, totalItems.value - 1)
      totalPages.value = Math.ceil(totalItems.value / pageSize)
      // Nếu trang hiện tại trống và không phải trang đầu, lùi về trang trước
      if (combos.value.length === 0 && currentPage.value > 0) {
        await fetchList(currentPage.value - 1)
      }
      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

  async function activate(item: ComboResponse): Promise<ComboResponse | null> {
    clearErrors()
    try {
      const updated = { ...item, status: 'ACTIVE' as const }
      await comboApi.update(item.id, updated)
      return await refreshOne(item.id)
    } catch (err) {
      handleError(err)
      return null
    }
  }

  async function deactivate(item: ComboResponse): Promise<ComboResponse | null> {
    clearErrors()
    try {
      const updated = { ...item, status: 'INACTIVE' as const }
      await comboApi.update(item.id, updated)
      return await refreshOne(item.id)
    } catch (err) {
      handleError(err)
      return null
    }
  }

  return {
    combos: readonly(combos),
    filteredCombos,
    isLoading: readonly(isLoading),
    fieldErrors: readonly(fieldErrors),
    globalErrors: readonly(globalErrors),
    currentPage: readonly(currentPage),
    totalPages: readonly(totalPages),
    totalItems: readonly(totalItems),
    pageSize,
    searchQuery,
    selectedStatus,
    fetchList,
    goToPage,
    create,
    update,
    deleteItem,
    activate,
    deactivate,
  }
}