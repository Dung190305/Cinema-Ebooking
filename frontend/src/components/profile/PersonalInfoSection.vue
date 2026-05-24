<!-- components/profile/PersonalInfoSection.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue'
import ProfileField from '@/components/profile/ProfileField.vue';
import CalendarPicker from '@/components/ui/calendar/CalendarPicker.vue';
import BaseButton from '@/components/ui/button/BaseButton.vue'
import { dateToISOString, parseISODate } from '@/utils/dateFormat'
import { useCloudinaryImage } from '@/composables/useCloudinaryImage'
import { useAvatarUpload } from '@/composables/useAvatarUpload'
import type { UserProfile, UpdateUserRequest } from '@/types/auth.types'
import LoadingOverlay from '@/components/ui/LoadingOverlay.vue';

const props = defineProps<{
    profile: UserProfile | null
    editMode: boolean;
    formData: UpdateUserRequest;
    saving: boolean;
    error: string | null;
    fieldErrors: Record<string, string>;
    hasChanges: boolean;
}>();

const emit = defineEmits<{
    (e: 'edit'): void;
    (e: 'cancel'): void;
    (e: 'save'): void;
    (e: 'update:formData', data: UpdateUserRequest): void;
    (e: 'avatarUpdated', profile: UserProfile): void
}>();

const avatarLoadingOverlay = ref(false)

// Giá trị hiển thị khi không edit
const displayDateOfBirth = computed(() => {
    if (!props.profile?.dateOfBirth) return ''
    const d = parseISODate(props.profile.dateOfBirth)
    if (!d) return props.profile.dateOfBirth
    const dd = String(d.getDate()).padStart(2, '0')
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const yyyy = d.getFullYear()
    return `${dd}/${mm}/${yyyy}`
})

const { getTransformedUrl } = useCloudinaryImage()
const { uploading, error: uploadError, openWidget } = useAvatarUpload({
    cloudName: import.meta.env.VITE_CLOUDINARY_CLOUD_NAME,
    uploadPreset: import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET,
    folder: 'avatars',
})

const avatarUrl = computed(() => {
    return getTransformedUrl(props.profile?.avatarUrl, { width: 200, height: 200, crop: 'fill', gravity: 'face' })
})

const handleAvatarChange = async () => {
    avatarLoadingOverlay.value = true
    try {
        const updatedProfile = await openWidget(() => {

            avatarLoadingOverlay.value = false
        })
        if (updatedProfile) emit('avatarUpdated', updatedProfile)
    } catch (err) {
        console.error('Avatar upload error:', err)
    } finally {

        avatarLoadingOverlay.value = false
    }
}
</script>

<template>
    <LoadingOverlay :visible="avatarLoadingOverlay" />

    <section v-if="profile" class="bg-bg-surface rounded-2xl p-5 sm:p-6 shadow-sm border border-border-subtle">
        <!-- Header với avatar và tên -->
        <div class="flex items-start gap-4 mb-5">
            <!-- Avatar -->
            <div class="relative group shrink-0">
                <div class="w-20 h-20 rounded-full overflow-hidden bg-bg-base border border-border-subtle">
                    <img v-if="avatarUrl" :src="avatarUrl" alt="Avatar" class="w-full h-full object-cover" />
                    <div v-else
                        class="w-full h-full flex items-center justify-center text-text-tertiary text-body font-semibold">
                        {{ profile.fullName?.charAt(0) || 'U' }}
                    </div>
                </div>
                <div
                    class="absolute inset-0 rounded-full bg-overlay-dark-50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <BaseButton variant="ghost" size="sm" iconOnly customClass="!text-white !bg-transparent"
                        :disabled="uploading" @click.stop="handleAvatarChange">
                        <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24"
                            stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                    </BaseButton>
                </div>
            </div>
            <p v-if="uploadError" class="mt-1 text-caption text-red-500 text-center">
                {{ uploadError }}
            </p>

            <!-- Tên và nút chỉnh sửa -->
            <div class="flex-1 flex items-center justify-between">
                <div>
                    <h2 class="text-title text-text-primary">{{ profile.fullName }}</h2>
                    <p class="text-caption text-text-tertiary">{{ profile.email }}</p>
                </div>
                <div v-if="!editMode">
                    <BaseButton variant="ghost" size="sm"
                        customClass="!text-accent bg-gradient-to-r from-accent to-accent bg-no-repeat bg-left-bottom bg-[length:0%_2px] hover:bg-[length:100%_2px] transition-all duration-300 pb-1"
                        @click="emit('edit')">
                        Chỉnh sửa
                    </BaseButton>
                </div>
                <div v-else class="flex gap-2">
                    <BaseButton variant="secondary" size="sm" @click="emit('cancel')">Huỷ</BaseButton>
                    <BaseButton variant="primary" size="sm" :disabled="saving || !hasChanges" @click="emit('save')">
                        {{ saving ? 'Đang lưu...' : 'Lưu' }}
                    </BaseButton>
                </div>
            </div>
        </div>

        <!-- Form chỉnh sửa (các trường) -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <ProfileField label="Họ tên" :value="profile.fullName" :edit="editMode" :valueEdit="formData.fullName ?? ''"
                :fieldError="fieldErrors?.fullName"
                @update:valueEdit="emit('update:formData', { ...formData, fullName: $event })" />
            <!-- Email readonly -->
            <ProfileField label="Email" :value="profile.email" readonly />
            <ProfileField label="Số điện thoại" :value="profile.phoneNumber" :edit="editMode"
                :valueEdit="formData.phoneNumber ?? ''" :fieldError="fieldErrors?.phoneNumber"
                @update:valueEdit="emit('update:formData', { ...formData, phoneNumber: $event })" />
            <!-- Ngày sinh -->
            <div>
                <label class="block text-caption text-text-tertiary mb-1">Ngày sinh</label>
                <template v-if="editMode">
                    <CalendarPicker :modelValue="parseISODate(formData.dateOfBirth ?? '')"
                        @update:modelValue="(val) => emit('update:formData', { ...formData, dateOfBirth: val ? dateToISOString(val) : '' })"
                        :hasError="!!fieldErrors?.dateOfBirth" variant="web" mode="date" :maxDate="new Date()" />
                    <p v-if="fieldErrors?.dateOfBirth" class="mt-1 text-caption text-red-500">{{ fieldErrors.dateOfBirth
                    }}</p>
                </template>
                <template v-else>
                    <!-- ✅ -->
                    <p class="text-body text-text-primary py-2">{{ displayDateOfBirth || '—' }}</p>
                </template>
            </div>
            <!-- Giới tính -->
            <ProfileField label="Giới tính" :value="profile.gender === 'MALE' ? 'Nam' : 'Nữ'" :edit="editMode"
                type="select" :valueEdit="formData.gender ?? ''" :fieldError="fieldErrors?.gender"
                @update:valueEdit="emit('update:formData', { ...formData, gender: $event as 'MALE' | 'FEMALE' })"
                :options="[{ label: 'Nam', value: 'MALE' }, { label: 'Nữ', value: 'FEMALE' }]" />
        </div>
    </section>
</template>