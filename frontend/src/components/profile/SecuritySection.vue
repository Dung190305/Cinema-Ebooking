<script setup lang="ts">
import { ref, computed } from 'vue';
import BaseButton from '@/components/ui/button/BaseButton.vue';

const props = defineProps<{
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
    loading: boolean;
    message: string | null;
    messageType?: 'success' | 'error'
}>();

const emit = defineEmits<{
    (e: 'update:oldPassword', value: string): void;
    (e: 'update:newPassword', value: string): void;
    (e: 'update:confirmPassword', value: string): void;
    (e: 'changePassword'): void;
}>();

const showForm = ref(false);

// Kiểm tra mật khẩu xác nhận có khớp không (chỉ hiển thị lỗi khi đã nhập ít nhất 1 ký tự)
const passwordMismatch = computed(() => {
    return props.confirmPassword.length > 0 && props.newPassword !== props.confirmPassword;
});

const isFormValid = computed(() => {
    return (
        props.oldPassword.trim() !== '' &&
        props.newPassword.trim() !== '' &&
        props.confirmPassword.trim() !== '' &&
        !passwordMismatch.value
    );
});
</script>

<template>
    <section class="bg-bg-surface rounded-2xl p-5 sm:p-6 shadow-sm border border-border-subtle">
        <div class="flex items-center justify-between">
            <h2 class="text-title text-text-primary">Bảo mật</h2>
            <BaseButton variant="ghost" size="sm"
                customClass="!text-accent bg-gradient-to-r from-accent to-accent bg-no-repeat bg-left-bottom bg-[length:0%_2px] hover:bg-[length:100%_2px] transition-all duration-300 pb-1"
                @click="showForm = !showForm">
                {{ showForm ? 'Ẩn' : 'Đổi mật khẩu' }}
            </BaseButton>
        </div>
        <div v-if="showForm" class="space-y-4 max-w-md mt-4">
            <div>
                <label class="block text-caption text-text-tertiary mb-1">Mật khẩu hiện tại</label>
                <input type="password" :value="oldPassword"
                    @input="emit('update:oldPassword', ($event.target as HTMLInputElement).value)"
                    class="w-full rounded-lg border border-border-default bg-bg-base px-3 py-2 text-body text-text-primary focus:outline-none focus:ring-2 focus:ring-accent" />
            </div>
            <div>
                <label class="block text-caption text-text-tertiary mb-1">Mật khẩu mới</label>
                <input type="password" :value="newPassword"
                    @input="emit('update:newPassword', ($event.target as HTMLInputElement).value)"
                    class="w-full rounded-lg border border-border-default bg-bg-base px-3 py-2 text-body text-text-primary focus:outline-none focus:ring-2 focus:ring-accent" />
            </div>
            <!-- Ô xác nhận mật khẩu mới -->
            <div>
                <label class="block text-caption text-text-tertiary mb-1">Xác nhận mật khẩu mới</label>
                <input type="password" :value="confirmPassword"
                    @input="emit('update:confirmPassword', ($event.target as HTMLInputElement).value)" :class="[
                        'w-full rounded-lg border bg-bg-base px-3 py-2 text-body text-text-primary focus:outline-none focus:ring-2 focus:ring-accent',
                        passwordMismatch ? 'border-red-500' : 'border-border-default'
                    ]" />
                <p v-if="passwordMismatch" class="text-caption text-red-500 mt-1">
                    Mật khẩu xác nhận không khớp
                </p>
            </div>

            <p v-if="message" :class="['text-caption', messageType === 'success' ? 'text-green-600' : 'text-red-500']">
                {{ message }}
            </p>

            <BaseButton @click="emit('changePassword')" :disabled="loading || !isFormValid" variant="primary" size="md"
                rounded="md">
                {{ loading ? 'Đang xử lý...' : 'Xác nhận đổi' }}
            </BaseButton>
        </div>
    </section>
</template>