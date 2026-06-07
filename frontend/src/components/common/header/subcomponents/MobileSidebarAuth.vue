<template>
    <!-- CHƯA LOGIN -->
    <div v-if="!auth.user" class="flex flex-col gap-2 px-1">
        <p class="text-xs text-text-secondary px-1">Đăng nhập để đặt vé và tích điểm</p>
        <button @click="() => { ui.openLoginModal(); emit('close') }"
            class="w-full py-2.5 rounded-xl bg-accent text-white text-sm font-medium hover:bg-accent/90 transition-colors">
            Đăng nhập
        </button>
    </div>

    <!-- ĐÃ LOGIN -->
    <div v-else class="px-1">
        <!-- User info card -->
        <div class="flex items-center gap-3 px-3 py-3 rounded-xl bg-bg-elevated mb-2">
            <div v-if="!auth.user.avatarUrl"
                class="w-10 h-10 rounded-full bg-gray-500 flex items-center justify-center text-sm font-semibold shrink-0">
                {{ auth.user.fullName?.charAt(0) }}
            </div>
            <img v-else :src="auth.user.avatarUrl" class="w-10 h-10 rounded-full object-cover shrink-0" />

            <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-text-primary truncate">{{ auth.user.fullName }}</p>
                <p class="text-xs text-text-secondary">
                    {{ (auth.loyaltyAccount?.currentPoints || 0).toLocaleString() }} điểm ·
                    {{ auth.loyaltyAccount?.tierName || 'BASIC' }}
                </p>
            </div>
        </div>

        <!-- Menu items -->
        <div class="space-y-0.5">
            <button @click="navigate('/profile')"
                class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-text-primary hover:bg-accent/10 transition-colors text-left">
                <BaseIcon :icon="User" :size="16" class="text-text-secondary shrink-0" />
                Hồ sơ cá nhân
            </button>
            <button @click="navigate('/my-coupons')"
                class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-text-primary hover:bg-accent/10 transition-colors text-left">
                <BaseIcon :icon="Ticket" :size="16" class="text-text-secondary shrink-0" />
                Mã khuyến mãi
            </button>
            <button v-if="auth.user?.role === 'ADMIN'" @click="navigate('/admin/analytics/dashboard')"
                class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-text-primary hover:bg-accent/10 transition-colors text-left">
                <BaseIcon :icon="Shield" :size="16" class="text-text-secondary shrink-0" />
                Trang quản trị
            </button>

            <div class="pt-1 border-t border-border-default mt-1">
                <button @click="logout"
                    class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-red-500 hover:bg-red-50 dark:hover:bg-red-950/20 transition-colors text-left">
                    <BaseIcon :icon="LogOut" :size="16" class="shrink-0" />
                    Đăng xuất
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth.store'
import { useUIStore } from '@/stores/ui.store'
import { useRouter } from 'vue-router'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { User, Ticket, Shield, LogOut } from 'lucide-vue-next'

const emit = defineEmits<{ close: [] }>()
const auth = useAuthStore()
const ui = useUIStore()
const router = useRouter()

function navigate(path: string) {
    router.push(path)
    emit('close')
}

function logout() {
    auth.logout()
    router.push('/')
    emit('close')
}
</script>