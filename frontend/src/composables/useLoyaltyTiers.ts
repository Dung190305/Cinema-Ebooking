// composables/useLoyaltyTiers.ts
import { ref, computed, onMounted } from 'vue'
import { loyaltyApi } from '@/api/loyalty.api'
import { membershipTierApi } from '@/api/membership-tier.api'
import type { LoyaltyAccountResponse } from '@/types/loyalty.types'
import type { MembershipTierResponse } from '@/types/membership-tier'

interface RejectedError {
    fieldErrors?: Record<string, string>
    globalErrors?: string[]
    message?: string
}

export function useLoyaltyTiers() {
    const loyalty = ref<LoyaltyAccountResponse | null>(null)
    const currentPoints = computed(() => loyalty.value?.currentPoints ?? 0)
    const totalSpending = computed(() => loyalty.value?.totalSpending ?? 0)
    const currentTier = ref<MembershipTierResponse | null>(null)
    const nextTier = ref<MembershipTierResponse | null>(null)
    const loading = ref(true)
    const error = ref<string | null>(null)

    const lifetimePoints = computed(() => loyalty.value?.lifetimePoints ?? 0)
    const currentTierName = computed(() => currentTier.value?.name ?? '...')
    const discountPercent = computed(() => currentTier.value?.discountPercent)
    const isHighestTier = computed(() => nextTier.value === null)

    const nextTierPointsRequired = computed(() =>
        nextTier.value ? Math.ceil(nextTier.value.minSpendingRequired / 1000) : Infinity
    )

    const pointsToNext = computed(() =>
        nextTier.value ? Math.max(0, nextTierPointsRequired.value - lifetimePoints.value) : 0
    )

    const progressPercent = computed(() =>
        nextTier.value ? Math.min(100, (lifetimePoints.value / nextTierPointsRequired.value) * 100) : 100
    )

    const nextTierName = computed(() => nextTier.value?.name);

    const fetchLoyaltyData = async () => {
        loading.value = true
        error.value = null
        try {
            const loyaltyRes = await loyaltyApi.getMyAccount()
            loyalty.value = loyaltyRes

            const tierRes = await membershipTierApi.getById(loyaltyRes.tierId)
            currentTier.value = tierRes

            const allTiers = await membershipTierApi.getList()
            const sorted = allTiers.content.sort((a, b) => a.minSpendingRequired - b.minSpendingRequired)
            const idx = sorted.findIndex(t => t.id === loyaltyRes.tierId)
            nextTier.value = sorted[idx + 1] ?? null

        } catch (err: unknown) {
            const e = err as RejectedError
            if (e.globalErrors?.length) {
                error.value = e.globalErrors.join('. ')
            } else if (e.message) {
                error.value = e.message
            } else {
                error.value = 'Không thể tải thông tin thẻ thành viên.'
            }
        } finally {
        loading.value = false
        }
    }

    onMounted(fetchLoyaltyData)

    return {
        loyalty,
        currentTier,
        nextTier,
        loading,
        error,
        lifetimePoints,
        currentTierName,
        discountPercent,
        isHighestTier,
        nextTierPointsRequired,
        pointsToNext,
        progressPercent,
        nextTierName,
        currentPoints,
        totalSpending,
    }
}