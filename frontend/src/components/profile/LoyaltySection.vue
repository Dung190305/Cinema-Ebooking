<!-- components/profile/LoyaltySection.vue -->
<script setup lang="ts">
defineProps<{
    error?: string | null
    currentTierName: string;
    discountPercent?: number;
    currentPoints: number;
    lifetimePoints: number;
    totalSpending: number;
    nextTierName?: string;
    nextTierSpendingRequired: number;
    spendingNeeded: number;
    progressPercent: number;
    isHighestTier: boolean;
    loading?: boolean;
}>();
</script>

<template>
    <section v-if="!loading"
        class="bg-bg-surface rounded-2xl p-5 sm:p-6 shadow-sm border border-border-subtle space-y-6">

        <div class="flex items-center justify-between flex-wrap gap-3">
            <h2 class="text-title text-text-primary">Thành viên thân thiết</h2>
            <router-link to="/loyalty-program"
                class="text-caption text-accent hover:underline flex items-center gap-1 transition">
                Chi tiết chương trình
                <span aria-hidden="true">→</span>
            </router-link>
        </div>
        <p v-if="error" class="text-caption text-red-500">{{ error }}</p>
        <!-- Hạng hiện tại -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
                <p class="text-caption text-text-tertiary">Hạng hiện tại</p>
                <p class="text-display font-brand text-accent">{{ currentTierName }}</p>
                <p v-if="discountPercent" class="text-caption text-text-secondary mt-1">
                    Giảm {{ discountPercent }}% giá vé
                </p>
            </div>
            <!-- Thông tin điểm & chi tiêu -->
            <div class="grid grid-cols-2 gap-x-6 gap-y-2 sm:grid-cols-3">
                <div>
                    <p class="text-caption text-text-tertiary">Điểm hiện có</p>
                    <p class="text-body font-semibold text-text-primary">{{ currentPoints.toLocaleString() }}</p>
                </div>
                <div>
                    <p class="text-caption text-text-tertiary">Điểm tích lũy</p>
                    <p class="text-body font-semibold text-text-primary">{{ lifetimePoints.toLocaleString() }}</p>
                </div>
                <div>
                    <p class="text-caption text-text-tertiary">Tổng chi tiêu</p>
                    <p class="text-body font-semibold text-text-primary">{{ totalSpending.toLocaleString() }} ₫</p>
                </div>
            </div>
        </div>

        <!-- Thanh tiến trình lên hạng dựa trên tổng chi tiêu -->
        <div v-if="!isHighestTier && nextTierName">
            <div class="flex justify-between text-caption text-text-secondary mb-1">
                <span>Tiến trình lên {{ nextTierName }}</span>
                <span>{{ totalSpending.toLocaleString() }}₫ / {{ nextTierSpendingRequired.toLocaleString() }}₫</span>
            </div>
            <div class="w-full h-2 bg-disabled rounded-full overflow-hidden">
                <div class="h-full bg-accent rounded-full transition-all duration-500"
                    :style="{ width: progressPercent + '%' }" />
            </div>
            <p class="text-caption text-text-tertiary mt-2">
                Cần thêm <strong>{{ spendingNeeded.toLocaleString() }}₫</strong> để lên hạng {{ nextTierName }}
            </p>
        </div>
        <p v-else class="text-body text-accent">
            Chúc mừng! Bạn đã đạt hạng cao nhất.
        </p>
    </section>
    <section v-else class="bg-bg-surface rounded-2xl p-5 sm:p-6 shadow-sm border border-border-subtle">
        <div class="animate-pulse text-body text-text-tertiary">Đang tải thông tin hạng...</div>
    </section>
</template>