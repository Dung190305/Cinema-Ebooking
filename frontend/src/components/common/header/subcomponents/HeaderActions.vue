<template>
    <div class="flex items-center gap-3 sm:gap-8">
        <!-- Search button: chỉ hiện từ sm trở lên, mobile dùng sidebar -->
        <button @click="emit('toggleSearch')"
            class="hidden sm:flex p-2 rounded-lg hover:bg-bg-elevated transition-colors" aria-label="Tìm kiếm phim">
            <BaseIcon :icon="searchOpen ? X : Search" :size="20" :stroke-width="1.5" />
        </button>

        <!-- Desktop: full button, Mobile: icon only -->
        <BaseButton variant="primary" size="lg" rounded="2xl" custom-class="hidden! sm:flex!"
            @click.stop="goToBookingPage">
            Đặt vé ngay
        </BaseButton>
        <BaseButton variant="primary" iconOnly size="sm" rounded="full" custom-class="sm:hidden"
            @click.stop="goToBookingPage">
            <BaseIcon :icon="Ticket" :size="18" />
        </BaseButton>

        <!-- AuthSection chỉ hiện trên desktop -->
        <div class="hidden sm:block">
            <AuthSection />
        </div>
    </div>
</template>

<script setup lang="ts">
import BaseButton from '@/components/ui/button/BaseButton.vue'
import { useRouter } from 'vue-router';
import { Search, Ticket, X } from 'lucide-vue-next'
import AuthSection from '@/components/common/header/subcomponents/AuthSection.vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue';

const router = useRouter()
const props = defineProps<{ searchOpen: boolean }>()
const emit = defineEmits<{ toggleSearch: [] }>()

function goToBookingPage() {
    sessionStorage.setItem('booking_force_reset', 'true')
    router.push('/bookings')
}
</script>