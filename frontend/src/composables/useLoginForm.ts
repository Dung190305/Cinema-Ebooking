import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth.api'
import { loyaltyApi } from '@/api/loyalty.api'
import { userApi } from '@/api/user.api'

export function useLoginForm(emit: (event: string) => void) {
    const auth = useAuthStore()
    const router = useRouter()

    const email = ref('')
    const password = ref('')
    const showPassword = ref(false)
    const loading = ref(false)
    const generalError = ref('')
    const fieldErrors = ref({ email: '', password: '' })

    // ─── Validate toàn bộ khi submit ──────────────────────────────
    const validateAll = (): boolean => {
        fieldErrors.value = { email: '', password: '' }
        if (!email.value.trim()) fieldErrors.value.email = 'Vui lòng nhập email'
        if (!password.value.trim()) fieldErrors.value.password = 'Vui lòng nhập mật khẩu'
        return !Object.values(fieldErrors.value).some(Boolean)
    }

    // ─── Clear lỗi khi user gõ ────────────────────────────────────
    const handleInput = (field: keyof typeof fieldErrors.value) => {
        fieldErrors.value[field] = ''
        generalError.value = ''
    }

    // ─── Map lỗi từ backend — đồng nhất với useRegisterForm ───────
    const handleBackendError = (err: any) => {
        fieldErrors.value = { email: '', password: '' }
        generalError.value = ''

        if (err.fieldErrors) {
            for (const [field, message] of Object.entries(err.fieldErrors)) {
                if (field in fieldErrors.value) {
                    fieldErrors.value[field as keyof typeof fieldErrors.value] = message as string
                }
            }
        }

        // Chỉ hiện globalError nếu không có field nào được map
        const hasFieldError = Object.values(fieldErrors.value).some(Boolean)
        if (!hasFieldError) {
            generalError.value =
                err.globalErrors?.[0] ??
                err.message ??
                'Đăng nhập thất bại, vui lòng thử lại'
        }
    }

    // ─── Submit ───────────────────────────────────────────────────
    const handleLogin = async () => {
        if (!validateAll()) return

        loading.value = true
        generalError.value = ''
        fieldErrors.value = { email: '', password: '' }

        try {
            const loginData = await authApi.login({
                email: email.value,
                password: password.value,
            })

            const userProfile = await userApi.getMe(loginData.accessToken)

            auth.setAuth(userProfile, loginData.accessToken, loginData.refreshToken)

            try {
                const loyaltySummary = await loyaltyApi.getMySummary();
                auth.setLoyaltyAccount(loyaltySummary.data);
            } catch (loyaltyErr) {
                // Loyalty có thể không bắt buộc, chỉ log lỗi
                console.warn('Không thể lấy thông tin loyalty:', loyaltyErr);
                auth.setLoyaltyAccount(null);
            }

            router.push(
                loginData.role === 'ADMIN'
                    ? '/admin/analystics/dashboard'
                    : '/'
            )
            emit('close')
        } catch (err: any) {
            handleBackendError(err)
        } finally {
            loading.value = false
        }
    }

    // ─── isDisabled — export để template dùng ─────────────────────
    // FIX: trước đây export canSubmit nhưng LoginForm.vue dùng isDisabled → undefined
    const isDisabled = computed(() =>
        loading.value ||
        !email.value.trim() ||
        !password.value.trim() ||
        Object.values(fieldErrors.value).some(Boolean)
    )

    const submit = () => {
        if (isDisabled.value) return
        handleLogin()
    }

    return {
        email, password, showPassword,
        loading, generalError, fieldErrors,
        isDisabled,           // ← FIX: export đúng tên mà LoginForm.vue cần
        handleInput, submit,
    }
}