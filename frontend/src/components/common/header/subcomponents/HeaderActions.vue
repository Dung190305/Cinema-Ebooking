<template>
    <div class="flex items-center gap-3 sm:gap-8">
        <BaseButton iconOnly size="sm" rounded="full" class="w-8! h-8! sm:w-9! sm:h-9!">
            <BaseIcon :icon="Search" :size="16" :scale="1.2" :stroke-width="1.5" />
        </BaseButton>

        <!-- Desktop: full button, Mobile: icon only -->
        <BaseButton variant="primary" size="lg" rounded="2xl" class="hidden sm:flex" @click.stop="goToBookingPage">
            Đặt vé ngay
        </BaseButton>
        <BaseButton variant="primary" iconOnly size="sm" rounded="full" class="sm:hidden">
            <BaseIcon :icon="Ticket" :size="18" />
        </BaseButton>

        <AuthSection />
    </div>
</template>
<script setup lang="ts">
import BaseButton from '@/components/ui/button/BaseButton.vue'
import { useRouter } from 'vue-router';
import { Search, Ticket } from 'lucide-vue-next'
import AuthSection from '@/components/common/header/subcomponents/AuthSection.vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue';

const router = useRouter()

function goToBookingPage() {
    router.push('/bookings')
}
</script>