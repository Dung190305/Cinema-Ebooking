<script setup lang="ts">
import { onBeforeUpdate } from 'vue'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import { useOtpForm } from '@/composables/useOtpForm'

const props = defineProps<{
  userId: number
  email: string
  expiresAt: string
  onSuccess?: () => void
}>()

const emit = defineEmits<{
  (e: 'switch'): void
  (e: 'close'): void
  (e: 'success'): void
}>()

// Nhớ truyền thêm tham số emit vào composable theo cập nhật TypeScript trước đó
const {
  otpDigits,
  loading,
  loadingResend,
  generalError,
  success,
  successMessage,
  isDisabled,
  isExpired,
  canResend,
  countdownLabel,
  resendCooldown,
  justResent,
  inputRefs,
  handleInput,
  handleKeydown,
  handlePaste,
  submit,
  resend,
} = useOtpForm(props.userId, props.email, props.expiresAt, emit as any)

// Tối ưu hóa việc dọn dẹp Refs trước khi DOM cập nhật
onBeforeUpdate(() => {
  inputRefs.value = []
})

// Định nghĩa hàm gán ref hợp lệ lỗi 'as HTMLInputElement' ở template
const setInputRef = (el: any, index: number) => {
  if (el) {
    inputRefs.value[index] = el as HTMLInputElement
  }
}

const handleSuccess = () => {
  props.onSuccess?.()
  emit('success')
  emit('switch')
}

// Chặn các ký tự không phải số (e.g. e, E, +, -, .) khi người dùng cố tình gõ trên Desktop
const filterNumber = (event: KeyboardEvent) => {
  if (['e', 'E', '+', '-', '.'].includes(event.key)) {
    event.preventDefault()
  }
}
</script>

