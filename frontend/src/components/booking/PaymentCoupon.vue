<script setup lang="ts">
import { ref, inject } from 'vue'
import { couponApi } from '@/api/coupon.api'
import type { CouponResponse } from '@/types/coupon.types'
import type { BookingState } from '@/composables/useBooking'
import { useAuthStore } from '@/stores/auth.store'

const emit = defineEmits(['prev', 'success'])
const booking = inject<BookingState>('booking')!

const authStore = useAuthStore()   // ← Khởi tạo auth store

const code = ref('')
const couponError = ref('')
const couponLoading = ref(false)
const applied = ref<CouponResponse | null>(null)

async function applyCoupon() {
    if (!code.value.trim()) return
    couponError.value = ''
    couponLoading.value = true
    try {
        const res = await couponApi.getList(0, 200)
        const found = (res.content ?? []).find(
            (c: CouponResponse) =>
                c.code.toLowerCase() === code.value.trim().toLowerCase() &&
                c.status === 'ACTIVE'
        )
        if (!found) {
            couponError.value = 'Mã không hợp lệ hoặc đã hết hạn'
            return
        }
        applied.value = found
        booking.appliedCoupon.value = {
            couponId: found.id,
            code: found.code,
            discountValue: found.value ?? 0,
        }
    } catch {
        couponError.value = 'Không thể kiểm tra mã. Vui lòng thử lại.'
    } finally {
        couponLoading.value = false
    }
}

function removeCoupon() {
    applied.value = null
    booking.appliedCoupon.value = null
    code.value = ''
    couponError.value = ''
}

async function validateAndNext() {
    if (!booking.selectedSeats.value.length) {
        alert('Chưa chọn ghế')
        return false
    }
    await confirmBooking()
    return true
}

async function confirmBooking() {
    // Sử dụng đúng authStore
    const userId = authStore.user?.id

    if (!userId) {
        alert('Vui lòng đăng nhập để tiếp tục')
        return
    }

    const result = await booking.createBooking(userId)
    if (result) {
        emit('success')
    }
}

defineExpose({ next: validateAndNext })
</script>
<template>
    <div>
        <h2 class="text-title mb-6">Xác nhận & thanh toán</h2>

        <!-- Order summary -->
        <div class="bg-bg-surface border border-border-default rounded-xl p-4 mb-4 space-y-2 text-caption">
            <div class="flex justify-between">
                <span class="text-text-secondary">Vé ({{ booking.selectedSeats.value.length }} ghế)</span>
                <span>{{ booking.seatTotal.value.toLocaleString() }}đ</span>
            </div>
            <div v-if="booking.comboTotal.value > 0" class="flex justify-between">
                <span class="text-text-secondary">Bắp nước</span>
                <span>{{ booking.comboTotal.value.toLocaleString() }}đ</span>
            </div>
            <div v-if="booking.discount.value > 0" class="flex justify-between text-green-500">
                <span>Giảm giá</span>
                <span>-{{ booking.discount.value.toLocaleString() }}đ</span>
            </div>
            <hr class="border-border-subtle" />
            <div class="flex justify-between text-body font-bold">
                <span>Tổng cộng</span>
                <span class="text-accent">{{ booking.grandTotal.value.toLocaleString() }}đ</span>
            </div>
        </div>

        <!-- Coupon -->
        <div class="bg-bg-surface border border-border-default rounded-xl p-4 mb-4">
            <p class="text-body font-medium mb-2">Mã giảm giá</p>
            <div v-if="applied"
                class="flex items-center justify-between p-3 rounded-lg bg-green-500/10 border border-green-500/30">
                <div>
                    <span class="text-green-500 font-semibold text-body">{{ applied.code }}</span>
                    <span class="text-text-secondary text-caption ml-2">Giảm {{ applied.value?.toLocaleString()
                    }}đ</span>
                </div>
                <button class="text-text-tertiary text-caption hover:text-red-400 transition"
                    @click="removeCoupon">Xoá</button>
            </div>
            <div v-else>
                <div class="flex gap-2">
                    <input v-model="code"
                        class="flex-1 px-3 py-2 border border-border-default rounded-lg bg-bg-base text-text-primary placeholder:text-text-tertiary"
                        placeholder="Nhập mã giảm giá" @keydown.enter="applyCoupon" />
                    <button
                        class="px-4 py-2 bg-accent text-text-on-accent rounded-lg font-medium disabled:opacity-50 transition hover:brightness-110"
                        :disabled="!code.trim() || couponLoading" @click="applyCoupon">
                        {{ couponLoading ? '…' : 'Áp dụng' }}
                    </button>
                </div>
                <p v-if="couponError" class="text-red-400 text-caption mt-1.5">{{ couponError }}</p>
            </div>
        </div>

        <div v-if="booking.createError.value"
            class="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 text-body">
            {{ booking.createError.value }}
        </div>
    </div>
</template>