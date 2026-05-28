// composables/useProfile.ts
import { ref, reactive, onMounted, computed } from 'vue'
import { userApi } from '@/api/user.api'
import type { UserProfile, UpdateUserRequest } from '@/types/auth.types'

interface RejectedError {
    fieldErrors?: Record<string, string>
    globalErrors?: string[]
    message?: string
}

export function useProfile() {
    const profile = ref<UserProfile | null>(null)
    const loading = ref(false)
    const error = ref<string | null>(null)             // Lỗi chung (global)
    const fieldErrors = ref<Record<string, string>>({}) // Lỗi từng trường

    const editMode = ref(false)
    const formData = reactive<UpdateUserRequest>({})
    const saving = ref(false)

    const originalFormData = ref<UpdateUserRequest>({})

    const hasChanges = computed(() => {
        return JSON.stringify(formData) !== JSON.stringify(originalFormData.value)
    })

    const fetchProfile = async () => {
        loading.value = true
        error.value = null
        fieldErrors.value = {}
        try {
            const res = await userApi.getMe()
            profile.value = res
            Object.assign(formData, {
                fullName: res.fullName,
                phoneNumber: res.phoneNumber,
                dateOfBirth: res.dateOfBirth,
                gender: res.gender,
        })
        } catch (err: unknown) {
            const e = err as RejectedError
            fieldErrors.value = e.fieldErrors ?? {}
            if (e.globalErrors?.length) {
                error.value = e.globalErrors.join('. ')
            } else if (e.message) {
                error.value = e.message
            } else {
                error.value = 'Không thể tải thông tin người dùng.'
            }
        } finally {
            loading.value = false
        }
    }

    const startEdit = () => {
        editMode.value = true
        if (profile.value) {
            const freshData = {
                fullName: profile.value.fullName,
                phoneNumber: profile.value.phoneNumber,
                dateOfBirth: profile.value.dateOfBirth,
                gender: profile.value.gender,
            }
            Object.assign(formData, freshData)
            // Ghi nhớ bản gốc
            originalFormData.value = { ...freshData }
        }
        fieldErrors.value = {}
        error.value = null
    }

    const cancelEdit = () => {
        editMode.value = false
        fieldErrors.value = {}
        error.value = null
    }

     const validateForm = (): boolean => {
        const errors: Record<string, string> = {}

        // Kiểm tra họ tên
        if (!formData.fullName || formData.fullName.trim() === '') {
            errors.fullName = 'Họ tên không được để trống'
        }

        // Kiểm tra số điện thoại
        if (!formData.phoneNumber || formData.phoneNumber.trim() === '') {
            errors.phoneNumber = 'Số điện thoại không được để trống'
        } else {
            // Tuỳ chọn: kiểm tra định dạng số điện thoại (ví dụ 10-11 số)
            const phoneRegex = /^[0-9]{10,11}$/
            if (!phoneRegex.test(formData.phoneNumber.replace(/\s/g, ''))) {
                errors.phoneNumber = 'Số điện thoại không hợp lệ (10-11 chữ số)'
            }
        }

        // Kiểm tra ngày sinh
        if (!formData.dateOfBirth) {
            errors.dateOfBirth = 'Ngày sinh không được để trống'
        }

        // Kiểm tra giới tính
        if (!formData.gender) {
            errors.gender = 'Giới tính không được để trống'
        }

        fieldErrors.value = errors
        if (Object.keys(errors).length > 0) {
            error.value = 'Vui lòng điền đầy đủ thông tin bắt buộc.'
            return false
        }
        return true
    }

    const saveProfile = async () => {
        if (!validateForm()) {
            return
        }

        if (!hasChanges.value) {
            editMode.value = false
            return
        }

        saving.value = true
        error.value = null
        fieldErrors.value = {}

        try {
            const res = await userApi.updateMe({ ...formData })
            profile.value = res
            editMode.value = false
        } catch (err: unknown) {
            const e = err as RejectedError
            fieldErrors.value = e.fieldErrors ?? {}
            if (e.globalErrors?.length) {
                error.value = e.globalErrors.join('. ')
            } else if (e.message) {
                error.value = e.message
            } else {
                error.value = 'Cập nhật thất bại.'
            }
        } finally {
        saving.value = false
        }
    }

    onMounted(fetchProfile)

    return {
        profile,
        loading,
        error,
        fieldErrors,
        editMode,
        formData,
        saving,
        hasChanges,
        startEdit,
        cancelEdit,
        saveProfile,
    }
}