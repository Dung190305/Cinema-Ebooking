<template>
    <nav class="flex gap-6 items-center">
        <!-- Dropdown Phim -->
        <NavDropdownItem label="Phim" :items="movieMenuItems" />

        <!-- Dropdown Lịch chiếu -->
        <NavDropdownItem label="Lịch chiếu" :items="showtimeMenuItems" />

        <!-- Dropdown Khuyến mãi (tùy chọn) -->
        <NavDropdownItem label="Khuyến mãi" :items="promotionMenuItems" />

        <!-- Link vé của tôi -->
        <router-link to="/profile?tab=tickets" class="hover:underline hover:text-accent text-text-primary text-body">
            Vé của tôi
        </router-link>
    </nav>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavDropdownItem, { type NavDropdownItemData } from '@/components/common/header/subcomponents/NavDropdownItem.vue';

const router = useRouter()
// Danh sách item cho dropdown "Phim"
const movieMenuItems: NavDropdownItemData[] = [
    {
        label: 'Phim đang chiếu',
        onClick: () => router.push({ path: '/movies', query: { status: 'NOW_SHOWING' } }),
    },
    {
        label: 'Phim sắp chiếu',
        onClick: () => router.push({ path: '/movies', query: { status: 'COMING_SOON' } }),
    },
    {
        label: 'Phim đề xuất',
        // API hiện chưa hỗ trợ RECOMMENDED, có thể dẫn về trang mặc định
        onClick: () => router.push({ path: '/movies', query: { status: 'NOW_SHOWING' } }),
    },
]

const showtimeMenuItems: NavDropdownItemData[] = [
    {
        label: 'Suất chiếu hôm nay',
        onClick: () => {
            const today = new Date().toISOString().split('T')[0]
            router.push({ path: '/showtimes', query: { date: today } })
        }
    },
    {
        label: 'Suất chiếu theo rạp',
        onClick: () => router.push('/showtimes')
    },
    {
        label: 'Tất cả suất chiếu',
        onClick: () => router.push('/showtimes')
    }
]

// Danh sách item cho dropdown "Khuyến mãi" (giữ nguyên hoặc tạo mới)
const promotionMenuItems: NavDropdownItemData[] = [
    {
        label: 'Ưu đãi thành viên',
        onClick: () => router.push('/promotions')
    },
    {
        label: 'Coupon',
        onClick: () => router.push('/coupons')
    }
]
</script>