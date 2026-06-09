<template>
  <header class="w-full text-text-primary px-4 sm:px-8 md:px-16 pt-4 pb-4 flex items-center justify-between gap-4">
    <Logo :isAdminPage="false" />

    <!-- Desktop Navigation (hidden on mobile) -->
    <div class="hidden md:block">
      <Navbar />
    </div>

    <div class="flex items-center gap-4 sm:gap-8">
      <HeaderActions :search-open="searchOpen" @toggle-search="toggleSearch" />

      <!-- Mobile menu button -->
      <button @click="toggleMobileMenu"
        class="md:hidden p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors" aria-label="Menu">
        <BaseIcon :icon="Menu" :size="24" :stroke-width="1.5" />
      </button>
    </div>
  </header>

  <SearchBar :is-visible="searchOpen" @close="searchOpen = false" />

  <!-- Mobile Navigation Drawer -->
  <Transition name="mobile-drawer">
    <div v-if="mobileMenuOpen" class="fixed inset-0 z-50 md:hidden">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/50" @click="closeMobileMenu"></div>

      <!-- Drawer -->
      <div class="absolute right-0 top-0 h-full w-80 max-w-[85vw] bg-bg-surface shadow-xl flex flex-col drawer-panel">
        <!-- Drawer Header -->
        <div class="flex justify-end p-4 border-b border-border-default">
          <button @click="closeMobileMenu"
            class="p-1 aspect-square rounded-full hover:bg-overlay-light-30 dark:hover:bg-overlay-dark-30 text-white transition-colors"
            aria-label="Close menu">
            <BaseIcon :icon="X" :size="16" :scale="1.2" />
          </button>
        </div>

        <!-- Mobile Navigation Content -->
        <div class="flex-1 overflow-y-auto p-4">
          <MobileNav @close="closeMobileMenu" />
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useTheme } from '@/composables/useTheme'
import Logo from '@/components/ui/logo/Logo.vue'
import Navbar from '@/components/common/header/subcomponents/Navbar.vue'
import HeaderActions from '@/components/common/header/subcomponents/HeaderActions.vue'
import MobileNav from '@/components/common/header/subcomponents/MobileNav.vue'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import { Menu, X } from 'lucide-vue-next'
import SearchBar from '@/components/common/search/SearchBar.vue'

const { toggleTheme, initTheme } = useTheme()
const mobileMenuOpen = ref(false)

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
  if (mobileMenuOpen.value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
  document.body.style.overflow = ''
}

onMounted(() => {
  initTheme()
})

onUnmounted(() => {
  document.body.style.overflow = ''
})

const searchOpen = ref(false)
function toggleSearch() {
  searchOpen.value = !searchOpen.value
}
</script>

<style>
.mobile-drawer-enter-active,
.mobile-drawer-leave-active {
  transition: opacity 0.3s ease;
}

.mobile-drawer-enter-active .drawer-panel,
.mobile-drawer-leave-active .drawer-panel {
  transition: transform 0.3s ease;
}

.mobile-drawer-enter-from,
.mobile-drawer-leave-to {
  opacity: 0;
}

.mobile-drawer-enter-from .drawer-panel,
.mobile-drawer-leave-to .drawer-panel {
  transform: translateX(100%);
}
</style>