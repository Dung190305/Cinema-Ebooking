<template>
  <div class="min-h-screen flex flex-col bg-bg-base items-center">
    <Header />
    <main class="w-full">
      <router-view />
    </main>
    <Footer />
  </div>
  <LoginModal v-if="ui.showLoginModal" @close="ui.closeLoginModal" @login="handleLoginSuccess" />
</template>

<script setup lang="ts">
import Footer from '@/components/common/Footer.vue'
import Header from '@/components/common/header/Header.vue';
import LoginModal from '@/components/common/login/LoginModal.vue';

import { useUIStore } from '@/stores/ui.store';
import { ref } from 'vue';


const ui = useUIStore()

const handleLoginSuccess = (user) => {
  console.log('logged in:', user)
  auth.isLoggedIn = true
  auth.user = user
}


</script>
