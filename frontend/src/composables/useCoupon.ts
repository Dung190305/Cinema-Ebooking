import { computed, readonly, ref } from 'vue'
import { couponApi } from '@/api/coupon.api'
import type {
  CouponResponse,
  CouponStatus,
  CouponType,
  CreateCouponRequest,
  UpdateCouponRequest,
  UpdateDraftCouponRequest
} from '@/types/coupon.types'
import type { NestedPage } from '@/types/common.types'

interface ApiRejected {
  fieldErrors: Record<string, string>
  globalErrors: string[]
  message: string
}

export function useCoupon() {
  const coupons = ref<CouponResponse[]>([])
  const isLoading = ref(false)
  const fieldErrors = ref<Record<string, string>>({})
  const globalErrors = ref<string[]>([])

  const currentPage = ref(0)
  const totalPages = ref(0)
  const totalItems = ref(0)
  const pageSize = 10

  const selectedType = ref<CouponType | 'ALL'>('ALL')
  const selectedStatus = ref<CouponStatus | 'ALL'>('ALL')
  const onlyUnused = ref(false)

  const filteredCoupons = computed(() =>
    coupons.value.filter((coupon) => {
      const matchesType = selectedType.value === 'ALL' || coupon.type === selectedType.value
      const matchesStatus = selectedStatus.value === 'ALL' || coupon.status === selectedStatus.value
      const matchesUsage = !onlyUnused.value || (coupon.remainingUsage ?? 1) === 0

      return matchesType && matchesStatus && matchesUsage
    }),
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
      const res = (await couponApi.getList(page, pageSize)) as unknown as NestedPage<CouponResponse>
      coupons.value = res.content
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

  async function create(body: CreateCouponRequest): Promise<boolean> {
    clearErrors()
    try {
      const created = (await couponApi.create(body)) as unknown as CouponResponse
      coupons.value.unshift(created)
      totalItems.value += 1
      totalPages.value = Math.ceil(totalItems.value / pageSize)
      if (coupons.value.length > pageSize) coupons.value.pop()
      return true
    } catch (err) {
      handleError(err)
      return false
    }
  }

async function refreshOne(id: number) {

  const fresh =
    await couponApi.getById(id) as CouponResponse

  const idx = coupons.value.findIndex(
    (coupon) => coupon.id === fresh.id,
  )

  if (idx !== -1) {
    coupons.value[idx] = fresh
  }

  return fresh
}

  async function update(
    item: CouponResponse,
  ): Promise<boolean> {

    clearErrors()

    const body: UpdateCouponRequest = {
      usageLimit: Number(item.usageLimit),
      endDate: item.endDate,
    }

    try {

      await couponApi.update(item.id, body)

      await refreshOne(item.id)

      return true

    } catch (err) {

      handleError(err)
      return false
    }
  }

  async function updateDraft(
    item: CouponResponse,
  ): Promise<boolean> {

    clearErrors()

    const body: UpdateDraftCouponRequest = {
      code: item.code,
      type: item.type,
      value: Number(item.value),
      usageLimit: Number(item.usageLimit),
      perUserUsage: Number(item.perUserUsage),
      pointsToRedeem: Number(item.pointsToRedeem),
      minimumBookingValue: Number(item.minimumBookingValue),
      maximumDiscountAmount: Number(item.maximumDiscountAmount),
      startDate: item.startDate,
      endDate: item.endDate,
    }
    try {

      await couponApi.updateDraft(
        item.id,
        body,
      )

      await refreshOne(item.id)

      return true

    } catch (err) {

      handleError(err)
      return false
    }
  }

  async function refreshOne(id: number) {
    const fresh = (await couponApi.getById(id)) as unknown as CouponResponse
    const idx = coupons.value.findIndex((coupon) => coupon.id === fresh.id)
    if (idx !== -1) coupons.value[idx] = fresh
    return fresh
  }

  async function activate(item: CouponResponse): Promise<CouponResponse | null> {
    clearErrors()
    try {
      await couponApi.activate(item.id)
      return await refreshOne(item.id)
    } catch (err) {
      handleError(err)
      return null
    }
  }

  async function disable(item: CouponResponse): Promise<CouponResponse | null> {
    clearErrors()
    try {
      await couponApi.disable(item.id)
      return await refreshOne(item.id)
    } catch (err) {
      handleError(err)
      return null
    }
  }

  return {
    coupons: readonly(coupons),
    filteredCoupons,
    isLoading: readonly(isLoading),
    fieldErrors: readonly(fieldErrors),
    globalErrors: readonly(globalErrors),
    currentPage: readonly(currentPage),
    totalPages: readonly(totalPages),
    totalItems: readonly(totalItems),
    pageSize,
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
  }
}