import apiClient from './axios'
import type {
    LoyaltyAccountSummaryResponse, LoyaltyAccountResponse } from '@/types/auth.types'

export const loyaltyApi = {
    // Lấy thông tin tóm tắt (điểm, hạng) của user hiện tại
    getMySummary: () =>
        apiClient.get<LoyaltyAccountSummaryResponse>('/loyalty/my-account/summary'),

    // Lấy toàn bộ thông tin loyalty account (nếu cần)
    getMyAccount: () =>
        apiClient.get<LoyaltyAccountResponse>('/loyalty/my-account')
};