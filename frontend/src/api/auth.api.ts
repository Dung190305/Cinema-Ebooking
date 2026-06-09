import apiClient from './axios'
import type {
    LoginRequest, LoginResponse, RegisterRequest, RefreshTokenRequest,
    SendOtpResponse, VerifyOtpResponse, ResetPasswordRequest,
} from '@/types/auth.types'

export const authApi = {
    login: (payload: LoginRequest) =>
        apiClient.post<LoginResponse>('/auth/login', payload),

    register: (payload: RegisterRequest) =>
        apiClient.post<SendOtpResponse>('/auth/register', payload),

    verifyOtp: (code: string, userId: number) =>
        apiClient.post<VerifyOtpResponse>('/auth/verify-otp', { code, userId }),

    resendOtp: (userId: number) =>
        apiClient.post<SendOtpResponse>(`/auth/resend-otp?userId=${userId}`),

    forgotPassword: (email: string) =>
        apiClient.post<SendOtpResponse>(`/auth/forgot_password?email=${encodeURIComponent(email)}`),

    verifyForgotPasswordOtp: (email: string, code: string) =>
        apiClient.post<VerifyOtpResponse>('/auth/verify-forgot-otp', { email, code }),

    resetPassword: (payload: ResetPasswordRequest) =>
        apiClient.post<VerifyOtpResponse>('/auth/reset_password', payload),

    refreshToken: (payload: RefreshTokenRequest) =>
        apiClient.post<LoginResponse>('/auth/refresh_token', payload),

    logout: (refreshToken: string) =>
        apiClient.post('/auth/logout', { refreshToken }),
}

