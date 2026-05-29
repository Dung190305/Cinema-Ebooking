<template>
    <div class="min-h-screen bg-bg-base">
        <!-- ── Hero header ───────────────────────────────────────────────────── -->
        <section class="relative overflow-hidden py-12 sm:py-16 px-4">
            <!-- Decorative blobs -->
            <div class="pointer-events-none absolute -top-20 -left-20 w-72 h-72 rounded-full bg-accent/10 blur-3xl" />
            <div
                class="pointer-events-none absolute -bottom-10 right-10 w-56 h-56 rounded-full bg-accent-alt/10 blur-3xl" />

            <div class="relative max-w-4xl mx-auto text-center">
                <div
                    class="inline-flex items-center gap-2 mb-4 px-3 py-1.5 rounded-full bg-accent/10 border border-accent/20">
                    <TicketIcon class="w-4 h-4 text-accent" />
                    <span class="text-xs font-medium text-accent uppercase tracking-widest">Ưu đãi</span>
                </div>
                <h1 class="text-display font-brand text-text-primary mb-3">Đổi Coupon</h1>
                <p class="text-text-secondary text-body max-w-md mx-auto">
                    Nhập mã coupon hoặc chọn từ danh sách bên dưới để lưu ưu đãi vào ví của bạn.
                </p>

                <!-- My coupons CTA -->
                <div class="mt-6 flex justify-center">
                    <router-link to="/my-coupons"
                        class="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-border-default text-text-secondary hover:border-accent hover:text-accent transition-colors text-sm">
                        <WalletIcon class="w-4 h-4" />
                        Xem coupon của tôi
                    </router-link>
                </div>
            </div>
        </section>

        <div class="max-w-4xl mx-auto px-4 pb-16">
            <!-- ── Redeem by code ───────────────────────────────────────────────── -->
            <div class="mb-10 p-5 sm:p-6 rounded-xl border border-border-default bg-bg-surface">
                <h2 class="text-title text-text-primary mb-4 flex items-center gap-2">
                    <HashIcon class="w-5 h-5 text-accent" />
                    Nhập mã coupon
                </h2>
                <div class="flex gap-3 flex-col sm:flex-row">
                    <input v-model="redeemCode" type="text" placeholder="VD: SUMMER2025" maxlength="32"
                        class="flex-1 px-4 py-3 rounded-lg border border-border-default bg-bg-base text-text-primary placeholder:text-text-disabled focus:outline-none focus:border-accent transition-colors text-sm uppercase tracking-widest"
                        @keydown.enter="redeemByCode" @input="clearRedeemState" />
                    <button :disabled="!redeemCode.trim() || redeemLoading" @click="redeemByCode"
                        class="px-6 py-3 rounded-lg bg-accent text-text-on-accent font-medium text-sm transition-all hover:opacity-90 disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-2 justify-center shrink-0">
                        <LoaderIcon v-if="redeemLoading" class="w-4 h-4 animate-spin" />
                        <span v-else>Đổi ngay</span>
                    </button>
                </div>

                <!-- Feedback -->
                <Transition name="fade-slide">
                    <div v-if="redeemError" class="mt-3 flex items-center gap-2 text-sm text-red-500">
                        <AlertCircleIcon class="w-4 h-4 shrink-0" />
                        {{ redeemError }}
                    </div>
                    <div v-else-if="redeemSuccess" class="mt-3 flex items-center gap-2 text-sm text-green-500">
                        <CheckCircle2Icon class="w-4 h-4 shrink-0" />
                        Đổi coupon thành công! Coupon đã được thêm vào ví của bạn.
                    </div>
                </Transition>
            </div>

            <!-- ── Coupon grid ──────────────────────────────────────────────────── -->
            <h2 class="text-title text-text-primary mb-5 flex items-center gap-2">
                <SparklesIcon class="w-5 h-5 text-accent" />
                Coupon đang có
            </h2>

            <!-- Loading skeleton -->
            <div v-if="loading && coupons.length === 0" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div v-for="i in 6" :key="i"
                    class="h-44 rounded-xl bg-bg-surface border border-border-subtle animate-pulse" />
            </div>

            <!-- Error state -->
            <div v-else-if="error" class="text-center py-16 text-text-secondary">
                <AlertCircleIcon class="w-10 h-10 mx-auto mb-3 text-text-disabled" />
                <p>{{ error }}</p>
                <button @click="fetchCoupons(true)" class="mt-4 text-accent text-sm hover:underline">
                    Thử lại
                </button>
            </div>

            <!-- Empty state -->
            <div v-else-if="!loading && coupons.length === 0" class="text-center py-16 text-text-secondary">
                <TicketIcon class="w-12 h-12 mx-auto mb-3 text-text-disabled" />
                <p class="text-body">Hiện chưa có coupon nào đang hoạt động.</p>
            </div>

            <!-- Coupon grid -->
            <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <CouponCard v-for="coupon in coupons" :key="coupon.id" :coupon="coupon" :loading="redeemLoading"
                    @redeem="redeemCoupon(coupon)" />
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
import { onMounted } from 'vue'
import {
    TicketIcon,
    WalletIcon,
    HashIcon,
    SparklesIcon,
    LoaderIcon,
    AlertCircleIcon,
    CheckCircle2Icon,
} from 'lucide-vue-next'
import { useCouponBrowse } from '@/composables/useCouponBrowse'
import CouponCard from '@/components/coupon/CouponCard.vue'

const {
    coupons,
    loading,
    error,
    hasMore,
    redeemCode,
    redeemLoading,
    redeemError,
    redeemSuccess,
    fetchCoupons,
    loadMore,
    redeemByCode,
    redeemCoupon,
    clearRedeemState,
} = useCouponBrowse()

onMounted(() => fetchCoupons(true))
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.25s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(-4px);
}
</style>