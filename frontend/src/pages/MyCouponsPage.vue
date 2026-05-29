<template>
    <div class="min-h-screen bg-bg-base">
        <!-- ── Header ─────────────────────────────────────────────────────────── -->
        <section class="relative overflow-hidden py-10 sm:py-14 px-4">
            <div class="pointer-events-none absolute -top-16 -right-16 w-64 h-64 rounded-full bg-accent/8 blur-3xl" />
            <div class="relative max-w-3xl mx-auto">
                <div class="flex items-center justify-between flex-wrap gap-4">
                    <div>
                        <div class="flex items-center gap-2 mb-2">
                            <WalletIcon class="w-5 h-5 text-accent" />
                            <h1 class="text-title font-brand text-text-primary">Coupon của tôi</h1>
                        </div>
                        <p class="text-text-secondary text-sm">
                            Bạn đang có
                            <span class="font-semibold text-accent">{{ activeCoupons.length }}</span>
                            coupon khả dụng
                        </p>
                    </div>
                    <router-link to="/coupons"
                        class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-accent text-text-on-accent text-sm font-medium hover:opacity-90 transition-opacity">
                        <PlusIcon class="w-4 h-4" />
                        Đổi thêm coupon
                    </router-link>
                </div>
            </div>
        </section>

        <div class="max-w-3xl mx-auto px-4 pb-16">
            <!-- ── Filter tabs ──────────────────────────────────────────────────── -->
            <div class="flex gap-2 mb-6 overflow-x-auto hide-scrollbar pb-1">
                <FilterTab v-for="tab in filterTabs" :key="tab.value" :label="tab.label"
                    :active="activeFilter === tab.value" @click="setFilter(tab.value)" />
            </div>

            <!-- Loading -->
            <div v-if="loading && coupons.length === 0" class="space-y-3">
                <div v-for="i in 4" :key="i"
                    class="h-28 rounded-xl bg-bg-surface border border-border-subtle animate-pulse" />
            </div>

            <!-- Error -->
            <div v-else-if="error" class="text-center py-16 text-text-secondary">
                <AlertCircleIcon class="w-10 h-10 mx-auto mb-3 text-text-disabled" />
                <p>{{ error }}</p>
                <button @click="fetchWallet(true)" class="mt-4 text-accent text-sm hover:underline">
                    Thử lại
                </button>
            </div>

            <!-- Empty -->
            <div v-else-if="!loading && filteredCoupons.length === 0" class="text-center py-16 text-text-secondary">
                <TicketIcon class="w-12 h-12 mx-auto mb-3 text-text-disabled" />
                <p class="text-body">{{ emptyMessage }}</p>
                <router-link v-if="activeFilter === 'ALL' || activeFilter === 'ACTIVE'" to="/coupons"
                    class="mt-4 inline-block text-accent text-sm hover:underline">
                    Đổi coupon ngay →
                </router-link>
            </div>

            <!-- List -->
            <div v-else class="space-y-3">
                <UserCouponItem v-for="item in filteredCoupons" :key="item.id" :item="item" />
            </div>

            <!-- Load more -->
            <div v-if="hasMore" class="mt-8 text-center">
                <button :disabled="loading" @click="loadMore"
                    class="px-6 py-2.5 rounded-full border border-border-default text-text-secondary hover:border-accent hover:text-accent transition-colors text-sm disabled:opacity-40">
                    <LoaderIcon v-if="loading" class="w-4 h-4 animate-spin inline mr-2" />
                    Xem thêm
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import {
    WalletIcon,
    PlusIcon,
    TicketIcon,
    LoaderIcon,
    AlertCircleIcon,
} from 'lucide-vue-next'
import { useUserCouponWallet } from '@/composables/useUserCouponWallet'
import UserCouponItem from '@/components/coupon/UserCouponItem.vue'
import FilterTab from '@/components/coupon/FilterTab.vue'
import type { UserCouponStatus } from '@/types/user-coupon.types'

const {
    coupons,
    loading,
    error,
    hasMore,
    activeFilter,
    filteredCoupons,
    activeCoupons,
    fetchWallet,
    loadMore,
    setFilter,
} = useUserCouponWallet()

const filterTabs: { label: string; value: UserCouponStatus | 'ALL' }[] = [
    { label: 'Tất cả', value: 'ALL' },
    { label: 'Khả dụng', value: 'ACTIVE' },
    { label: 'Đã dùng', value: 'USED' },
    { label: 'Hết hạn', value: 'EXPIRED' },
    { label: 'Đã thu hồi', value: 'REVOKED' },
]

const emptyMessage = computed(() => {
    const map: Record<string, string> = {
        ALL: 'Bạn chưa có coupon nào.',
        ACTIVE: 'Không có coupon khả dụng.',
        USED: 'Chưa có coupon đã dùng.',
        EXPIRED: 'Không có coupon hết hạn.',
        REVOKED: 'Không có coupon bị thu hồi.',
    }
    return map[activeFilter.value] ?? 'Không có dữ liệu.'
})

onMounted(() => fetchWallet(true))
</script>