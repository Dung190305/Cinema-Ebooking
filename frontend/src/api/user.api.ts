
import apiClient from './axios'
import type { UserProfile, UpdateUserRequest, UpdateAvatarRequest, ChangePasswordRequest } from '@/types/auth.types'

export const userApi = {
    getMe: (token?: string) =>
        apiClient.get<UserProfile>('/users/me', {
            headers: token
                ? { Authorization: `Bearer ${token}` }
                : undefined, 
        }),
    
    updateMe: (data: UpdateUserRequest) =>
        apiClient.put<UserProfile>('/users/me', data),

    updateAvatar: (data: UpdateAvatarRequest) =>
        apiClient.patch<UserProfile>('/users/me/avatar', data),

    changePassword: (data: ChangePasswordRequest) =>
        apiClient.put<void>('/users/me/change-password', data),
}