<template>
  <div
    class="w-full max-w-md mx-auto bg-white dark:bg-zinc-900 rounded-2xl shadow-xl border border-zinc-100 dark:border-zinc-800/80 p-8 transition-all duration-300"
  >
    <div v-if="success" class="flex flex-col items-center text-center py-4 animate-fade-in">
      <div class="relative mb-6">
        <div
          class="absolute inset-0 rounded-full bg-green-100 dark:bg-green-950/40 animate-ping opacity-75"
        ></div>
        <div
          class="relative w-20 h-20 rounded-full bg-gradient-to-tr from-green-500 to-emerald-400 flex items-center justify-center shadow-lg shadow-green-500/20"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-10 h-10 text-white animate-scale-up"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="3"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
          </svg>
        </div>
      </div>

      <h2 class="text-2xl font-bold text-zinc-900 dark:text-zinc-50 tracking-tight mb-2">
        Xác minh thành công!
      </h2>
      <p class="text-sm text-zinc-500 dark:text-zinc-400 max-w-xs leading-relaxed mb-1">
        Tài khoản của bạn đã được kích hoạt và sẵn sàng sử dụng.
      </p>
      <p
        v-if="successMessage"
        class="text-xs bg-green-50 dark:bg-green-950/30 text-green-600 dark:text-green-400 font-medium px-3 py-1.5 rounded-lg border border-green-100 dark:border-green-900/30 mb-6"
      >
        {{ successMessage }}
      </p>

      <BaseButton
        variant="primary"
        class="w-full py-3 text-sm font-semibold shadow-lg shadow-accent/20 transition-all duration-200 hover:-translate-y-0.5"
        @click="handleSuccess"
      >
        Đăng nhập ngay
      </BaseButton>
    </div>

    <div v-else class="animate-fade-in">
      <div class="text-center mb-6">
        <div class="flex justify-center mb-4">
          <div
            class="w-16 h-16 rounded-2xl bg-accent/10 flex items-center justify-center border border-accent/20 text-accent transition-transform hover:rotate-6 duration-300"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="w-8 h-8"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"
              />
            </svg>
          </div>
        </div>

        <h1 class="text-2xl font-bold text-zinc-900 dark:text-zinc-50 tracking-tight mb-2">
          Nhập mã xác minh
        </h1>
        <p class="text-sm text-zinc-500 dark:text-zinc-400 leading-relaxed">
          Mã bảo mật gồm 6 chữ số đã được gửi đến hòm thư <br />
          <span
            class="text-accent font-semibold underline decoration-accent/30 underline-offset-4"
            >{{ email }}</span
          >
        </p>
      </div>

      <div
        :class="['flex justify-center gap-2 mb-4', generalError ? 'animate-shake' : '']"
        @paste.prevent="handlePaste"
      >
        <input
          v-for="(digit, index) in otpDigits"
          :key="index"
          :ref="(el) => setInputRef(el, index)"
          v-model="otpDigits[index]"
          type="text"
          inputmode="numeric"
          maxlength="1"
          pattern="[0-9]*"
          autocomplete="one-time-code"
          @keydown="filterNumber"
          @input="handleInput(index, $event)"
          @keydown.delete="handleKeydown(index, $event)"
          @keyup.enter="index === otpDigits.length - 1 ? submit() : null"
          :class="[
            'w-12 h-14 text-center text-2xl font-bold border rounded-xl outline-none transition-all duration-150 transform select-none',
            digit
              ? 'border-accent bg-accent/5 dark:bg-accent/10 text-accent ring-4 ring-accent/10 scale-105 font-extrabold'
              : 'border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-800/40 text-zinc-800 dark:text-zinc-200 focus:border-accent focus:ring-4 focus:ring-accent/10',
            generalError
              ? 'border-red-500 dark:border-red-500/80 bg-red-50/30 dark:bg-red-950/20 text-red-600 focus:ring-red-500/10'
              : '',
          ]"
        />
      </div>

      <div
        v-if="generalError"
        class="text-center text-xs font-medium text-red-500 dark:text-red-400 mb-4 animate-fade-in"
      >
        ⚠️ {{ generalError }}
      </div>

      <div class="flex items-center justify-center gap-2 mb-6">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-4 h-4 text-zinc-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <span
          class="text-xs font-medium tracking-wide transition-colors duration-200"
          :class="isExpired ? 'text-red-500 font-semibold' : 'text-zinc-500 dark:text-zinc-400'"
        >
          {{ isExpired ? 'Mã số OTP đã hết hạn sử dụng' : `Mã hết hạn sau: ${countdownLabel}` }}
        </span>
      </div>

      <BaseButton
        :disabled="isDisabled || loading"
        variant="primary"
        class="w-full py-3 text-sm font-semibold shadow-md disabled:opacity-40 disabled:cursor-not-allowed mb-5 flex items-center justify-center gap-2"
        @click="submit"
      >
        <svg
          v-if="loading"
          class="animate-spin h-4 w-4 text-white"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            class="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            stroke-width="4"
          ></circle>
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
        <span>{{ loading ? 'Đang tiến hành xác minh...' : 'Xác minh tài khoản' }}</span>
      </BaseButton>

      <div class="text-center text-sm text-zinc-500 dark:text-zinc-400 min-h-[24px] mb-2">
        <template v-if="justResent">
          <span
            class="inline-flex items-center gap-1 text-green-600 dark:text-green-400 font-medium animate-bounce"
          >
            ✨ Mã mới đã được gửi thành công!
          </span>
        </template>
        <template v-else>
          Bạn chưa nhận được mật mã?
          <button
            v-if="canResend"
            type="button"
            :disabled="loadingResend"
            class="text-accent hover:text-accent-hover font-semibold ml-1 transition-all focus:outline-none hover:underline underline-offset-2"
            @click="resend"
          >
            {{ loadingResend ? 'Đang gửi...' : 'Gửi lại mã mới' }}
          </button>
          <span
            v-else
            class="text-zinc-400 dark:text-zinc-600 ml-1 font-semibold bg-zinc-100 dark:bg-zinc-800 px-2 py-0.5 rounded-md text-xs"
          >
            Gửi lại sau {{ resendCooldown }}s
          </span>
        </template>
      </div>

      <div class="pt-5 mt-5 border-t border-zinc-100 dark:border-zinc-800/60">
        <div
          class="flex items-center justify-center gap-3 text-xs text-zinc-400 dark:text-zinc-500 font-medium"
        >
          <button
            type="button"
            class="hover:text-accent dark:hover:text-accent transition-colors duration-200"
            @click="emit('switch')"
          >
            Quay lại đăng ký
          </button>
          <span class="text-zinc-200 dark:text-zinc-700">|</span>
          <button
            type="button"
            class="hover:text-zinc-600 dark:hover:text-zinc-300 transition-colors duration-200"
            @click="emit('close')"
          >
            Đóng cửa sổ
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ─── Tích hợp sẵn Animation mượt mà bằng CSS ─── */
@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  20%,
  60% {
    transform: translateX(-4px);
  }
  40%,
  80% {
    transform: translateX(4px);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleUp {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.animate-shake {
  animation: shake 0.4s ease-in-out;
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}
</style>
