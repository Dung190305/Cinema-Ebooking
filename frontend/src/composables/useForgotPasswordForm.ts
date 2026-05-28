import { ref, computed, onUnmounted } from 'vue'
import { authApi } from '@/api/auth.api'

const OTP_LENGTH = 6
const RESEND_COOLDOWN_SECONDS = 60

type Step = 1 | 2

export function useForgotPasswordForm(emit: (event: 'switch' | 'close') => void) {
  // ─── State ───────────────────────────────────────────────────
  const currentStep = ref<Step>(1)
  const email = ref('')
  const emailError = ref('')
  const expiresAt = ref('')
  const otpSent = ref(false)

  const otpDigits = ref<string[]>(Array(OTP_LENGTH).fill(''))
  const inputRefs = ref<HTMLInputElement[]>([])

  const newPassword = ref('')
  const confirmPassword = ref('')
  const showNewPassword = ref(false)
  const showConfirmPassword = ref(false)
  const passwordError = ref('')
  const confirmPasswordError = ref('')

  const loadingSend = ref(false)
  const loadingSubmit = ref(false)
  const loadingResend = ref(false)
  const generalError = ref('')
  const success = ref(false)
  const justResent = ref(false) // Vừa gửi lại → hiện thông báo xanh lá trong vài giây

  // ─── Hai bộ đếm ngược độc lập ─────────────────────────────────
  let expiryTimer: ReturnType<typeof setInterval> | null = null
  let resendTimer: ReturnType<typeof setInterval> | null = null

  const expiryCooldown = ref(0) // về 0 = mã cũ hết hạn
  const resendCooldown = ref(0) // về 0 = được phép bấm gửi lại mã mới

  // Khởi chạy đếm ngược thời gian sống của OTP (Từ backend trả về)
  const startExpiryCountdown = () => {
    if (expiryTimer) clearInterval(expiryTimer)
    if (!expiresAt.value) return

    const tick = () => {
      const remaining = Math.max(
        0,
        Math.floor((new Date(expiresAt.value).getTime() - Date.now()) / 1000),
      )
      expiryCooldown.value = remaining
      if (remaining <= 0 && expiryTimer) {
        clearInterval(expiryTimer)
      }
    }

    tick()
    expiryTimer = setInterval(tick, 1000)
  }

  // Khởi chạy bộ đếm chống spam gửi liên tục (60 giây cố định ở frontend)
  const startResendCooldown = () => {
    resendCooldown.value = RESEND_COOLDOWN_SECONDS
    justResent.value = true

    // Tự động ẩn dòng thông báo "Mã mới đã được gửi" sau 5 giây để hiển thị lại trạng thái đếm giây
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

  onUnmounted(() => {
    if (expiryTimer) clearInterval(expiryTimer)
    if (resendTimer) clearInterval(resendTimer)
  })

  // ─── Computed Properties ──────────────────────────────────────
  const fullOtpCode = computed(() => otpDigits.value.join(''))

  const isSendDisabled = computed(() => loadingSend.value || !email.value.trim())

  const isExpired = computed(() => expiryCooldown.value === 0)

  // Nút "Tiếp tục" ở Step 1: Kích hoạt khi nhập đủ 6 số VÀ mã cũ CHƯA HẾT HẠN
  const isSubmitDisabled = computed(
    () => loadingSubmit.value || fullOtpCode.value.length !== OTP_LENGTH || isExpired.value,
  )

  // Nút "Gửi lại mã": Chỉ phụ thuộc vào việc đã qua hết thời gian chờ 60 giây hay chưa
  const canResend = computed(() => resendCooldown.value === 0 && !loadingResend.value)

  const countdownLabel = computed(() => {
    const s = expiryCooldown.value
    if (s === 0) return 'Đã hết hạn'
    const m = Math.floor(s / 60)
    const sec = s % 60
    return m > 0 ? `${m}:${sec.toString().padStart(2, '0')}` : `${sec}s`
  })

  const resendCooldownLabel = computed(() => `${resendCooldown.value}s`)

  // ─── Xử lý bóc tách lỗi ──────────────────────────────────────────
  const handleBackendError = (err: any) => {
    generalError.value =
      err.globalErrors?.[0] ??
      err.response?.data?.message ??
      err.message ??
      'Không thể kết nối đến máy chủ, thử lại sau'
  }

  const clearErrors = () => {
    emailError.value = ''
    passwordError.value = ''
    confirmPasswordError.value = ''
    generalError.value = ''
  }

  // ─── Gửi mã OTP lần đầu ───────────────────────────────────────
  const handleSendOtp = async () => {
    clearErrors()

    if (!email.value.trim()) {
      emailError.value = 'Vui lòng nhập email'
      return
    }

    loadingSend.value = true
    try {
      // 1. Hứng toàn bộ phản hồi dưới dạng any để kiểm tra cấu trúc trực tiếp
      const res = (await authApi.forgotPassword(email.value)) as any

      // 2. In ra Console để bạn nhìn rõ cấu trúc (F12 kiểm tra Tab Console nhé)
      console.log('Cấu trúc API ForgotPassword thực tế:', res)

      // 3. Tự động bóc tách thông minh: Tìm trường expiresAt ở bất cứ tầng nào nó xuất hiện
      let finalExpiresAt = ''
      if (res?.data?.data?.expiresAt) {
        finalExpiresAt = res.data.data.expiresAt // Trường hợp bọc 2 tầng data
      } else if (res?.data?.expiresAt) {
        finalExpiresAt = res.data.expiresAt // Trường hợp bọc 1 tầng data chuẩn Axios
      } else if (res?.expiresAt) {
        finalExpiresAt = res.expiresAt // Trường hợp Interceptor đã unwrap thẳng
      }

      if (finalExpiresAt) {
        expiresAt.value = finalExpiresAt
        otpSent.value = true
        startExpiryCountdown()
        otpDigits.value = Array(OTP_LENGTH).fill('')
        setTimeout(() => {
          inputRefs.value[0]?.focus()
        }, 50)
      } else {
        emailError.value =
          'Hệ thống không tìm thấy thời gian hết hạn (expiresAt) từ máy chủ trả về!'
      }
    } catch (err: any) {
      emailError.value =
        err.response?.data?.message ?? err.message ?? 'Không thể gửi mã OTP, vui lòng thử lại'
    }
    bits: {
      loadingSend.value = false
    }
  }

  // ─── Gửi lại mã OTP (Resend) ──────────────────────────────────
  const handleResend = async () => {
    if (!canResend.value || loadingResend.value) return

    loadingResend.value = true
    generalError.value = ''

    try {
      const res = (await authApi.forgotPassword(email.value)) as any

      let finalExpiresAt = ''
      if (res?.data?.data?.expiresAt) {
        finalExpiresAt = res.data.data.expiresAt
      } else if (res?.data?.expiresAt) {
        finalExpiresAt = res.data.expiresAt
      } else if (res?.expiresAt) {
        finalExpiresAt = res.expiresAt
      }

      if (finalExpiresAt) {
        expiresAt.value = finalExpiresAt
        otpSent.value = true
        startExpiryCountdown()
        startResendCooldown()
        otpDigits.value = Array(OTP_LENGTH).fill('')
        inputRefs.value[0]?.focus()
      } else {
        generalError.value = 'Không tìm thấy thời gian hết hạn mới từ máy chủ!'
      }
    } catch (err: any) {
      handleBackendError(err)
    } finally {
      loadingResend.value = false
    }
  }

  // ─── Ô nhập liệu mã số helpers ─────────────────────────────────
  const setInputRef = (el: any, index: number) => {
    if (el) {
      // Di chuyển từ khóa 'as' vào đây, TypeScript sẽ hiểu 100%
      inputRefs.value[index] = el as HTMLInputElement
    }
  }

  const handleOtpInput = (index: number, event: Event) => {
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

  const handleOtpKeydown = (index: number, event: KeyboardEvent) => {
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

  const handleOtpPaste = (event: ClipboardEvent) => {
    // Sửa lỗi cú pháp event.clipData -> event.clipboardData chuẩn API W3C
    const pasted = event.clipboardData?.getData('text').replace(/\D/g, '').slice(0, OTP_LENGTH)
    if (pasted) {
      for (let i = 0; i < OTP_LENGTH; i++) {
        otpDigits.value[i] = pasted[i] ?? ''
      }
      inputRefs.value[Math.min(pasted.length, OTP_LENGTH) - 1]?.focus()
    }
  }

  // ─── Chuyển sang bước 2 ────────────────────────────────────────
  const goToResetPassword = () => {
    if (fullOtpCode.value.length !== OTP_LENGTH) {
      generalError.value = 'Vui lòng nhập đủ 6 số OTP'
      return
    }
    if (isExpired.value) {
      generalError.value = 'Mã OTP này đã quá hạn sử dụng, vui lòng bấm lấy mã mới'
      return
    }
    generalError.value = ''
    currentStep.value = 2
  }

  // ─── Tiến hành đặt lại mật khẩu mới tại Bước 2 ────────────────────
  const handleResetPassword = async () => {
    clearErrors()

    if (fullOtpCode.value.length !== OTP_LENGTH) {
      generalError.value = 'Vui lòng điền mã OTP hợp lệ từ bước trước'
      return
    }
    if (!newPassword.value) {
      passwordError.value = 'Vui lòng nhập mật khẩu mới'
      return
    }
    if (newPassword.value.length < 6) {
      passwordError.value = 'Mật khẩu phải có ít nhất 6 ký tự'
      return
    }
    if (!confirmPassword.value) {
      confirmPasswordError.value = 'Vui lòng nhập lại mật khẩu'
      return
    }
    if (newPassword.value !== confirmPassword.value) {
      confirmPasswordError.value = 'Mật khẩu xác nhận không trùng khớp'
      return
    }

    loadingSubmit.value = true
    generalError.value = ''

    try {
      // Thực hiện tuần tự xác thực OTP rồi update password mới qua API backend
      await authApi.verifyForgotPasswordOtp(email.value, fullOtpCode.value)
      await authApi.resetPassword({
        email: email.value,
        otp: fullOtpCode.value,
        newPassword: newPassword.value,
      })
      success.value = true
    } catch (err: any) {
      handleBackendError(err)
    }
    bits: {
      loadingSubmit.value = false
    }
  }

  const goToLogin = () => emit('switch')

  return {
    // Refs chuyển đổi Reactive dữ liệu
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
    expiryCooldown,
    resendCooldown,
    countdownLabel,
    resendCooldownLabel,
    isExpired,
    canResend,
    isSendDisabled,
    isSubmitDisabled,
    justResent,
    inputRefs,
    setInputRef,
    handleSendOtp,
    handleOtpInput,
    handleOtpKeydown,
    handleOtpPaste,
    handleResend,
    goToResetPassword,
    handleResetPassword,
    goToLogin,
  }
}
