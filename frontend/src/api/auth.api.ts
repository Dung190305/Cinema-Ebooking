
import apiClient from './axios'
import type {
    LoginRequest, LoginResponse, RegisterRequest, RefreshTokenRequest,
    LoyaltyAccountSummaryResponse, LoyaltyAccountResponse } from '@/types/auth.types'

export const authApi = {
    login: (payload: LoginRequest) =>
        apiClient.post<LoginResponse>('/auth/login', payload),

    register: (payload: RegisterRequest) =>
        apiClient.post<LoginResponse>('/auth/register', payload),

    refreshToken: (payload: RefreshTokenRequest) =>
        apiClient.post<LoginResponse>('/auth/refresh_token', payload)
}

export const loyaltyApi = {
    // Lấy thông tin tóm tắt (điểm, hạng) của user hiện tại
    getMySummary: () =>
        apiClient.get<LoyaltyAccountSummaryResponse>('/loyalty/my-account/summary'),

    // Lấy toàn bộ thông tin loyalty account (nếu cần)
    getMyAccount: () =>
        apiClient.get<LoyaltyAccountResponse>('/loyalty/my-account')
};