
import apiClient from './axios'
import type {
    LoginRequest, LoginResponse, RegisterRequest, RefreshTokenRequest,
} from '@/types/auth.types'

export const authApi = {
    login: (payload: LoginRequest) =>
        apiClient.post<LoginResponse>('/auth/login', payload),

    register: (payload: RegisterRequest) =>
        apiClient.post<LoginResponse>('/auth/register', payload),

    refreshToken: (payload: RefreshTokenRequest) =>
        apiClient.post<LoginResponse>('/auth/refresh_token', payload)
}

