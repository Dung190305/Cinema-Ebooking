<script setup lang="ts">
import { onBeforeUpdate } from 'vue'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import { useForgotPasswordForm } from '@/composables/useForgotPasswordForm'

const emit = defineEmits<{
  switch: []
  close: []
}>()

const {
  currentStep,
  email,
  emailError,
  otpSent,
  otpDigits,
  newPassword,
  confirmPassword,
  showNewPassword,
  showConfirmPassword,
  passwordError,
  confirmPasswordError,
  loadingSend,
  loadingSubmit,
  loadingResend,
  generalError,
  success,
  countdownLabel,
  resendCooldown,
  isExpired,
  canResend,
  isSendDisabled,
  isSubmitDisabled,
  justResent,
  setInputRef,
  handleSendOtp,
  handleOtpInput,
  handleOtpKeydown,
  handleOtpPaste,
  handleResend,
  goToResetPassword,
  handleResetPassword,
  goToLogin,
  inputRefs, // Đảm bảo composable có trả về mảng ref này để clear
} = useForgotPasswordForm(emit as any)

// Dọn sạch mảng refs trước khi DOM re-render hoặc chuyển Step
onBeforeUpdate(() => {
  if (inputRefs && inputRefs.value) {
    inputRefs.value = []
  }
})

// Chặn các ký tự toán học/khoa học đặc biệt tại ô OTP số
const filterNumber = (event: KeyboardEvent) => {
  if (['e', 'E', '+', '-', '.'].includes(event.key)) {
    event.preventDefault()
  }
}

// Hàm an toàn để lùi bước về Step 1 thay vì gán trực tiếp lên Template
const handleBackToStep1 = () => {
  currentStep.value = 1
}
</script>

