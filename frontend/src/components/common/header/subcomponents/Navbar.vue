<template>
    <nav class="flex gap-3 sm:gap-6 items-center whitespace-nowrap ">
        <!-- Dropdown Phim -->
        <NavDropdownItem label="Phim" :items="movieMenuItems" />

        <!-- Dropdown Lịch chiếu -->
        <NavDropdownItem label="Lịch chiếu" :items="showtimeMenuItems" />

        <!-- Dropdown Khuyến mãi -->
        <NavDropdownItem label="Khuyến mãi" :items="promotionMenuItems" />

        <router-link to="/my-bookings"
            class="hover:underline hover:text-accent text-text-primary text-body-sm sm:text-body whitespace-nowrap">
            Vé của tôi
        </router-link>
    </nav>
</template>
<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavDropdownItem, { type NavDropdownItemData } from '@/components/common/header/subcomponents/NavDropdownItem.vue';
import { getDateKeyVN } from '@/utils/dateFormat'
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
            // Lấy ngày hôm nay theo giờ VN
            const todayVN = new Date().toLocaleString('en-US', { timeZone: 'Asia/Ho_Chi_Minh' })
            const todayDate = new Date(todayVN)
            const todayStr = getDateKeyVN(todayDate.toISOString())   // hoặc tự format
            // Hoặc dùng trực tiếp:
            // const todayStr = new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Ho_Chi_Minh' })
            router.push({ path: '/showtimes', query: { date: todayStr } })
        }
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