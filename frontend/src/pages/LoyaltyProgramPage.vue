<!-- pages/LoyaltyProgramPage.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { membershipTierApi } from '@/api/membership-tier.api'
import { earningRuleApi } from '@/api/earning-rule.api'
import type { MembershipTierResponse } from '@/types/membership-tier'
import type { EarningRuleResponse } from '@/types/earning-rule'
import { formatVND } from '@/utils/currency'
import Skeleton from '@/components/ui/skeleton/Skeleton.vue'
import LoyaltyTierCard from '@/components/loyalty/LoyaltyTierCard.vue'

interface TierWithRules extends MembershipTierResponse {
    rules: EarningRuleResponse[]
}

const loading = ref(true)
const error = ref<string | null>(null)
const tiersWithRules = ref<TierWithRules[]>([])

// Skeleton configuration for loading state
const skeletonBlocks = [
    { type: 'text', width: '200px', height: '32px', class: 'mb-6' },
    { type: 'box', height: '380px', repeat: 3, class: 'rounded-2xl' }
]

const fetchProgramData = async () => {
    loading.value = true
    error.value = null
    try {
        // Fetch all membership tiers (size 100 to get all)
        const tiersRes = await membershipTierApi.getList(0, 100)
        let tiers = tiersRes.content
        // Sort by minSpendingRequired (lowest first)
        tiers = tiers.sort((a, b) => a.minSpendingRequired - b.minSpendingRequired)

        // Fetch all earning rules
        const rules = await earningRuleApi.getAll()

        // Group rules by tierId
        const rulesByTier = new Map<number, EarningRuleResponse[]>()
        rules.forEach(rule => {
            if (!rulesByTier.has(rule.tierId)) {
                rulesByTier.set(rule.tierId, [])
            }
            rulesByTier.get(rule.tierId)!.push(rule)
        })

        // Combine tiers with their rules
        tiersWithRules.value = tiers.map(tier => ({
            ...tier,
            rules: rulesByTier.get(tier.id) || []
        }))
    } catch (err: any) {
        console.error('Failed to fetch loyalty program:', err)
        error.value = err?.message || 'Không thể tải chương trình thành viên. Vui lòng thử lại sau.'
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchProgramData()
})
</script>

<template>
    <div class="min-h-screen bg-bg-base py-8 sm:py-12 px-4 sm:px-6">
        <div class="max-w-7xl mx-auto">
            <!-- Header Section -->
            <div class="text-center mb-8 sm:mb-12">
                <h1 class="text-display font-brand text-text-primary mb-3">
                    Chương trình Thành viên
                </h1>
                <p class="text-body text-text-secondary max-w-2xl mx-auto">
                    Càng xem nhiều, càng nhận ưu đãi lớn. Khám phá các cấp bậc và quyền lợi đặc biệt dành riêng cho bạn.
                </p>
            </div>

            <!-- Loading State -->
            <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <Skeleton :blocks="skeletonBlocks" />
            </div>

            <!-- Error State -->
            <div v-else-if="error" class="bg-error/10 border border-error/20 rounded-2xl p-6 text-center">
                <p class="text-error font-medium">{{ error }}</p>
                <button @click="fetchProgramData"
                    class="mt-4 px-6 py-2 bg-accent text-text-on-accent rounded-xl hover:opacity-90 transition">
                    Thử lại
                </button>
            </div>

            <!-- Tiers Grid -->
            <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <LoyaltyTierCard v-for="(tier, index) in tiersWithRules" :key="tier.id" :tier="tier"
                    :is-highest="index === tiersWithRules.length - 1"
                    :next-min-spending="tiersWithRules[index + 1]?.minSpendingRequired" />
            </div>

            <!-- Footer Note -->
            <div class="mt-12 text-center text-caption text-text-tertiary border-t border-border-subtle pt-8">
                <p>Điều khoản và điều kiện áp dụng. Chương trình có thể thay đổi theo thông báo của rạp.</p>
            </div>
        </div>
    </div>
</template>