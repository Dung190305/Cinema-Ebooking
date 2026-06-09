<!-- pages/RefundPolicyPage.vue -->
<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const tiers = [
    { condition: '≥ 72 giờ trước suất chiếu', percentage: 70, color: 'text-green-600', bg: 'bg-green-50 border-green-200' },
    { condition: '24 – 72 giờ trước suất chiếu', percentage: 50, color: 'text-blue-600', bg: 'bg-blue-50 border-blue-200' },
    { condition: '6 – 24 giờ trước suất chiếu', percentage: 25, color: 'text-yellow-600', bg: 'bg-yellow-50 border-yellow-200' },
    { condition: '2 – 6 giờ trước suất chiếu', percentage: 10, color: 'text-orange-500', bg: 'bg-orange-50 border-orange-200' },
    { condition: 'Dưới 2 giờ hoặc sau suất chiếu', percentage: 0, color: 'text-red-500', bg: 'bg-red-50 border-red-200' },
]

const steps = [
    { icon: 'ticket', label: 'Vào trang "Vé của tôi"', desc: 'Chọn đơn hàng đã thanh toán muốn hủy' },
    { icon: 'click', label: 'Nhấn "Yêu cầu hủy vé"', desc: 'Xem ước tính hoàn tiền và xác nhận' },
    { icon: 'check', label: 'Gửi yêu cầu', desc: 'Yêu cầu được gửi đến quản trị viên để duyệt' },
    { icon: 'clock', label: 'Chờ phê duyệt', desc: 'Quản trị viên xử lý trong 5–7 ngày làm việc' },
    { icon: 'money', label: 'Nhận hoàn tiền', desc: 'Tiền được hoàn về phương thức thanh toán ban đầu' },
]
</script>

