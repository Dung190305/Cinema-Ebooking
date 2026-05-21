<!-- components/profile/ProfilePage.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { useProfile } from '@/composables/useProfile'
import { useLoyaltyTiers } from '@/composables/useLoyaltyTiers'
import { useChangePassword } from '@/composables/useChangePassword'
import PersonalInfoSection from '@/components/profile/PersonalInfoSection.vue'
import LoyaltySection from '@/components/profile/LoyaltySection.vue'
import SecuritySection from '@/components/profile/SecuritySection.vue'
import type { UserProfile } from '@/types/auth.types'
import Skeleton from '@/components/ui/skeleton/Skeleton.vue'
import { personalInfoSkeletonBlocks, loyaltySkeletonBlocks } from '@/skeletons/profile.skeleton'
import { useAuthStore } from '@/stores/auth.store'

const activeTab = ref<'info' | 'loyalty' | 'security'>('info')
const auth = useAuthStore()

const {
    profile, loading: profileLoading, error: profileError,
    editMode, formData, saving, hasChanges,
    startEdit, cancelEdit, saveProfile,
    fieldErrors,
} = useProfile()

const {
    loading: loyaltyLoading,
    error: loyaltyError,
    currentTierName,
    discountPercent,
    lifetimePoints,
    currentPoints,
    totalSpending,
    nextTierName,
    nextTierPointsRequired,
    pointsToNext,
    progressPercent,
    isHighestTier,
} = useLoyaltyTiers()

const {
    oldPassword, newPassword, confirmPassword,
    loading: changingPwd, message: pwdMessage,
    changePassword,
} = useChangePassword()

const handleAvatarUpdated = (updatedProfile: UserProfile) => {
    auth.updateUserProfile(updatedProfile)
    if (profile.value) {
        profile.value = updatedProfile
    }
}

const handleSaveProfile = async () => {
    await saveProfile()
    auth.refreshUserProfile()
}

const tabs = [
    { id: 'info' as const, label: 'Thông tin cá nhân' },
    { id: 'loyalty' as const, label: 'Thành viên' },
    { id: 'security' as const, label: 'Bảo mật' },
]
</script>

<template>
    <div v-if="profileLoading || !profile" class="min-h-screen bg-bg-base flex items-center justify-center">
        <p class="text-body text-text-secondary">Đang tải...</p>
    </div>
    <div v-else class="min-h-screen bg-bg-base">
        <div class="max-w-4xl mx-auto px-4 py-6 sm:px-6 lg:px-8">
            <!-- Tab Navigation -->
            <div class="flex border-b border-border-subtle mb-6">
                <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id" :class="[
                    'px-4 py-3 text-body font-medium transition-colors relative',
                    activeTab === tab.id
                        ? 'text-accent'
                        : 'text-text-secondary hover:text-text-primary'
                ]">
                    {{ tab.label }}
                    <span v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-accent" />
                </button>
            </div>

            <!-- Nội dung tab -->
            <div v-if="activeTab === 'info'">
                <Skeleton v-if="profileLoading" :blocks="personalInfoSkeletonBlocks" />
                <PersonalInfoSection v-else :profile="profile" :editMode="editMode" :formData="formData"
                    :saving="saving" :hasChanges="hasChanges" :error="profileError" :fieldErrors="fieldErrors"
                    @edit="startEdit" @cancel="cancelEdit" @save="handleSaveProfile"
                    @update:formData="Object.assign(formData, $event)" @avatarUpdated="handleAvatarUpdated" />
            </div>

            <div v-if="activeTab === 'loyalty'">
                <Skeleton v-if="loyaltyLoading" :blocks="loyaltySkeletonBlocks" />
                <LoyaltySection v-else :error="loyaltyError" :currentTierName="currentTierName"
                    :discountPercent="discountPercent" :currentPoints="currentPoints" :lifetimePoints="lifetimePoints"
                    :totalSpending="totalSpending" :nextTierName="nextTierName"
                    :nextTierPointsRequired="nextTierPointsRequired" :pointsToNext="pointsToNext"
                    :progressPercent="progressPercent" :isHighestTier="isHighestTier" :loading="loyaltyLoading" />
            </div>


            <SecuritySection v-if="activeTab === 'security'" v-model:oldPassword="oldPassword"
                v-model:newPassword="newPassword" v-model:confirmPassword="confirmPassword" :loading="changingPwd"
                :message="pwdMessage" @changePassword="changePassword" />
        </div>
    </div>
</template>