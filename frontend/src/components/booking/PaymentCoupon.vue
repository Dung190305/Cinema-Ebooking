<script setup lang="ts">
import { ref, inject, computed, onMounted } from 'vue'
import { couponApi } from '@/api/coupon.api'
import { paymentApi } from '@/api/payment.api'
import { bookingApi } from '@/api/booking.api'
import { useUserCouponWallet } from '@/composables/useUserCouponWallet'
import type { CouponResponse } from '@/types/coupon.types'
import type { PaymentMethod } from '@/types/payment.types'
import type { BookingState } from '@/composables/useBooking'
import { useAuthStore } from '@/stores/auth.store'
import { useCloudinaryImage } from '@/composables/useCloudinaryImage'

const { getTransformedUrl } = useCloudinaryImage()

const emit = defineEmits(['prev', 'success'])
const booking = inject<BookingState>('booking')!
const authStore = useAuthStore()
const {
    coupons: userCoupons,
    fetchAllCoupons,
    redeemCoupon,
    useCoupon,
    restoreCoupon,
    error: couponError,
    loading: couponLoading,
} = useUserCouponWallet()

// UI state
const code = ref('')
const appliedCouponDetail = ref<CouponResponse | null>(null)
const selectedUserCouponId = ref<number | null>(null)
const selectedPaymentMethod = ref<PaymentMethod>('MOMO')
const isProcessing = ref(false)
const isUserCouponExpanded = ref(false)
const generalError = ref('')

// State cho booking pending
const pendingBookingId = ref<number | null>(null)
const showPendingDialog = ref(false)

// Danh sách coupon khả dụng (ACTIVE, còn lượt)
const availableUserCoupons = computed(() =>
    userCoupons.value.filter(uc => uc.status === 'ACTIVE' && uc.usageRemain > 0)
)

// Áp dụng coupon từ danh sách có sẵn
function applyUserCoupon(uc: typeof availableUserCoupons.value[0]) {
    appliedCouponDetail.value = {
        id: uc.couponId,
        code: uc.couponCode,
        type: uc.couponType,
        value: uc.couponValue,
        minimumBookingValue: uc.minimumBookingValue,
        maximumDiscountAmount: uc.maximumDiscountAmount,
    } as CouponResponse
    selectedUserCouponId.value = uc.id
    const subtotal = booking.seatTotal.value + booking.comboTotal.value
    const rawDiscount = uc.couponType === 'PERCENT'
        ? subtotal * (uc.couponValue / 100)
        : uc.couponValue
    const actualDiscount = Math.min(rawDiscount, uc.maximumDiscountAmount ?? Infinity)
    booking.appliedCoupon.value = {
        couponId: uc.couponId,
        code: uc.couponCode,
        couponType: uc.couponType,
        couponValue: uc.couponValue,
        discountValue: actualDiscount,
        minimumBookingValue: uc.minimumBookingValue,
        maximumDiscountAmount: uc.maximumDiscountAmount,
    }
}

// Nhập mã mới
async function applyCoupon() {
    if (!code.value.trim()) return
    const newUserCoupon = await redeemCoupon(code.value.trim())
    if (newUserCoupon) {
        appliedCouponDetail.value = {
            id: newUserCoupon.couponId,
            code: newUserCoupon.couponCode,
            type: newUserCoupon.couponType,
            value: newUserCoupon.couponValue,
            minimumBookingValue: newUserCoupon.minimumBookingValue,
            maximumDiscountAmount: newUserCoupon.maximumDiscountAmount,
        } as CouponResponse
        selectedUserCouponId.value = newUserCoupon.id
        const subtotal2 = booking.seatTotal.value + booking.comboTotal.value
        const rawDiscount2 = newUserCoupon.couponType === 'PERCENT'
            ? subtotal2 * (newUserCoupon.couponValue / 100)
            : newUserCoupon.couponValue
        const actualDiscount2 = Math.min(rawDiscount2, newUserCoupon.maximumDiscountAmount ?? Infinity)
        booking.appliedCoupon.value = {
            couponId: newUserCoupon.couponId,
            code: newUserCoupon.couponCode,
            couponType: newUserCoupon.couponType,
            couponValue: newUserCoupon.couponValue,
            discountValue: actualDiscount2,
            minimumBookingValue: newUserCoupon.minimumBookingValue,
            maximumDiscountAmount: newUserCoupon.maximumDiscountAmount,
        }
        code.value = ''
        await fetchAllCoupons()
    } else {
        // lỗi đã được set trong composable
        if (couponError.value) generalError.value = couponError.value
    }
}

