<template>
    <nav class="flex gap-6 items-center">
        <!-- Dropdown Phim -->
        <NavDropdownItem label="Phim" :items="movieMenuItems" />

        <!-- Các mục khác (chưa có dropdown) -->
        <a class="flex items-center gap-2 hover:underline hover:text-accent text-text-primary text-body">
            Lịch chiếu
            <BaseIcon :icon="ChevronDown" :size="14" :scale="1.2" :stroke-width="1.5" />
        </a>
        <a class="flex items-center gap-2 hover:underline hover:text-accent text-text-primary text-body">
            Khuyến mãi
            <BaseIcon :icon="ChevronDown" :size="14" :scale="1.2" :stroke-width="1.5" />
        </a>
        <a class="hover:underline hover:text-accent text-text-primary text-body">Vé của tôi</a>
    </nav>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ChevronDown } from 'lucide-vue-next';
import BaseIcon from '@/components/ui/icon/BaseIcon.vue';
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
</script>