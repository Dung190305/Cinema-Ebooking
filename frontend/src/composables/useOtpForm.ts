import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { authApi } from '@/api/auth.api'

const OTP_LENGTH = 6
const RESEND_COOLDOWN_SECONDS = 60
const OTP_STORAGE_KEY = 'pendingOtpVerification'

// Định nghĩa Interface dữ liệu lưu trữ
export interface StoredOtpData {
  userId: number
  email: string
  expiresAt: string
}

// Các helper quản lý LocalStorage
export function saveOtpData(data: StoredOtpData): void {
  localStorage.setItem(OTP_STORAGE_KEY, JSON.stringify(data))
}

export function loadOtpData(): StoredOtpData | null {
  try {
    const raw = localStorage.getItem(OTP_STORAGE_KEY)
    if (!raw) return null
    const data = JSON.parse(raw) as StoredOtpData
    if (new Date(data.expiresAt).getTime() <= Date.now()) {
      clearOtpData()
      return null
    }
    return data
  } catch {
    return null
  }
}

export function clearOtpData(): void {
  localStorage.removeItem(OTP_STORAGE_KEY)
}

// ─────────────────────────────────────────────────────────────────
// MAIN COMPOSABLE
// ─────────────────────────────────────────────────────────────────
export function useOtpForm(
  userId: number,
  email: string,
  initialExpiresAt: string,
  emit: (event: 'switch' | 'close' | 'success') => void,
) {
  // --- 1. State/Refs Khởi Tạo ---
  const otpDigits = ref<string[]>(Array(OTP_LENGTH).fill(''))
  const loading = ref(false)
  const loadingResend = ref(false)
  const generalError = ref('')
  const success = ref(false)
  const successMessage = ref('')
  const inputRefs = ref<HTMLInputElement[]>([])

  const currentExpiresAt = ref(initialExpiresAt)
  const expiryCooldown = ref(0) // về 0 = đã hết hạn
  const resendCooldown = ref(0) // về 0 = được phép gửi lại
  const justResent = ref(false) // vừa gửi lại → hiện thông báo

  let expiryTimer: ReturnType<typeof setInterval> | null = null
  let resendTimer: ReturnType<typeof setInterval> | null = null

  // --- 2. Các hàm Logic Hệ Thống (Được đưa lên trên Watcher) ---
  
  // Khởi động countdown hết hạn
  const startExpiryCountdown = () => {
    if (expiryTimer) clearInterval(expiryTimer)

    const tick = () => {
      const target = new Date(currentExpiresAt.value).getTime()
      const remaining = Math.max(0, Math.floor((target - Date.now()) / 1000))
      expiryCooldown.value = remaining
      if (remaining <= 0 && expiryTimer) {
        clearInterval(expiryTimer)
      }
    }

    tick()
    expiryTimer = setInterval(tick, 1000)
  }

  // Khởi động countdown chống spam gửi lại (Resend)
  const startResendCooldown = () => {
    resendCooldown.value = RESEND_COOLDOWN_SECONDS
    justResent.value = true

    setTimeout(() => {
      justResent.value = false
    }, 5000)

    if (resendTimer) clearInterval(resendTimer)
    resendTimer = setInterval(() => {
      resendCooldown.value--
      if (resendCooldown.value <= 0) {
        resendCooldown.value = 0
        clearInterval(resendTimer!)
      }
    }, 1000)
  }

  const handleBackendError = (err: any) => {
    generalError.value =
      err.globalErrors?.[0] ??
      err.response?.data?.message ??
      err.message ??
      'Không thể kết nối đến máy chủ, thử lại sau'
  }

  // --- 3. Watchers & Lifecycle Hooks ---
  
  // Theo dõi động thời gian hết hạn (Đã hoạt động an toàn nhờ đưa hàm lên trước)
  watch(
    currentExpiresAt,
    () => {
      startExpiryCountdown()
    },
    { immediate: true },
  )

  onMounted(() => {
    startExpiryCountdown()
    setTimeout(() => {
      inputRefs.value[0]?.focus()
    }, 50)
  }) 

  onUnmounted(() => {
    if (expiryTimer) clearInterval(expiryTimer)
    if (resendTimer) clearInterval(resendTimer)
  })

  // --- 4. Computed Properties ---
  const fullCode = computed(() => otpDigits.value.join(''))
  const isExpired = computed(() => expiryCooldown.value === 0)
  const isDisabled = computed(() => loading.value || fullCode.value.length !== OTP_LENGTH || isExpired.value)
  const canResend = computed(() => resendCooldown.value === 0 && !loadingResend.value)

  const countdownLabel = computed(() => {
    const s = expiryCooldown.value
    if (s === 0) return 'Đã hết hạn'
    const m = Math.floor(s / 60)
    const sec = s % 60
    return m > 0 ? `${m}:${sec.toString().padStart(2, '0')}` : `${sec}s`
  })

  // --- 5. Các Hàm Tương Tác API / Sự Kiện ---
  
  // Xác minh OTP (Submit)
  const submit = async () => {
    if (isDisabled.value) return

    loading.value = true
    generalError.value = ''

    try {
      const { data } = await authApi.verifyOtp(fullCode.value, userId)
      success.value = true
      successMessage.value = data?.message ?? 'Xác minh thành công!'
      clearOtpData()
      emit('success') // Kích hoạt sự kiện báo cho modal/view cha biết để xử lý tiếp
    } catch (err: any) {
      otpDigits.value = Array(OTP_LENGTH).fill('')
      inputRefs.value[0]?.focus()
      handleBackendError(err)
    } finally {
      loading.value = false
    }
  }

  // Gửi lại mã OTP (Resend)
  const resend = async () => {
    if (!canResend.value || loadingResend.value) return

    loadingResend.value = true
    generalError.value = ''

    try {
      const { data } = await authApi.resendOtp(userId)
      if (data && data.expiresAt) {
        currentExpiresAt.value = data.expiresAt
      } else {
        currentExpiresAt.value = new Date(Date.now() + 5 * 60 * 1000).toISOString()
      }

      saveOtpData({ userId, email, expiresAt: currentExpiresAt.value })

      startExpiryCountdown()
      startResendCooldown()

      otpDigits.value = Array(OTP_LENGTH).fill('')
      inputRefs.value[0]?.focus()
    } catch (err: any) {
      handleBackendError(err)
    } finally {
      loadingResend.value = false
    }
  }

  // Xử lý ô Input (Tự nhảy ô khi nhập, xóa lùi khi bấm Backspace, Paste cả chuỗi)
  const handleInput = (index: number, event: Event) => {
    const input = event.target as HTMLInputElement
    const value = input.value.replace(/\D/g, '')

    if (value.length > 1) {
      const chars = value.split('')
      for (let i = 0; i < OTP_LENGTH; i++) {
        otpDigits.value[i] = chars[i] ?? ''
      }
      inputRefs.value[Math.min(chars.length - 1, OTP_LENGTH - 1)]?.focus()
    } else {
      otpDigits.value[index] = value
      if (value && index < OTP_LENGTH - 1) {
        inputRefs.value[index + 1]?.focus()
      }
    }
    generalError.value = ''
  }

  const handleKeydown = (index: number, event: KeyboardEvent) => {
    if (event.key === 'Backspace') {
      if (!otpDigits.value[index] && index > 0) {
        otpDigits.value[index - 1] = ''
        inputRefs.value[index - 1]?.focus()
      } else {
        otpDigits.value[index] = ''
      }
    } else if (event.key === 'ArrowLeft' && index > 0) {
      inputRefs.value[index - 1]?.focus()
    } else if (event.key === 'ArrowRight' && index < OTP_LENGTH - 1) {
      inputRefs.value[index + 1]?.focus()
    }
  }

  const handlePaste = (event: ClipboardEvent) => {
    const pasted = event.clipboardData?.getData('text').replace(/\D/g, '').slice(0, OTP_LENGTH)
    if (pasted) {
      for (let i = 0; i < OTP_LENGTH; i++) {
        otpDigits.value[i] = pasted[i] ?? ''
      }
      inputRefs.value[Math.min(pasted.length, OTP_LENGTH) - 1]?.focus()
    }
  }

  const goToLogin = () => emit('switch')

  return {
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
    expiryCooldown,
    resendCooldown,
    justResent,
    inputRefs,
    handleInput,
    handleKeydown,
    handlePaste,
    submit,
    resend,
    goToLogin,
  }
}