<template>
    <div class="min-h-screen bg-bg-base">
        <!-- Hero -->
        <div class="bg-bg-surface border-b border-border-subtle">
            <div class="max-w-4xl mx-auto px-4 sm:px-6 pt-4 pb-6 sm:pt-6 sm:pb-8">
                <button @click="router.back()"
                    class="inline-flex items-center gap-1.5 text-caption text-text-tertiary hover:text-accent transition-colors mb-6 group">
                    <svg class="w-4 h-4 transition-transform group-hover:-translate-x-0.5" fill="none"
                        stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                    </svg>
                    Quay lại
                </button>
                <div class="flex items-start gap-4">
                    <div class="w-12 h-12 rounded-xl bg-accent/10 flex items-center justify-center shrink-0">
                        <svg class="w-6 h-6 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M9 14l6-6m-5.5.5h.01m4.99 5h.01M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16l3.5-2 3.5 2 3.5-2 3.5 2z" />
                        </svg>
                    </div>
                    <div>
                        <h1 class="text-display text-text-primary">Chính sách hoàn tiền</h1>
                        <p class="text-body text-text-secondary mt-1.5">
                            Hệ thống hỗ trợ hoàn tiền linh hoạt theo thời gian hủy vé. Vui lòng đọc kỹ trước khi yêu cầu
                            hủy.
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <div class="max-w-4xl mx-auto px-4 sm:px-6 py-10 space-y-10">

            <!-- Section 1: Điều kiện -->
            <section class="bg-bg-surface rounded-2xl border border-border-subtle overflow-hidden">
                <div class="px-6 py-4 border-b border-border-subtle bg-bg-base">
                    <h2 class="text-title text-text-primary flex items-center gap-2">
                        <span
                            class="w-6 h-6 rounded-md bg-accent text-white text-xs font-bold flex items-center justify-center">1</span>
                        Điều kiện được hoàn tiền
                    </h2>
                </div>
                <div class="px-6 py-5 space-y-3">
                    <p class="text-body text-text-secondary">Yêu cầu hoàn tiền được chấp nhận khi thỏa mãn <strong
                            class="text-text-primary">tất cả</strong> các điều kiện sau:</p>
                    <ul class="space-y-2.5">
                        <li class="flex items-start gap-3">
                            <span
                                class="mt-0.5 w-5 h-5 rounded-full bg-green-100 flex items-center justify-center shrink-0">
                                <svg class="w-3 h-3 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                                    <path fill-rule="evenodd"
                                        d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                        clip-rule="evenodd" />
                                </svg>
                            </span>
                            <span class="text-body text-text-primary">Booking đã được <strong>thanh toán thành
                                    công</strong> (trạng thái: Đã thanh toán)</span>
                        </li>
                        <li class="flex items-start gap-3">
                            <span
                                class="mt-0.5 w-5 h-5 rounded-full bg-green-100 flex items-center justify-center shrink-0">
                                <svg class="w-3 h-3 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                                    <path fill-rule="evenodd"
                                        d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                        clip-rule="evenodd" />
                                </svg>
                            </span>
                            <span class="text-body text-text-primary">Vé <strong>chưa được sử dụng</strong> (chưa
                                check-in tại rạp)</span>
                        </li>
                        <li class="flex items-start gap-3">
                            <span
                                class="mt-0.5 w-5 h-5 rounded-full bg-green-100 flex items-center justify-center shrink-0">
                                <svg class="w-3 h-3 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                                    <path fill-rule="evenodd"
                                        d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                        clip-rule="evenodd" />
                                </svg>
                            </span>
                            <span class="text-body text-text-primary">Yêu cầu hủy được gửi <strong>ít nhất 2
                                    giờ</strong> trước giờ chiếu</span>
                        </li>
                    </ul>

                    <!-- Not eligible note -->
                    <div class="mt-4 flex items-start gap-2 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
                        <svg class="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <p class="text-caption text-red-700">
                            Nếu không đáp ứng đủ điều kiện, hệ thống sẽ từ chối yêu cầu hoàn tiền và bạn sẽ không nhận
                            được bất kỳ khoản hoàn nào.
                        </p>
                    </div>
                </div>
            </section>

            <!-- Section 2: Tỷ lệ hoàn tiền -->
            <section class="bg-bg-surface rounded-2xl border border-border-subtle overflow-hidden">
                <div class="px-6 py-4 border-b border-border-subtle bg-bg-base">
                    <h2 class="text-title text-text-primary flex items-center gap-2">
                        <span
                            class="w-6 h-6 rounded-md bg-accent text-white text-xs font-bold flex items-center justify-center">2</span>
                        Tỷ lệ hoàn tiền theo thời gian
                    </h2>
                </div>
                <div class="px-6 py-5">
                    <p class="text-body text-text-secondary mb-5">
                        Số tiền hoàn phụ thuộc vào thời điểm bạn gửi yêu cầu so với giờ chiếu. Thời gian tính theo đồng
                        hồ hệ thống tại thời điểm nhấn "Xác nhận hủy vé".
                    </p>

                    <!-- Desktop table -->
                    <div class="hidden sm:block rounded-xl border border-border-subtle overflow-hidden">
                        <table class="w-full text-left">
                            <thead>
                                <tr class="bg-bg-base border-b border-border-subtle">
                                    <th class="px-5 py-3 text-caption font-semibold text-text-secondary">Thời điểm hủy
                                    </th>
                                    <th class="px-5 py-3 text-caption font-semibold text-text-secondary text-right">Tỷ
                                        lệ hoàn</th>
                                    <th class="px-5 py-3 text-caption font-semibold text-text-secondary text-right">Ví
                                        dụ (500.000 ₫)</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-border-subtle">
                                <tr v-for="(tier, i) in tiers" :key="i" class="hover:bg-bg-base transition-colors">
                                    <td class="px-5 py-3.5 text-body text-text-primary">{{ tier.condition }}</td>
                                    <td class="px-5 py-3.5 text-right">
                                        <span :class="['text-body font-bold', tier.color]">{{ tier.percentage }}%</span>
                                    </td>
                                    <td class="px-5 py-3.5 text-right text-body text-text-secondary">
                                        {{ tier.percentage > 0 ? new Intl.NumberFormat('vi-VN', {
                                            style: 'currency',
                                            currency: 'VND'
                                        }).format(500000 * tier.percentage / 100) : 'Không hoàn' }}
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <!-- Mobile cards -->
                    <div class="sm:hidden space-y-3">
                        <div v-for="(tier, i) in tiers" :key="i"
                            :class="['rounded-xl border px-4 py-4 flex items-center justify-between', tier.bg]">
                            <div class="flex-1 min-w-0 pr-3">
                                <p class="text-caption font-medium text-text-primary">{{ tier.condition }}</p>
                                <p class="text-caption text-text-tertiary mt-0.5">
                                    Ví dụ 500.000 ₫ → {{ tier.percentage > 0 ? new Intl.NumberFormat('vi-VN', {
                                        style:
                                            'currency', currency: 'VND'
                                    }).format(500000 * tier.percentage / 100) : 'Không hoàn'
                                    }}
                                </p>
                            </div>
                            <span :class="['text-title font-extrabold shrink-0', tier.color]">{{ tier.percentage
                                }}%</span>
                        </div>
                    </div>

                    <!-- Note -->
                    <div
                        class="mt-5 flex items-start gap-2 bg-bg-base border border-border-subtle rounded-lg px-4 py-3">
                        <svg class="w-4 h-4 text-text-tertiary mt-0.5 shrink-0" fill="none" stroke="currentColor"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <p class="text-caption text-text-secondary">
                            Số tiền hoàn tính trên <strong>tổng số tiền thanh toán thực tế</strong> (đã trừ ưu đãi thành
                            viên và mã giảm giá). Điểm loyalty đã tích từ booking này cũng sẽ bị trừ lại.
                        </p>
                    </div>
                </div>
            </section>

            <!-- Section 3: Quy trình -->
            <section class="bg-bg-surface rounded-2xl border border-border-subtle overflow-hidden">
                <div class="px-6 py-4 border-b border-border-subtle bg-bg-base">
                    <h2 class="text-title text-text-primary flex items-center gap-2">
                        <span
                            class="w-6 h-6 rounded-md bg-accent text-white text-xs font-bold flex items-center justify-center">3</span>
                        Quy trình hoàn tiền
                    </h2>
                </div>
                <div class="px-6 py-5">
                    <div class="relative">
                        <!-- Vertical line -->
                        <div class="absolute left-5 top-5 bottom-5 w-px bg-border-subtle hidden sm:block"></div>

                        <div class="space-y-4">
                            <div v-for="(step, i) in steps" :key="i" class="flex items-start gap-4">
                                <div
                                    class="relative z-10 w-10 h-10 rounded-full bg-accent/10 border-2 border-accent/20 flex items-center justify-center shrink-0">
                                    <span class="text-caption font-bold text-accent">{{ i + 1 }}</span>
                                </div>
                                <div class="pt-1.5 pb-1">
                                    <p class="text-body font-semibold text-text-primary">{{ step.label }}</p>
                                    <p class="text-caption text-text-secondary mt-0.5">{{ step.desc }}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Section 4: Lưu ý bổ sung -->
            <section class="bg-bg-surface rounded-2xl border border-border-subtle overflow-hidden">
                <div class="px-6 py-4 border-b border-border-subtle bg-bg-base">
                    <h2 class="text-title text-text-primary flex items-center gap-2">
                        <span
                            class="w-6 h-6 rounded-md bg-accent text-white text-xs font-bold flex items-center justify-center">4</span>
                        Lưu ý bổ sung
                    </h2>
                </div>
                <div class="px-6 py-5 space-y-3">
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <div class="flex items-start gap-3 bg-bg-base rounded-xl border border-border-subtle p-4">
                            <svg class="w-5 h-5 text-blue-500 mt-0.5 shrink-0" fill="none" stroke="currentColor"
                                viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <div>
                                <p class="text-caption font-semibold text-text-primary">Thời gian xử lý</p>
                                <p class="text-caption text-text-secondary mt-0.5">5–7 ngày làm việc sau khi quản trị
                                    viên phê duyệt yêu cầu.</p>
                            </div>
                        </div>

                        <div class="flex items-start gap-3 bg-bg-base rounded-xl border border-border-subtle p-4">
                            <svg class="w-5 h-5 text-purple-500 mt-0.5 shrink-0" fill="none" stroke="currentColor"
                                viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                            </svg>
                            <div>
                                <p class="text-caption font-semibold text-text-primary">Phương thức hoàn tiền</p>
                                <p class="text-caption text-text-secondary mt-0.5">Hoàn về phương thức thanh toán gốc
                                    (thẻ ngân hàng, ví điện tử...).</p>
                            </div>
                        </div>

                        <div class="flex items-start gap-3 bg-bg-base rounded-xl border border-border-subtle p-4">
                            <svg class="w-5 h-5 text-orange-500 mt-0.5 shrink-0" fill="none" stroke="currentColor"
                                viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                            </svg>
                            <div>
                                <p class="text-caption font-semibold text-text-primary">Điểm loyalty</p>
                                <p class="text-caption text-text-secondary mt-0.5">Điểm tích lũy từ booking bị hủy sẽ bị
                                    thu hồi sau khi hoàn tiền.</p>
                            </div>
                        </div>

                        <div class="flex items-start gap-3 bg-bg-base rounded-xl border border-border-subtle p-4">
                            <svg class="w-5 h-5 text-green-500 mt-0.5 shrink-0" fill="none" stroke="currentColor"
                                viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                            </svg>
                            <div>
                                <p class="text-caption font-semibold text-text-primary">Coupon đã sử dụng</p>
                                <p class="text-caption text-text-secondary mt-0.5">Coupon đã áp dụng sẽ được hoàn lại
                                    vào tài khoản sau khi hủy thành công.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- CTA -->
            <div class="text-center py-4">
                <p class="text-caption text-text-tertiary mb-4">Có thắc mắc về chính sách hoàn tiền?</p>
                <router-link to="/my-bookings"
                    class="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-accent text-white text-body font-medium hover:opacity-90 transition-opacity">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
                    </svg>
                    Xem vé của tôi
                </router-link>
            </div>

        </div>
    </div>
</template>