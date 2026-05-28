<template>
  <footer class="w-full bg-bg-surface border-t border-border-subtle px-6 py-8">
    <div class="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
      <Logo />
      <!-- Bản quyền -->
      <div class="text-text-tertiary text-caption">
        © {{ new Date().getFullYear() }} Cinema E-booking. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import Logo from '@/components/ui/logo/Logo.vue';

</script>