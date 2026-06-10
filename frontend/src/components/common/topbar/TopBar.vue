<template>
    <!-- TOPBAR -->
    <header class="flex flex-col gap-2 w-full h-fit bg-bg-admin-topbar text-text-on-admin-topbar py-4">
        <div class="flex items-center w-full pr-4" :style="{ gap: 'var(--sidebar-right-gap)' }">

            <!-- LEFT: Logo + Collapse -->
            <div class="flex items-center shrink-0 pl-16" :style="{ width: 'var(--sidebar-width)' }">
                <Logo isAdminPage />
                <div class="ml-auto">
                    <BaseButton @click="ui.toggleSidebar()" iconOnly size="sm" rounded="full">
                        <BaseIcon :size="24" :scale="1.1" :icon="ui.isSidebarCollapsed ? ChevronRight : ChevronLeft" />
                    </BaseButton>
                </div>
            </div>

            <!-- MIDDLE: Check-in -->
            <BaseButton iconOnly size="sm" rounded="full" @click="goToCheckIn" title="Check-in">
                <BaseIcon :icon="QrCode" :size="16" :scale="1.2" :stroke-width="1.5" />
            </BaseButton>

            <!-- SPACER -->
            <div class="flex-1"></div>

            <!-- RIGHT: Logout -->
            <BaseButton iconOnly size="md" rounded="full"
                customClass="text-text-admin-secondary hover:text-red-400 hover:bg-red-400/10 focus:ring-2 focus:ring-red-400/50"
                @click="logout" title="Đăng xuất">
                <BaseIcon :icon="LogOut" :size="18" :scale="1.2" :stroke-width="1.5" />
            </BaseButton>
        </div>


        <div class="flex items-start w-fit" :style="{
            paddingLeft: !ui.isSidebarCollapsed
                ? 'calc(var(--sidebar-width) + var(--sidebar-right-gap))'
                : 'calc(var(--sidebar-collapsed-width) + var(--sidebar-right-gap))'
        }">
            <!-- content -->
            <div class="flex flex-col gap-1">
                <h1 class="text-title">{{ pageTitle }}</h1>
            </div>
        </div>
    </header>
</template>
<script setup>
import Logo from '@/components/ui/logo/Logo.vue'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { ChevronLeft, ChevronRight, QrCode, LogOut } from 'lucide-vue-next'
import { useUIStore } from '@/stores/ui.store'
import { useBreadcrumb } from '@/composables/useBreadcrumb'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const ui = useUIStore()

function logout() {
    auth.logout()
    router.push('/')
}

function goToCheckIn() {
    router.push('/admin/operations/checkin')
}
const { pageTitle, breadcrumb } = useBreadcrumb()
</script>