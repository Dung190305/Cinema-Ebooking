<template>
  <div class="flex flex-col h-full">
    <div class="space-y-1">
      <MobileNavItem label="Phim" :items="movieMenuItems" @close="$emit('close')" />
      <MobileNavItem label="Lịch chiếu" :items="showtimeMenuItems" @close="$emit('close')" />
      <MobileNavItem label="Khuyến mãi" :items="promotionMenuItems" @close="$emit('close')" />
      <router-link
        to="/profile?tab=tickets"
        class="block px-4 py-3 text-text-primary hover:bg-accent/10 rounded-lg transition-colors"
        @click="$emit('close')"
      >
        Vé của tôi
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import MobileNavItem from './MobileNavItem.vue'

defineEmits<{
  close: []
}>()

const router = useRouter()

const movieMenuItems = [
  { label: 'Phim đang chiếu', route: '/movies', query: { status: 'NOW_SHOWING' } },
  { label: 'Phim sắp chiếu', route: '/movies', query: { status: 'COMING_SOON' } },
  { label: 'Phim đề xuất', route: '/movies', query: { status: 'NOW_SHOWING' } },
  { label: 'Phim dành cho bạn', route: '/movies', query: { status: 'RECOMMENDED' } },
]

const showtimeMenuItems = [
  {
    label: 'Suất chiếu hôm nay',
    route: '/showtimes',
    getQuery: () => ({ date: new Date().toISOString().split('T')[0] }),
  },
  { label: 'Suất chiếu theo rạp', route: '/showtimes' },
  { label: 'Tất cả suất chiếu', route: '/showtimes' },
]

const promotionMenuItems = [
  { label: 'Ưu đãi thành viên', route: '/promotions' },
  { label: 'Coupon', route: '/coupons' },
]
</script>
