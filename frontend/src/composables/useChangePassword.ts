// composables/useChangePassword.ts
import { ref } from 'vue'
import { userApi } from '@/api/user.api'

interface RejectedError {
    fieldErrors?: Record<string, string>
    globalErrors?: string[]
    message?: string
}

const messageType = ref<'success' | 'error' | null>(null)

export function useChangePassword() {
    const oldPassword = ref('')
    const newPassword = ref('')
    const confirmPassword = ref('') 
    const loading = ref(false)
    const message = ref<string | null>(null)
    const fieldErrors = ref<Record<string, string>>({})

    const changePassword = async () => {
        // Reset trạng thái
        message.value = null
        fieldErrors.value = {}

        // Kiểm tra rỗng
        if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
            message.value = 'Vui lòng nhập đầy đủ tất cả các trường.'
            messageType.value = 'error'
            return
        }

        // Kiểm tra mật khẩu mới và xác nhận có khớp
        if (newPassword.value !== confirmPassword.value) {
            message.value = 'Mật khẩu mới và xác nhận không khớp.'
            messageType.value = 'error'
            return
        }

        loading.value = true
        try {
            await userApi.changePassword({
                oldPassword: oldPassword.value,
                newPassword: newPassword.value,
            })
            message.value = 'Đổi mật khẩu thành công!'
            messageType.value = 'success'
            // Reset tất cả các trường
            oldPassword.value = ''
            newPassword.value = ''
            confirmPassword.value = ''
        } catch (err: unknown) {
            const e = err as RejectedError
            messageType.value = 'error'
            fieldErrors.value = e.fieldErrors ?? {}
            if (e.globalErrors?.length) {
                message.value = e.globalErrors.join('. ')
            } else if (e.message) {
                message.value = e.message
            } else {
                message.value = 'Đổi mật khẩu thất bại.'
            }
        } finally {
            loading.value = false
        }
    }

    return {
        oldPassword,
        newPassword,
        confirmPassword,
        loading,
        message,
        messageType,
        fieldErrors,
        changePassword,
    }
}