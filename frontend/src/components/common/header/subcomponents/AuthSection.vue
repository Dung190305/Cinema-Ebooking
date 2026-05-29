<template>
    <div class="min-w-36 sm:min-w-50 min-h-12 sm:min-h-18 flex items-center justify-end gap-2 sm:gap-4 cursor-pointer"
        :class="auth.user ? 'justify-start' : ''">
        <!-- CHƯA LOGIN -->
        <template v-if="!auth.user">
            <button @click="ui.openLoginModal()"
                class="text-text-secondary hover:text-accent transition-colors text-body-sm sm:text-body relative group">
                Đăng nhập
                <span class="absolute left-0 -bottom-1 w-0 h-px bg-accent transition-all group-hover:w-full"></span>
            </button>
        </template>

        <!-- ĐÃ LOGIN -->
        <template v-else>
            <div class="relative inline-block" @mouseenter="handleTriggerEnter" @mouseleave="handleTriggerLeave">
                <div ref="triggerRef" @click.stop="toggleDropdown"
                    class="flex items-center gap-2 sm:gap-4 cursor-pointer">
                    <div v-if="!auth.user.avatarUrl"
                        class="w-8 h-8 sm:w-10 sm:h-10 rounded-full bg-gray-500 flex items-center justify-center text-body-sm sm:text-body font-semibold">
                        {{ auth.user.fullName?.charAt(0) }}
                    </div>
                    <img v-else :src="auth.user.avatarUrl" class="w-8 h-8 sm:w-10 sm:h-10 rounded-full object-cover" />

                    <!-- User info - hidden on very small screens -->
                    <div class="hidden xs:flex flex-col gap-1 sm:gap-2">
                        <div class="flex items-center gap-1 sm:gap-2">
                            <MembershipIcon :membership="auth.loyaltyAccount?.tierName || 'basic'"></MembershipIcon>
                            <div class="flex flex-col">
                                <div class="truncate max-w-20 sm:max-w-29 text-body-sm sm:text-body font-medium"
                                    :title="auth.user.fullName">
                                    {{ auth.user?.fullName }}
                                </div>
                                <div class="text-[8px] sm:text-[10px] text-text-secondary uppercase">
                                    {{ auth.loyaltyAccount?.tierName || 'BASIC' }}
                                </div>
                            </div>
                        </div>
                        <div class="flex items-center gap-1 sm:gap-3">
                            <GiftIcon :membership="auth.loyaltyAccount?.tierName || 'basic'"></GiftIcon>
                            <div class="flex max-w-20 sm:max-w-29 text-xs sm:text-sm">
                                <span class="truncate" :title="auth.user.points">
                                    {{ (auth.loyaltyAccount?.currentPoints || 0).toLocaleString() }}
                                </span>
                                <span class="ml-0.5 sm:ml-1 shrink-0 text-xs sm:text-sm">
                                    Points
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Dropdown menu -->
                <Transition name="user-dropdown" appear>
                    <div v-if="showDropdown" ref="dropdownRef" @mouseenter="handleDropdownEnter"
                        @mouseleave="handleDropdownLeave"
                        class="absolute left-0 top-full mt-2 w-48 sm:w-56 bg-bg-surface border border-border-default rounded-md shadow-lg z-50">
                        <div>
                            <div @click="goToProfile"
                                class="block rounded-t-md px-3 sm:px-4 py-2 text-sm text-text-primary hover:bg-accent cursor-pointer">
                                Hồ sơ cá nhân
                            </div>

                            <div @click="goToCoupons"
                                class="block rounded-t-md px-3 sm:px-4 py-2 text-sm text-text-primary hover:bg-accent cursor-pointer">
                                Mã khuyến mãi
                            </div>
                            <div v-if="auth.user?.role == 'ADMIN'" @click="goToAdminPage"
                                class="group flex items-center justify-between px-3 sm:px-4 py-2 text-sm text-text-primary hover:bg-accent/30 cursor-pointer transition-all duration-200">
                                <span>Trang quản trị</span>
                                <span
                                    class="inline-flex items-center opacity-0 group-hover:opacity-100 translate-x-0 group-hover:translate-x-1 transition-all duration-200">
                                    <BaseIcon :icon="ArrowRight" :size="16" />
                                </span>
                            </div>
                            <div @click="logout"
                                class="block px-3 sm:px-4 rounded-b-md py-2 text-sm text-red-600 hover:bg-red-50 cursor-pointer border-t border-border-subtle">
                                Đăng xuất
                            </div>
                        </div>
                    </div>
                </Transition>
            </div>
        </template>
    </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import GiftIcon from '@/components/ui/icon/GiftIcon.vue'
import { useUIStore } from '@/stores/ui.store'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'
import MembershipIcon from '@/components/ui/icon/MembershipIcon.vue'
import { useSafeTriangleHover } from '@/composables/useSafeTriangleHover'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { ArrowRight } from 'lucide-vue-next'
const auth = useAuthStore()
const router = useRouter()
const ui = useUIStore()

const {
    showDropdown,
    handleTriggerEnter,
    handleTriggerLeave,
    handleDropdownEnter,
    handleDropdownLeave,
    setDropdownElement,
    setTriggerElement,
    cleanup,
} = useSafeTriangleHover(500)

const triggerRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)

onMounted(() => {
    if (triggerRef.value) {
        setTriggerElement(triggerRef.value)
    }
})

watch(dropdownRef, async (el) => {
    if (el) {
        // đảm bảo DOM đã layout xong
        await nextTick()
        setDropdownElement(el)
    } else {
        // Khi v-if hủy, el = null, xóa element cũ
        setDropdownElement(null)
    }
})

onUnmounted(() => {
    cleanup()
})

function logout() {
    auth.logout()
    router.push('/')
    showDropdown.value = false
}

function goToAdminPage() {
    if (!auth.isAdmin) return
    router.push('/admin/analytics/dashboard')
    showDropdown.value = false
}

function goToProfile() {
    router.push('/profile')
    showDropdown.value = false // đóng dropdown
}

function goToCoupons() {
    router.push('/my-coupons')
    showDropdown.value = false
}
</script>