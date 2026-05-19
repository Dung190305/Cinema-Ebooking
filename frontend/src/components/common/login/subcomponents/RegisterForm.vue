<script setup lang="ts">
import BaseButton from '@/components/ui/button/BaseButton.vue'
import CalendarPicker from '@/components/ui/calendar/CalenderPicker.vue'
import { useRegisterForm } from '@/composables/useRegisterForm'

const emit = defineEmits<{
    switch: []
    close: []
}>()

const {
    form, errors,
    showPassword, showConfirmPassword,
    loading, generalError,
    isDisabled,
    handleInput, selectGender, handleDateSelect, submit,
    success, successMessage,
} = useRegisterForm(emit)
</script>

<template>
    <h1 class="text-title text-center my-4 text-text-primary">Đăng Kí Tài Khoản</h1>
    <!-- SUCCESS STATE -->
    <div v-if="success" class="flex flex-col gap-4 text-center pt-6">

        <div class="text-green-500 text-body font-medium">
            {{ successMessage }}
        </div>

        <BaseButton variant="primary" class="w-full" @click="emit('switch')">
            Quay lại đăng nhập
        </BaseButton>

    </div>
    <div v-if="!success">
        <form @submit.prevent="submit" class="flex flex-col gap-2 mb-4">
            <!-- HỌ VÀ TÊN -->
            <div>
                <label class="text-[12px] text-text-secondary">Họ và tên</label>
                <input v-model="form.fullName" type="text" placeholder="Nhập họ và tên""
                    @input=" handleInput('fullName')" :class="[
                        'w-full text-body text-text-primary placeholder-text-secondary mt-0.5 px-3 py-2 border rounded-md focus:ring-2 outline-none',
                        errors.fullName
                            ? 'border-red-400 focus:ring-red-300'
                            : 'border-border-subtle focus:ring-blue-500'
                    ]" />
                <p v-if="errors.fullName" class="mt-1 text-[11px] text-red-500">{{ errors.fullName }}</p>
            </div>

            <!-- EMAIL -->
            <div>
                <label class="text-[12px] text-text-secondary">Email</label>
                <input v-model="form.email" type="text" placeholder="Nhập email" @input="handleInput('email')" :class="[
                    'w-full text-body text-text-primary placeholder-text-secondary mt-0.5 px-3 py-2 border rounded-md focus:ring-2 outline-none',
                    errors.email
                        ? 'border-red-400 focus:ring-red-300'
                        : 'border-border-subtle focus:ring-blue-500'
                ]" />
                <p v-if="errors.email" class="mt-1 text-[11px] text-red-500">{{ errors.email }}</p>
            </div>

            <!-- SỐ ĐIỆN THOẠI -->
            <div>
                <label class="text-[12px] text-text-secondary">Số điện thoại</label>
                <input v-model="form.phoneNumber" type="tel" placeholder="Nhập số điện thoại"
                    @input="handleInput('phoneNumber')" :class="[
                        'w-full text-body text-text-primary placeholder-text-secondary mt-0.5 px-3 py-2 border rounded-md focus:ring-2 outline-none',
                        errors.phoneNumber
                            ? 'border-red-400 focus:ring-red-300'
                            : 'border-border-subtle focus:ring-blue-500'
                    ]" />
                <p v-if="errors.phoneNumber" class="mt-1 text-[11px] text-red-500">{{ errors.phoneNumber }}</p>
            </div>

            <!-- GIỚI TÍNH -->
            <div>
                <label class="text-[12px] text-text-secondary">Giới tính</label>
                <div class="flex gap-3 mt-1.5">
                    <button v-for="option in [{ value: 'male', label: 'Nam' }, { value: 'female', label: 'Nữ' }]"
                        :key="option.value" type="button" @click="selectGender(option.value as 'male' | 'female')"
                        :class="[
                            'flex items-center gap-2 px-4 py-2 rounded-md border text-body transition-all',
                            form.gender === option.value
                                ? 'border-accent text-text-primary'
                                : errors.gender
                                    ? 'border-red-400 text-text-secondary'
                                    : 'border-border-subtle text-text-secondary hover:border-border-default'
                        ]">
                        <span :class="[
                            'w-3.5 h-3.5 rounded-full border-2 flex items-center justify-center shrink-0',
                            form.gender === option.value ? 'border-accent' : 'border-text-secondary'
                        ]">
                            <span v-if="form.gender === option.value" class="w-1.5 h-1.5 rounded-full bg-accent" />
                        </span>
                        {{ option.label }}
                    </button>
                </div>
                <p v-if="errors.gender" class="mt-1 text-[11px] text-red-500">{{ errors.gender }}</p>
            </div>

            <!-- NGÀY SINH -->
            <div>
                <label class="text-[12px] text-text-secondary">Ngày sinh</label>
                <div class="mt-0.5">
                    <CalendarPicker v-model="form.dateOfBirth" @update:model-value="handleDateSelect"
                        :hasError="!!errors.dateOfBirth" :maxDate="new Date()" />
                </div>
                <p v-if="errors.dateOfBirth" class="mt-1 text-[11px] text-red-500">{{ errors.dateOfBirth }}</p>
            </div>

            <!-- MẬT KHẨU -->
            <div>
                <label class="text-[12px] text-text-secondary">Mật khẩu</label>
                <div class="relative mt-0.5">
                    <input v-model="form.password" :type="showPassword ? 'text' : 'password'"
                        placeholder="Nhập mật khẩu (ít nhất 6 ký tự)" @input="handleInput('password')" :class="[
                            'w-full text-body text-text-primary placeholder-text-secondary px-3 py-2 pr-10 border rounded-md focus:ring-2 outline-none',
                            errors.password
                                ? 'border-red-400 focus:ring-red-300'
                                : 'border-border-subtle focus:ring-blue-500'
                        ]" />
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
                <p v-if="errors.password" class="mt-1 text-[11px] text-red-500">{{ errors.password }}</p>
            </div>

            <!-- NHẬP LẠI MẬT KHẨU -->
            <div>
                <label class="text-[12px] text-text-secondary">Nhập lại mật khẩu</label>
                <div class="relative mt-0.5">
                    <input v-model="form.confirmPassword" :type="showConfirmPassword ? 'text' : 'password'"
                        placeholder="Nhập lại mật khẩu" @input="handleInput('confirmPassword')" :class="[
                            'w-full text-body text-text-primary placeholder-text-secondary px-3 py-2 pr-10 border rounded-md focus:ring-2 outline-none',
                            errors.confirmPassword
                                ? 'border-red-400 focus:ring-red-300'
                                : 'border-border-subtle focus:ring-blue-500'
                        ]" />
                    <button type="button"
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-text-primary transition-colors"
                        @click="showConfirmPassword = !showConfirmPassword" tabindex="-1">
                        <svg v-if="!showConfirmPassword" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none"
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
                <p v-if="errors.confirmPassword" class="mt-1 text-[11px] text-red-500">{{ errors.confirmPassword }}</p>
            </div>

            <!-- ĐỒNG Ý ĐIỀU KHOẢN -->
            <div>
                <label class="flex items-start gap-2.5">
                    <div class="relative shrink-0 mt-0.5 group">
                        <input type="checkbox" v-model="form.agreeTerms" class="sr-only" />
                        <div :class="[
                            'w-4 h-4 rounded border-2 flex items-center justify-center transition-all cursor-pointer',
                            form.agreeTerms
                                ? 'bg-accent border-accent'
                                : 'border-border-subtle group-hover:bg-overlay-dark-10'
                        ]">
                            <svg v-if="form.agreeTerms" xmlns="http://www.w3.org/2000/svg"
                                class="w-2.5 h-2.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"
                                stroke-width="3.5">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                            </svg>
                        </div>
                    </div>
                    <span class="text-[12px] text-text-secondary leading-relaxed">
                        Tôi đồng ý với
                        <span class="text-blue-500 hover:text-blue-600 cursor-pointer">Điều khoản sử dụng</span>
                        và
                        <span class="text-blue-500 hover:text-blue-600 cursor-pointer">Chính sách bảo mật</span>
                    </span>
                </label>
            </div>

            <!-- GENERAL ERROR + BUTTON HOÀN THÀNH -->
            <div class="flex flex-col gap-3">
                <div v-if="generalError"
                    class="flex items-start gap-2 px-3 py-2.5 rounded-md border border-border-admin-subtle text-red-500 text-[12px]">
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 mt-0.5 shrink-0" fill="none"
                        viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
                    </svg>
                    <span>{{ generalError }}</span>
                </div>
                <BaseButton type="submit" :disabled="isDisabled" variant="primary" size="md" rounded="md"
                    class="w-full disabled:opacity-50 disabled:pointer-events-none">
                    {{ loading ? 'Đang xử lý...' : 'HOÀN THÀNH' }}
                </BaseButton>
            </div>
        </form>

        <!-- SWITCH TO LOGIN -->
        <div class="pt-4 border-t border-border-subtle">
            <h2 class="text-center text-text-primary text-body mb-1">Bạn đã có tài khoản?</h2>
            <BaseButton @click="emit('switch')" variant="secondary" size="sm" rounded="lg" class="w-full py-2">
                Đăng Nhập
            </BaseButton>
        </div>
    </div>
</template>