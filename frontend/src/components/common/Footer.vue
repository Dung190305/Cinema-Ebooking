<template>
  <footer class="w-full bg-bg-surface border-t border-border-subtle">

    <!-- Main content -->
    <div class="max-w-7xl mx-auto px-6 pt-12 pb-8">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10">

        <!-- Col 1: Brand -->
        <div class="lg:col-span-1 space-y-4">
          <Logo />
          <p class="text-caption text-text-tertiary leading-relaxed">
            Đặt vé xem phim nhanh chóng, tiện lợi. Trải nghiệm điện ảnh đỉnh cao chỉ với vài cú chạm.
          </p>
          <!-- Social buttons -->
          <div class="flex items-center gap-2 pt-1">
            <a href="https://www.facebook.com" target="_blank" rel="noopener noreferrer" aria-label="Facebook"
              class="group w-9 h-9 rounded-lg border border-border-subtle bg-bg-base flex items-center justify-center text-text-tertiary hover:border-[#1877F2] hover:text-[#1877F2] hover:bg-[#1877F2]/5 transition-all duration-200">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                <path
                  d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
              </svg>
            </a>
            <a href="https://www.youtube.com" target="_blank" rel="noopener noreferrer" aria-label="YouTube"
              class="group w-9 h-9 rounded-lg border border-border-subtle bg-bg-base flex items-center justify-center text-text-tertiary hover:border-[#FF0000] hover:text-[#FF0000] hover:bg-[#FF0000]/5 transition-all duration-200">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                <path
                  d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z" />
              </svg>
            </a>
            <a href="https://www.tiktok.com" target="_blank" rel="noopener noreferrer" aria-label="TikTok"
              class="group w-9 h-9 rounded-lg border border-border-subtle bg-bg-base flex items-center justify-center text-text-tertiary hover:border-text-primary hover:text-text-primary hover:bg-bg-base transition-all duration-200">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                <path
                  d="M19.59 6.69a4.83 4.83 0 01-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 01-2.88 2.5 2.89 2.89 0 01-2.89-2.89 2.89 2.89 0 012.89-2.89c.28 0 .54.04.79.1V9.01a6.27 6.27 0 00-.79-.05 6.34 6.34 0 00-6.34 6.34 6.34 6.34 0 006.34 6.34 6.34 6.34 0 006.33-6.34V8.69a8.17 8.17 0 004.78 1.52V6.76a4.85 4.85 0 01-1.01-.07z" />
              </svg>
            </a>
          </div>
        </div>

        <!-- Col 2: Khám phá -->
        <div class="space-y-4">
          <h3 class="text-caption font-semibold text-text-primary uppercase tracking-wider">Khám phá</h3>
          <ul class="space-y-2.5">
            <li>
              <router-link :to="{ name: 'movies' }"
                class="text-caption text-text-tertiary hover:text-accent transition-colors inline-flex items-center gap-1.5 group">
                <span class="w-1 h-1 rounded-full bg-border-default group-hover:bg-accent transition-colors"></span>
                Phim đang chiếu
              </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'showtimes' }"
                class="text-caption text-text-tertiary hover:text-accent transition-colors inline-flex items-center gap-1.5 group">
                <span class="w-1 h-1 rounded-full bg-border-default group-hover:bg-accent transition-colors"></span>
                Lịch chiếu
              </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'coupon-browse' }"
                class="text-caption text-text-tertiary hover:text-accent transition-colors inline-flex items-center gap-1.5 group">
                <span class="w-1 h-1 rounded-full bg-border-default group-hover:bg-accent transition-colors"></span>
                Ưu đãi & Coupon
              </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'LoyaltyProgram' }"
                class="text-caption text-text-tertiary hover:text-accent transition-colors inline-flex items-center gap-1.5 group">
                <span class="w-1 h-1 rounded-full bg-border-default group-hover:bg-accent transition-colors"></span>
                Chương trình thành viên
              </router-link>
            </li>
          </ul>
        </div>

        <!-- Col 3: Hỗ trợ -->
        <div class="space-y-4">
          <h3 class="text-caption font-semibold text-text-primary uppercase tracking-wider">Hỗ trợ</h3>
          <ul class="space-y-2.5">
            <li>
              <router-link :to="{ name: 'refund-policy' }"
                class="text-caption text-text-tertiary hover:text-accent transition-colors inline-flex items-center gap-1.5 group">
                <span class="w-1 h-1 rounded-full bg-border-default group-hover:bg-accent transition-colors"></span>
                Chính sách hoàn tiền
              </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'my-bookings' }"
                class="text-caption text-text-tertiary hover:text-accent transition-colors inline-flex items-center gap-1.5 group">
                <span class="w-1 h-1 rounded-full bg-border-default group-hover:bg-accent transition-colors"></span>
                Vé của tôi
              </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'my-coupons' }"
                class="text-caption text-text-tertiary hover:text-accent transition-colors inline-flex items-center gap-1.5 group">
                <span class="w-1 h-1 rounded-full bg-border-default group-hover:bg-accent transition-colors"></span>
                Coupon của tôi
              </router-link>
            </li>
          </ul>
        </div>

        <!-- Col 4: Liên hệ -->
        <div class="space-y-4">
          <h3 class="text-caption font-semibold text-text-primary uppercase tracking-wider">Liên hệ</h3>
          <ul class="space-y-3">
            <li>
              <a href="tel:+84708378286"
                class="flex items-start gap-2.5 text-caption text-text-tertiary hover:text-accent transition-colors group">
                <span
                  class="mt-0.5 w-6 h-6 rounded-md border border-border-subtle bg-bg-base flex items-center justify-center shrink-0 group-hover:border-accent/40 transition-colors">
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                </span>
                <div>
                  <p class="text-text-secondary font-medium">Hotline</p>
                  <p>+84 708 378 286</p>
                </div>
              </a>
            </li>
            <li>
              <a href="mailto:support@cinemaebooking.vn"
                class="flex items-start gap-2.5 text-caption text-text-tertiary hover:text-accent transition-colors group">
                <span
                  class="mt-0.5 w-6 h-6 rounded-md border border-border-subtle bg-bg-base flex items-center justify-center shrink-0 group-hover:border-accent/40 transition-colors">
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                </span>
                <div>
                  <p class="text-text-secondary font-medium">Email</p>
                  <p>support@cinemaebooking.vn</p>
                </div>
              </a>
            </li>
            <li>
              <div class="flex items-start gap-2.5 text-caption text-text-tertiary">
                <span
                  class="mt-0.5 w-6 h-6 rounded-md border border-border-subtle bg-bg-base flex items-center justify-center shrink-0">
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </span>
                <div>
                  <p class="text-text-secondary font-medium">Giờ hỗ trợ</p>
                  <p>8:00 – 22:00 (T2 – CN)</p>
                </div>
              </div>
            </li>
          </ul>
        </div>

      </div>
    </div>

    <!-- Bottom bar -->
    <div class="border-t border-border-subtle">
      <div class="max-w-7xl mx-auto px-6 py-4 flex flex-col sm:flex-row items-center justify-between gap-2">
        <p class="text-caption text-text-tertiary order-2 sm:order-1">
          © {{ new Date().getFullYear() }} Cinema E-booking. All rights reserved.
        </p>
        <div class="flex items-center gap-4 order-1 sm:order-2">
          <span class="text-caption text-text-disabled">Được xây dựng với ❤️ tại Việt Nam</span>
        </div>
      </div>
    </div>

  </footer>
</template>

<script setup lang="ts">
import Logo from '@/components/ui/logo/Logo.vue'
</script>