function removeCoupon() {
    appliedCouponDetail.value = null
    selectedUserCouponId.value = null
    booking.appliedCoupon.value = null
    code.value = ''
}


function restoreLockFromStorage(showtimeId: number): boolean {
    const key = `seatlock_${showtimeId}`
    const raw = sessionStorage.getItem(key)
    if (!raw) return false

    try {
        const data = JSON.parse(raw)
        const expiry = new Date(data.expiredAt).getTime()
        if (expiry > Date.now() && data.seats?.length) {
            seatLock?.restoreLock(data.expiredAt, data.seats)
            return true
        } else {
            // Đã hết hạn, xóa storage
            sessionStorage.removeItem(key)
            return false
        }
    } catch {
        return false
    }
}

// Huỷ booking cũ, reset state để chọn lại ghế
async function cancelOldBookingAndReset() {
    if (!pendingBookingId.value) return
    showPendingDialog.value = false
    isProcessing.value = true
    try {
        await bookingApi.cancelBooking(pendingBookingId.value)
        // Reset booking state (xóa ghế đã chọn, coupon, ...)
        booking.resetBooking?.()
        // Tải lại dữ liệu ghế cho suất chiếu hiện tại
        const showtimeId = booking.selectedShowtime.value?.id
        if (showtimeId) {
            await booking.loadShowtimeSeats?.(showtimeId)
        }
        generalError.value = 'Đã huỷ đơn hàng cũ. Bạn có thể chọn ghế lại.'
        pendingBookingId.value = null
        sessionStorage.removeItem('returned_from_payment')
    } catch (err: any) {
        generalError.value = 'Không thể huỷ đơn hàng, vui lòng thử lại sau.'
    } finally {
        isProcessing.value = false
    }
}

async function confirmAndPay() {
    const userId = authStore.user?.id
    if (!userId) {
        generalError.value = 'Vui lòng đăng nhập.'
        return
    }
    if (isProcessing.value) return

    isProcessing.value = true
    generalError.value = ''

    try {
        await booking.createBooking(userId)
        if (!booking.createdBooking.value) {
            generalError.value = booking.createError.value || 'Không thể tạo booking. Vui lòng thử lại.'
            return
        }

        // Xử lý coupon
        if (selectedUserCouponId.value) {
            try {
                const used = await useCoupon(selectedUserCouponId.value)
                if (!used) {
                    generalError.value = 'Không thể áp dụng coupon. Vui lòng kiểm tra lại hoặc thử coupon khác.'
                    return
                }
            } catch (couponErr: any) {
                generalError.value = couponErr?.message || 'Lỗi khi sử dụng coupon.'
                return
            }
        }

        sessionStorage.setItem('failed_payment_showtimeId', String(booking.selectedShowtime.value?.id))
        sessionStorage.setItem('just_left_for_payment', 'true')
        sessionStorage.setItem('last_payment_method', selectedPaymentMethod.value)

        const paymentRes = await paymentApi.create({
            bookingId: booking.createdBooking.value.bookingId,
            method: selectedPaymentMethod.value,
            callbackUrl: `${window.location.origin}/payment/result`,
        })

        if (!paymentRes.paymentUrl) {
            if (selectedUserCouponId.value) {
                await restoreCoupon(selectedUserCouponId.value)
            }
            generalError.value = 'Không lấy được link thanh toán. Vui lòng thử lại.'
            sessionStorage.removeItem('just_left_for_payment')
            return
        }
        sessionStorage.setItem('returned_from_payment', 'true')
        window.location.href = paymentRes.paymentUrl
    } catch (err: any) {
        generalError.value = err?.message || 'Đã xảy ra lỗi trong quá trình thanh toán.'
        if (selectedUserCouponId.value) {
            try { await restoreCoupon(selectedUserCouponId.value) } catch { }
        }
        sessionStorage.removeItem('just_left_for_payment')
    } finally {
        isProcessing.value = false
    }
}

async function validateAndNext() {
    generalError.value = ''
    if (!booking.selectedSeats.value.length) {
        generalError.value = 'Vui lòng chọn ít nhất một ghế trước khi thanh toán.'
        return false
    }
    if (!authStore.user?.id) {
        generalError.value = 'Vui lòng đăng nhập để tiếp tục.'
        return false
    }

    await confirmAndPay()
    return true
}

onMounted(async () => {
    try {
        await fetchAllCoupons()
    } catch (err: any) {
        generalError.value = 'Không thể tải danh sách coupon.'
    }
})

