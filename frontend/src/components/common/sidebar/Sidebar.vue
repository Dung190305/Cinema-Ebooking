<script setup lang="ts">
import SidebarMenuItem from '@/components/common/sidebar/SidebarMenuItem.vue'
import { useSidebarMenu } from '@/composables/useSidebarMenu'
import { useUIStore } from '@/stores/ui.store'
import { useAuthStore } from '@/stores/auth.store'

const auth = useAuthStore()
const ui = useUIStore()
const menu = useSidebarMenu()

const getInitial = (name: string) => {
    return name?.trim()?.charAt(0)?.toUpperCase() || 'A'
}
</script>

<template>
    <aside
        class="flex flex-col h-screen fixed left-0 top-16 shrink-0 bg-bg-admin-sidebar border-r border-border-admin-default overflow-hidden"
        :style="{
            width: ui.isSidebarCollapsed
                ? 'var(--sidebar-collapsed-width)'
                : 'var(--sidebar-width)'
        }">

        <!-- TOP: ADMIN INFO -->
        <div class="flex group relative flex-col items-center px-3 py-4 border-b border-border-admin-default shrink-0">

            <!-- AVATAR -->
            <div
                class="w-11 h-11 rounded-full overflow-hidden flex items-center justify-center bg-gray-300 text-white font-semibold shrink-0">

                <img v-if="auth.user?.avatarUrl" :src="auth.user?.avatarUrl" class="w-full h-full object-cover"
                    @error="$event.target.style.display = 'none'" />

                <span v-else>
                    {{ getInitial(auth.user?.fullName) }}
                </span>
            </div>

            <!-- INFO (expanded) -->
            <div v-if="!ui.isSidebarCollapsed" class="flex flex-col items-center gap-0.5 mt-2">
                <h1 class="text-body text-text-admin-primary text-center leading-tight">{{ auth.user?.fullName }}</h1>
                <h2 class="text-xs text-text-admin-tertiary uppercase">{{ auth.user?.role }}</h2>
            </div>

            <!-- TOOLTIP (collapsed) -->
            <div v-if="ui.isSidebarCollapsed"
                class="absolute left-full ml-3 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none bg-overlay-light-70 text-white text-xs px-2 py-1 rounded whitespace-nowrap z-50">
                {{ auth.user?.fullName }}
            </div>
        </div>

        <!-- MENU -->
        <nav class="flex-1 overflow-y-auto hide-scrollbar px-2 py-3">
            <SidebarMenuItem v-for="item in menu" :key="item.key" :item="item" />
        </nav>

    </aside>
</template>
