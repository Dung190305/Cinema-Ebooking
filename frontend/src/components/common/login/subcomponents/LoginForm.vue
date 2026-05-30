<script setup lang="ts">
import BaseButton from '@/components/ui/button/BaseButton.vue'
import { useLoginForm } from '@/composables/useLoginForm'

const emit = defineEmits<{
    close: []
    switch: []
    forgot: []
}>()

const {
    email, password, showPassword,
    loading, generalError,
    fieldErrors,
    isDisabled,
    handleInput, submit
} = useLoginForm(emit)

</script>

<template>
    <h1 class="text-title text-center my-4 text-text-primary">
        Đăng Nhập Tài Khoản
    </h1>

    <form @submit.prevent="submit" class="flex flex-col gap-6 mb-4">
        <div class="flex flex-col gap-3">

            <!-- EMAIL -->
            <div>
                <label class="text-[12px] text-text-secondary">Email</label>
                <input v-model="email" type="email" placeholder="Nhập Email" @input="handleInput('email')" :class="[
                    'w-full text-body text-text-primary placeholder-text-secondary mt-0.5 px-3 py-2 border rounded-md focus:ring-2 outline-none',
                    fieldErrors.email
                        ? 'border-red-400 focus:ring-red-300'
                        : 'border-border-subtle focus:ring-blue-500'
                ]" />
                <p v-if="fieldErrors.email" class="mt-1 text-[11px] text-red-500">
                    {{ fieldErrors.email }}
                </p>
            </div>

            <!-- PASSWORD -->
            <div>
                <label class="text-[12px] text-text-secondary">Mật khẩu</label>
                <div class="relative mt-0.5">
                    <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="Nhập Mật khẩu"
                        @input="handleInput('password')" :class="[
                            'w-full text-body text-text-primary placeholder-text-secondary px-3 py-2 pr-10 border rounded-md focus:ring-2 outline-none',
                            fieldErrors.password
                                ? 'border-red-400 focus:ring-red-300'
                                : 'border-border-subtle focus:ring-blue-500'
                        ]" />

                    <!-- toggle password -->
                    <button type="button"
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-text-primary transition-colors"
                        @click="showPassword = !showPassword" tabindex="-1">
                        <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none"
                            viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                        </svg>
                        <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24"
                            stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M13.875 18.825A10.05 10.05 0 0112 19c-4.477 0-8.268-2.943-9.542-7a9.97 9.97 0 012.308-3.592M6.938 6.938A9.97 9.97 0 0112 5c4.477 0 8.268 2.943 9.542 7a9.97 9.97 0 01-1.497 2.567M3 3l18 18" />
                        </svg>
                    </button>
                </div>

                <p v-if="fieldErrors.password" class="mt-1 text-[11px] text-red-500">
                    {{ fieldErrors.password }}
                </p>
            </div>
        </div>

        <!-- GLOBAL ERROR -->
        <div class="flex flex-col gap-3">
            <div v-if="generalError"
                class="flex items-start gap-2 px-3 py-2.5 rounded-md border border-border-admin-subtle text-red-500 text-[12px]">

                <span>{{ generalError }}</span>
            </div>

            <BaseButton type="submit" :disabled="isDisabled" variant="primary" size="md" rounded="md"
                class="w-full disabled:opacity-50 disabled:cursor-not-allowed">
                {{ loading ? 'Đang đăng nhập...' : 'ĐĂNG NHẬP' }}
            </BaseButton>
        </div>
    </form>

    <button type="button"
        class="text-caption text-left w-fit cursor-pointer text-text-secondary hover:text-accent mb-4"
        @click="emit('forgot')">
        Quên mật khẩu?
    </button>

    <div class="pt-4 border-t border-border-subtle">
        <h2 class="text-center text-text-primary text-body mb-1">
            Bạn chưa có tài khoản?
        </h2>
        <BaseButton @click="emit('switch')" variant="secondary" size="sm" rounded="lg" class="w-full py-2">
            Đăng Ký
        </BaseButton>
    </div>
</template>