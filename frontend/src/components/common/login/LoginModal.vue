<script setup lang="ts">
import { ref } from 'vue'
import { X } from 'lucide-vue-next';
import BaseButton from '@/components/ui/button/BaseButton.vue';
import BaseIcon from '@/components/ui/icon/BaseIcon.vue';
import LoginForm from '@/components/common/login/subcomponents/LoginForm.vue'
import RegisterForm from '@/components/common/login/subcomponents/RegisterForm.vue'
import ForgotPasswordForm from '@/components/common/login/subcomponents/ForgotPasswordForm.vue'

const emit = defineEmits<{ close: [] }>()

const modalState = ref<'login' | 'register' | 'forgot'>('login')
const closeModal = () => emit('close')
</script>

<template>
    <div class="fixed inset-0 bg-black/30 overflow-y-auto z-50 py-8" @click.stop>

        <div
            class="w-[clamp(20rem,30%,30rem)] mx-auto bg-bg-surface rounded-xl shadow-xl border border-border-default animate-fadeIn pb-10">

            <!-- HEADER IMAGE -->
            <div class="relative">
                <img src="/images/login/LoginBanner.png" class="w-full h-50 rounded-t-lg object-cover" />
                <BaseButton variant="ghost" size="sm" rounded="full" iconOnly @click="closeModal"
                    class="absolute right-2 top-2">
                    <BaseIcon :icon="X" :size="16" :scale="1.2" />
                </BaseButton>
            </div>

            <!-- FORM CONTENT -->
            <div class="px-6 pt-0">
                <LoginForm v-if="modalState === 'login'" @switch="modalState = 'register'" @close="closeModal"
                    @forgot="modalState = 'forgot'" />
                <RegisterForm v-else-if="modalState === 'register'" @switch="modalState = 'login'" @close="closeModal" />
                <ForgotPasswordForm v-else-if="modalState === 'forgot'" @switch="modalState = 'login'" @close="closeModal" />
            </div>
        </div>
    </div>
</template>