const logoUrls = {
    MOMO: 'https://res.cloudinary.com/diae3v9nm/image/upload/v1779817715/png-transparent-momo-hd-logo-thumbnail_n11goz.png',
    VNPAY: 'https://res.cloudinary.com/diae3v9nm/image/upload/v1779817717/vnpay-logo-vinadesign-25-12-57-55_pjsyns.jpg',
    ZALOPAY: 'https://res.cloudinary.com/diae3v9nm/image/upload/v1779817716/Logo-ZaloPay-Square-1024x1024_ajzbzi.webp'
}

defineExpose({ next: validateAndNext })
</script>

<template>
    <div>
        <h2 class="text-title mb-6">Xác nhận & thanh toán</h2>

        <!-- Dialog thông báo booking đang chờ -->
        <div v-if="showPendingDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div class="bg-bg-surface rounded-xl p-6 max-w-md w-full mx-4 shadow-2xl">
                <h3 class="text-lg font-bold mb-2">Đã có đơn hàng đang chờ</h3>
                <p class="text-text-secondary mb-4">
                    Bạn đã tạo một đơn hàng cho suất chiếu này nhưng chưa thanh toán.
                    Bạn muốn tiếp tục thanh toán hay huỷ để chọn lại ghế khác?
                </p>
                <div class="flex gap-3">
                    <button @click="continuePayment" :disabled="isProcessing"
                        class="flex-1 py-2 bg-accent text-white rounded-lg hover:bg-accent/90 transition disabled:opacity-50">
                        Tiếp tục thanh toán
                    </button>
                    <button @click="cancelOldBookingAndReset" :disabled="isProcessing"
                        class="flex-1 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition disabled:opacity-50">
                        Huỷ & chọn lại ghế
                    </button>
                </div>
            </div>
        </div>

        <!-- Thông báo lỗi chung -->
        <div v-if="generalError"
            class="mb-4 p-3 rounded-lg bg-red-500/10 border border-error/30 text-error flex items-center justify-between">
            <span>{{ generalError }}</span>
            <button @click="generalError = ''" class="ml-2 text-text-tertiary hover:text-white transition">✕</button>
        </div>


        <!-- Nhập mã giảm giá -->
        <div class="bg-bg-surface border border-border-default rounded-xl p-4 mb-4">
            <p class="text-body font-medium mb-2">Nhập mã giảm giá</p>
            <div v-if="!appliedCouponDetail">
                <div class="flex gap-2">
                    <input v-model="code" class="flex-1 px-3 py-2 border border-border-default rounded-lg bg-bg-base"
                        placeholder="Nhập mã khuyến mãi" @keydown.enter="applyCoupon" />
                    <button class="px-4 py-2 bg-accent text-text-on-accent rounded-lg font-medium disabled:opacity-50"
                        :disabled="!code.trim() || couponLoading" @click="applyCoupon">
                        {{ couponLoading ? '…' : 'Áp dụng' }}
                    </button>
                </div>
                <p v-if="couponError" class="text-red-400 text-caption mt-1.5">{{ couponError }}</p>
            </div>
            <div v-else
                class="flex items-center justify-between p-3 rounded-lg bg-green-500/10 border border-green-500/30">
                <div>
                    <span class="text-green-500 font-semibold text-body">{{ appliedCouponDetail.code }}</span>
                    <span class="text-text-secondary text-caption ml-2">
                        Giảm
                        <template v-if="appliedCouponDetail.type === 'PERCENT'">
                            {{ appliedCouponDetail.value }}%
                        </template>
                        <template v-else>
                            {{ appliedCouponDetail.value?.toLocaleString() }}đ
                        </template>
                    </span>
                    <div class="text-caption text-text-tertiary">
                        Đơn tối thiểu: {{ appliedCouponDetail.minimumBookingValue?.toLocaleString() }}đ &nbsp;|&nbsp;
                        Giảm tối đa: {{ appliedCouponDetail.maximumDiscountAmount?.toLocaleString() }}đ
                    </div>
                </div>
                <button class="text-text-tertiary text-caption hover:text-red-400 transition"
                    @click="removeCoupon">Xoá</button>
            </div>
        </div>

        <div class="bg-bg-surface border border-border-default rounded-xl p-4 mb-4">
            <button @click="isUserCouponExpanded = !isUserCouponExpanded"
                class="flex items-center justify-between w-full text-body font-medium transition">
                <span>Mã giảm giá đang có ({{ availableUserCoupons.length }})</span>
                <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 transition-transform duration-200"
                    :class="{ 'rotate-180': isUserCouponExpanded }" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd"
                        d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                        clip-rule="evenodd" />
                </svg>
            </button>

            <div v-if="isUserCouponExpanded" class="mt-3 space-y-2 max-h-60 overflow-y-auto">
                <div v-if="availableUserCoupons.length === 0" class="text-text-tertiary text-caption text-center py-3">
                    Bạn chưa có mã giảm giá khả dụng
                </div>
                <label v-for="uc in availableUserCoupons" :key="uc.id"
                    class="flex items-start gap-3 p-3 rounded-lg border border-border-default hover:bg-bg-hover cursor-pointer transition"
                    :class="{ 'border-accent bg-accent/5': selectedUserCouponId === uc.id }">
                    <input type="radio" name="userCoupon" :value="uc.id" v-model="selectedUserCouponId"
                        @change="applyUserCoupon(uc)" class="w-4 h-4 mt-1 accent-accent" />
                    <div class="flex-1 min-w-0">
                        <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
                            <span class="font-medium truncate">{{ uc.couponCode }}</span>
                            <span class="text-accent font-semibold whitespace-nowrap">
                                <template v-if="uc.couponType === 'PERCENT'">
                                    -{{ uc.couponValue }}%
                                </template>
                                <template v-else>
                                    -{{ uc.couponValue?.toLocaleString() }}đ
                                </template>
                            </span>
                            <span class="text-caption text-text-tertiary whitespace-nowrap">Còn {{ uc.usageRemain }}
                                lượt</span>
                        </div>
                        <div class="text-caption text-text-secondary mt-1 flex flex-wrap gap-x-3 gap-y-0.5">
                            <span>Đơn tối thiểu: {{ uc.minimumBookingValue?.toLocaleString() }}đ</span>
                            <span>Giảm tối đa: {{ uc.maximumDiscountAmount?.toLocaleString() }}đ</span>
                        </div>
                        <p v-if="uc.expiredAt" class="text-caption text-warning mt-0.5">
                            HSD: {{ new Date(uc.expiredAt).toLocaleDateString('vi-VN') }}
                        </p>
                    </div>
                </label>
            </div>
        </div>

        <!-- Phương thức thanh toán -->
        <div class="bg-bg-surface border border-border-default rounded-xl p-4 mb-4">
            <p class="text-body font-medium mb-3">Phương thức thanh toán</p>
            <div class="flex flex-col gap-3">
                <label
                    class="flex items-center gap-4 p-3 border border-border-default rounded-lg cursor-pointer hover:bg-bg-hover transition"
                    :class="{ 'border-accent bg-accent/5': selectedPaymentMethod === 'MOMO' }">
                    <input type="radio" value="MOMO" v-model="selectedPaymentMethod" class="w-5 h-5" />
                    <div class="flex items-center gap-3 flex-1">
                        <img :src="logoUrls.MOMO" class="w-10 h-10 rounded-lg object-cover" />
                        <div>
                            <div class="font-semibold">MoMo</div>
                            <div class="text-text-tertiary text-caption">Thanh toán qua ví MoMo, quét mã QR</div>
                        </div>
                    </div>
                </label>

                <label
                    class="flex items-center gap-4 p-3 border border-border-default rounded-lg cursor-pointer hover:bg-bg-hover transition"
                    :class="{ 'border-accent bg-accent/5': selectedPaymentMethod === 'VNPAY' }">
                    <input type="radio" value="VNPAY" v-model="selectedPaymentMethod" class="w-5 h-5" />
                    <div class="flex items-center gap-3 flex-1">
                        <img :src="logoUrls.VNPAY" class="w-10 h-10 rounded-lg object-cover" />
                        <div>
                            <div class="font-semibold">VNPAY</div>
                            <div class="text-text-tertiary text-caption">Hỗ trợ thẻ nội địa, Internet Banking</div>
                        </div>
                    </div>
                </label>

                <label
                    class="flex items-center gap-4 p-3 border border-border-default rounded-lg cursor-pointer hover:bg-bg-hover transition"
                    :class="{ 'border-accent bg-accent/5': selectedPaymentMethod === 'ZALOPAY' }">
                    <input type="radio" value="ZALOPAY" v-model="selectedPaymentMethod" class="w-5 h-5" />
                    <div class="flex items-center gap-3 flex-1">
                        <img :src="logoUrls.ZALOPAY" class="w-10 h-10 rounded-lg object-cover" />
                        <div>
                            <div class="font-semibold">ZaloPay</div>
                            <div class="text-text-tertiary text-caption">Thanh toán an toàn, tiện lợi trên Zalo</div>
                        </div>
                    </div>
                </label>
            </div>
        </div>

        <div v-if="booking.createError.value"
            class="mb-4 p-3 rounded-lg bg-red-500/10 border border-error/30 text-error">
            {{ booking.createError.value }}
        </div>
    </div>
</template>