<template>
  <div v-if="success" class="flex flex-col gap-4 text-center pt-6">
    <div class="flex justify-center">
      <div class="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-8 h-8 text-green-500"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
        </svg>
      </div>
    </div>

    <div>
      <h2 class="text-title text-text-primary mb-1">Đặt lại mật khẩu thành công!</h2>
      <p class="text-body text-text-secondary">Mật khẩu của bạn đã được thay đổi.</p>
    </div>

    <BaseButton variant="primary" class="w-full mt-2" @click="goToLogin">
      Đăng nhập ngay
    </BaseButton>
  </div>

  <div v-else-if="currentStep === 1">
    <div class="text-center mb-5">
      <div class="flex justify-center mb-3">
        <div class="w-14 h-14 rounded-full bg-accent/10 flex items-center justify-center">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-7 h-7 text-accent"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z"
            />
          </svg>
        </div>
      </div>
      <h1 class="text-title text-text-primary mb-1">Quên mật khẩu?</h1>
      <p class="text-[13px] text-text-secondary">
        Nhập email đã đăng ký, chúng tôi sẽ gửi mã OTP để đặt lại mật khẩu.
      </p>
    </div>

    <div class="mb-3">
      <label class="text-[12px] text-text-secondary font-medium">Email</label>
      <input
        v-model="email"
        type="email"
        placeholder="Nhập email của bạn"
        :disabled="otpSent || loadingSend"
        @input="emailError = ''"
        @keyup.enter="!isSendDisabled && handleSendOtp"
        :class="[
          'w-full text-body text-text-primary placeholder-text-secondary mt-0.5 px-3 py-2 border rounded-md focus:ring-2 outline-none transition-all disabled:bg-bg-secondary/50',
          emailError
            ? 'border-red-400 focus:ring-red-300'
            : 'border-border-subtle focus:ring-blue-500',
        ]"
      />
      <p v-if="emailError" class="mt-1 text-[11px] text-red-500">{{ emailError }}</p>
    </div>

    <div class="mb-4">
      <BaseButton
        :disabled="isSendDisabled || loadingSend"
        variant="primary"
        size="md"
        rounded="md"
        class="w-full disabled:opacity-50 disabled:pointer-events-none"
        @click="handleSendOtp"
      >
        {{ loadingSend ? 'Đang gửi mã...' : otpSent ? 'Gửi lại mã OTP mới' : 'Gửi mã OTP' }}
      </BaseButton>
    </div>

    <div v-if="otpSent" class="mt-4 pt-4 border-t border-border-subtle animate-fade-in">
      <p class="text-[12px] text-text-secondary mb-3">
        Mã OTP đã được gửi đến
        <span class="text-accent font-medium">{{ email }}</span>
      </p>

      <div class="flex justify-center gap-2 mb-3" @paste.prevent="handleOtpPaste">
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
          @input="handleOtpInput(index, $event)"
          @keydown.delete="handleOtpKeydown(index, $event)"
          @keyup.enter="index === otpDigits.length - 1 && !isExpired ? goToResetPassword() : null"
          :class="[
            'w-11 h-13 text-center text-[1.4rem] font-semibold border rounded-lg outline-none transition-all select-none',
            digit
              ? 'border-accent bg-accent/5 text-text-primary'
              : 'border-border-subtle bg-bg-primary text-text-primary',
            generalError ? 'border-red-400 animate-shake' : '',
          ]"
        />
      </div>

      <div class="flex items-center justify-center gap-1.5 mb-3">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-3.5 h-3.5 text-text-secondary"
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
          class="text-[12px]"
          :class="isExpired ? 'text-red-500 font-medium' : 'text-text-secondary'"
        >
          {{ isExpired ? 'Mã đã hết hạn' : `Mã hết hạn sau: ${countdownLabel}` }}
        </span>
      </div>

      <div class="text-center text-[13px] text-text-secondary mb-5 min-h-[20px]">
        <template v-if="justResent">
          <span class="text-green-500 font-medium"
            >Mã mới đã được gửi. Vui lòng kiểm tra email.</span
          >
        </template>
        <template v-else>
          Bạn chưa nhận được mã?
          <button
            v-if="canResend"
            type="button"
            :disabled="loadingResend"
            class="text-accent hover:text-accent-hover font-medium ml-1 disabled:opacity-50"
            @click="handleResend"
          >
            {{ loadingResend ? 'Đang gửi...' : 'Gửi lại mã' }}
          </button>
          <span v-else class="text-text-secondary ml-1 font-medium">
            Gửi lại sau {{ resendCooldown }}s
          </span>
        </template>
      </div>

      <BaseButton
        variant="primary"
        size="md"
        rounded="md"
        class="w-full mb-4 disabled:opacity-50 disabled:pointer-events-none"
        :disabled="otpDigits.join('').length !== 6 || isExpired"
        @click="goToResetPassword"
      >
        Tiếp tục
      </BaseButton>
    </div>

    <div class="text-center mt-2">
      <button
        type="button"
        class="text-[13px] text-text-secondary hover:text-accent transition-colors"
        @click="emit('switch')"
      >
        Quay lại đăng nhập
      </button>
    </div>
  </div>

  <div v-else-if="currentStep === 2">
    <div class="text-center mb-5">
      <div class="flex justify-center mb-3">
        <div class="w-14 h-14 rounded-full bg-accent/10 flex items-center justify-center">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-7 h-7 text-accent"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"
            />
          </svg>
        </div>
      </div>
      <h1 class="text-title text-text-primary mb-1">Đặt lại mật khẩu</h1>
      <p class="text-[13px] text-text-secondary">
        Nhập mật khẩu mới cho tài khoản
        <span class="text-accent font-medium">{{ email }}</span>
      </p>
    </div>

    <div class="mb-4 p-3 rounded-md bg-accent/5 border border-accent/20">
      <p class="text-[12px] text-text-secondary mb-2 font-medium">Mã OTP đã nhập:</p>
      <div class="flex justify-center gap-1">
        <span
          v-for="(digit, i) in otpDigits"
          :key="i"
          class="w-9 h-10 flex items-center justify-center text-[1.2rem] font-semibold text-accent bg-white border border-accent/30 rounded shadow-sm"
        >
          {{ digit }}
        </span>
      </div>
    </div>

    <div class="mb-3">
      <label class="text-[12px] text-text-secondary font-medium">Mật khẩu mới</label>
      <div class="relative mt-0.5">
        <input
          v-model="newPassword"
          :type="showNewPassword ? 'text' : 'password'"
          placeholder="Ít nhất 6 ký tự"
          @input="passwordError = ''"
          :class="[
            'w-full text-body text-text-primary placeholder-text-secondary px-3 py-2 pr-10 border rounded-md focus:ring-2 outline-none transition-all',
            passwordError
              ? 'border-red-400 focus:ring-red-300'
              : 'border-border-subtle focus:ring-blue-500',
          ]"
        />
        <button
          type="button"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-text-primary transition-colors"
          @click="showNewPassword = !showNewPassword"
          tabindex="-1"
        >
          <svg
            v-if="!showNewPassword"
            xmlns="http://www.w3.org/2000/svg"
            class="w-4 h-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            />
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
            />
          </svg>
          <svg
            v-else
            xmlns="http://www.w3.org/2000/svg"
            class="w-4 h-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M13.875 18.825A10.05 10.05 0 0112 19c-4.477 0-8.268-2.943-9.542-7a9.97 9.97 0 012.308-3.592M6.938 6.938A9.97 9.97 0 0112 5c4.477 0 8.268 2.943 9.542 7a9.97 9.97 0 01-1.497 2.567M3 3l18 18"
            />
          </svg>
        </button>
      </div>
      <p v-if="passwordError" class="mt-1 text-[11px] text-red-500">{{ passwordError }}</p>
    </div>

    <div class="mb-4">
      <label class="text-[12px] text-text-secondary font-medium">Nhập lại mật khẩu mới</label>
      <div class="relative mt-0.5">
        <input
          v-model="confirmPassword"
          :type="showConfirmPassword ? 'text' : 'password'"
          placeholder="Nhập lại mật khẩu mới"
          @input="confirmPasswordError = ''"
          :class="[
            'w-full text-body text-text-primary placeholder-text-secondary px-3 py-2 pr-10 border rounded-md focus:ring-2 outline-none transition-all',
            confirmPasswordError
              ? 'border-red-400 focus:ring-red-300'
              : 'border-border-subtle focus:ring-blue-500',
          ]"
        />
        <button
          type="button"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-text-primary transition-colors"
          @click="showConfirmPassword = !showConfirmPassword"
          tabindex="-1"
        >
          <svg
            v-if="!showConfirmPassword"
            xmlns="http://www.w3.org/2000/svg"
            class="w-4 h-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            />
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
            />
          </svg>
          <svg
            v-else
            xmlns="http://www.w3.org/2000/svg"
            class="w-4 h-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M13.875 18.825A10.05 10.05 0 0112 19c-4.477 0-8.268-2.943-9.542-7a9.97 9.97 0 012.308-3.592M6.938 6.938A9.97 9.97 0 0112 5c4.477 0 8.268 2.943 9.542 7a9.97 9.97 0 01-1.497 2.567M3 3l18 18"
            />
          </svg>
        </button>
      </div>
      <p v-if="confirmPasswordError" class="mt-1 text-[11px] text-red-500">
        {{ confirmPasswordError }}
      </p>
    </div>

    <div class="flex flex-col gap-3 mb-4">
      <div
        v-if="generalError"
        class="flex items-start gap-2 px-3 py-2.5 rounded-md border border-red-200 bg-red-50 text-red-600 text-[12px] animate-shake"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-4 h-4 mt-0.5 shrink-0"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"
          />
        </svg>
        <span>{{ generalError }}</span>
      </div>

      <BaseButton
        :disabled="isSubmitDisabled || loadingSubmit"
        variant="primary"
        size="md"
        rounded="md"
        class="w-full disabled:opacity-50 disabled:pointer-events-none"
        @click="handleResetPassword"
      >
        {{ loadingSubmit ? 'Đang đặt lại...' : 'Đặt lại mật khẩu' }}
      </BaseButton>
    </div>

    <div class="text-center">
      <button
        type="button"
        class="text-[13px] text-text-secondary hover:text-accent transition-colors font-medium"
        @click="handleBackToStep1"
      >
        ← Quay lại thay đổi mã
      </button>
    </div>
  </div>
</template>

<style scoped>
@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  20% {
    transform: translateX(-4px);
  }
  40% {
    transform: translateX(4px);
  }
  60% {
    transform: translateX(-4px);
  }
  80% {
    transform: translateX(4px);
  }
}

.animate-shake {
  animation: shake 0.35s ease-in-out;
}

.animate-fade-in {
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
