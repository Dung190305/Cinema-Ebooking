<!-- components/loyalty/LoyaltyTierCard.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import type { MembershipTierResponse } from '@/types/membership-tier'
import type { EarningRuleResponse } from '@/types/earning-rule'
import { formatVND } from '@/utils/currency'

interface TierWithRules extends MembershipTierResponse {
    rules: EarningRuleResponse[]
}

const props = defineProps<{
    tier: TierWithRules
    isHighest: boolean
    nextMinSpending?: number
}>()

const perUnit = "1.000đ"

// Tier level mapping for visual styles
const tierLevel = computed(() => props.tier.tierLevel)

const cardBgClass = computed(() => {
    switch (tierLevel.value) {
        case 1: return 'bg-bg-surface'
        case 2: return 'bg-bg-surface'
        case 3: return 'bg-gradient-to-br from-amber-50/10 to-bg-surface dark:from-amber-900/5'
        default: return 'bg-gradient-to-br from-accent/5 to-bg-surface'
    }
})

const borderClass = computed(() => {
    switch (tierLevel.value) {
        case 1: return 'border-border-subtle'
        case 2: return 'border-gray-300 dark:border-gray-600'
        case 3: return 'border-amber-300 dark:border-amber-700 shadow-md shadow-amber-500/10'
        default: return 'border-accent/40 shadow-lg shadow-accent/10'
    }
})

const badgeClass = computed(() => {
    switch (tierLevel.value) {
        case 1: return 'bg-border-subtle/50 text-text-secondary'
        case 2: return 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300'
        case 3: return 'bg-amber-100 dark:bg-amber-900/50 text-amber-700 dark:text-amber-300'
        default: return 'bg-accent/20 text-accent'
    }
})

const discountTextClass = computed(() => {
    if (tierLevel.value >= 3) return 'text-accent'
    return 'text-text-primary'
})

const formatEarningRuleText = (rule: EarningRuleResponse): string => {
    if (rule.earningType === 'MULTIPLIER' && rule.multiplier) {
        return `Nhân ${rule.multiplier}x điểm cho mỗi 1.000đ`
    }
    if (rule.earningType === 'FIXED' && rule.fixedPoints) {
        return `+${rule.fixedPoints.toLocaleString()} điểm mỗi giao dịch`
    }
    if (rule.description) {
        return rule.description
    }
    return 'Chi tiết theo chương trình'
}

const primaryRule = computed(() => {
    if (props.tier.rules.length === 0) return null
    return [...props.tier.rules].sort((a, b) => b.priority - a.priority)[0]
})

const otherRules = computed(() => {
    if (!primaryRule.value) return props.tier.rules
    return props.tier.rules.filter(r => r.id !== primaryRule.value!.id)
})

const spendingRangeText = computed(() => {
    if (props.isHighest) {
        return `${formatVND(props.tier.minSpendingRequired)}+`
    }
    if (props.nextMinSpending) {
        return `${formatVND(props.tier.minSpendingRequired)} - ${formatVND(props.nextMinSpending)}`
    }
    return formatVND(props.tier.minSpendingRequired)
})
</script>

<template>
    <div :class="[
        cardBgClass,
        borderClass,
        {
            'hover:shadow-xl': tierLevel <= 2,
            'hover:shadow-2xl hover:shadow-amber-500/20': tierLevel === 3,
            'hover:shadow-2xl hover:shadow-accent/25': tierLevel >= 4
        }
    ]" class="group relative rounded-2xl overflow-hidden border transition-all duration-300 hover:-translate-y-1">
        <!-- Tier Badge / Level -->
        <div class="absolute top-4 right-4 z-10">
            <div :class="badgeClass" class="px-3 py-1 rounded-full text-caption font-semibold backdrop-blur-sm">
                Bậc {{ tier.tierLevel }}
            </div>
        </div>

        <!-- Card Content -->
        <div class="p-5 sm:p-6 relative">
            <!-- Tier Name -->
            <div class="mb-4">
                <h3 class="text-title font-brand text-text-primary mb-1">
                    {{ tier.name }}
                </h3>
            </div>

            <!-- Spending Requirement -->
            <div class="mb-4 p-3 bg-bg-base/50 rounded-xl backdrop-blur-sm">
                <p class="text-caption text-text-tertiary mb-1">Chi tiêu tối thiểu</p>
                <p class="text-body font-semibold text-text-primary">{{ spendingRangeText }}</p>
            </div>

            <!-- Earning Rules -->
            <div class="mb-4">
                <div class="flex justify-between items-center text-caption text-text-tertiary mb-2">
                    <p>Cách tích điểm</p>
                    <p>với mỗi {{ perUnit }}</p>
                </div>

                <div v-if="primaryRule" class="space-y-2">
                    <div class="flex items-center gap-2 text-body text-text-primary bg-accent/5 rounded-lg p-2">
                        <span class="text-accent text-lg">✦</span>
                        <span class="font-medium">{{ formatEarningRuleText(primaryRule) }}</span>
                    </div>
                    <div v-for="rule in otherRules" :key="rule.id" class="text-caption text-text-secondary pl-6">
                        • {{ formatEarningRuleText(rule) }}
                    </div>
                </div>
                <p v-else class="text-caption text-text-tertiary italic">Đang cập nhật</p>
            </div>

            <!-- Benefits Description -->
            <div v-if="tier.benefitsDescription" class="pt-3 border-t border-border-subtle">
                <p class="text-caption text-text-tertiary mb-1">Quyền lợi đặc biệt</p>
                <p class="text-caption text-text-secondary line-clamp-3">
                    {{ tier.benefitsDescription }}
                </p>
            </div>
        </div>

        <!-- Subtle shine effect for highest tier -->
        <div v-if="isHighest"
            class="absolute inset-0 pointer-events-none bg-lỉnear-to-tr from-transparent via-white/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500">
        </div>
    </div>
</template>