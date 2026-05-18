import axios, { AxiosError } from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import type { LoginResponse } from '@/types/auth.types'
import type { ApiResponse, ApiError } from '@/types/common.types'
import { mapFieldErrors } from '@/utils/errorMapper'


const BASE_URL = 'http://localhost:8080/api/v1'

export const apiClient = axios.create({
    baseURL: BASE_URL,
    timeout: 10000,
    headers: { 'Content-Type': 'application/json' },
})

const PUBLIC_ENDPOINTS = [
  '/auth/login',
  '/auth/register',
  '/auth/forgot_password',
  '/auth/reset_password',
  '/auth/refresh_token' 
]

const PROTECTED_GET_PATHS = [
  '/users/me',
  '/loyalty/my-account',  
]

const isPublicEndpoint = (url?: string): boolean => {
  if (!url) return false
  return PUBLIC_ENDPOINTS.some(publicPath => url.includes(publicPath))
}

const isPublicGetRequest = (config: InternalAxiosRequestConfig): boolean => {
  if (config.method?.toUpperCase() !== 'GET') return false
  if (PROTECTED_GET_PATHS.some(path => config.url?.includes(path))) return false  
  return true
}

// ================= REQUEST =================
apiClient.interceptors.request.use((config) => {
    if (isPublicEndpoint(config.url) || isPublicGetRequest(config)) {
        delete config.headers.Authorization;
        return config 
    } 
    const token = localStorage.getItem('accessToken')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

// ================= RESPONSE =================


let isRefreshing = false
let failedQueue: Array<{
    resolve: (value?: any) => void
    reject: (reason?: any) => void
    config: InternalAxiosRequestConfig
}> = []

const processQueue = (error, token) => {
    failedQueue.forEach(({ resolve, reject }) => {
        error ? reject(error) : resolve(token)  // resolve với token
    })
}

const logout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    window.dispatchEvent(new CustomEvent('auth:logout'))  // giữ để store cleanup nếu cần

    if (!window.location.pathname.includes('/')) {
        window.location.href = '/'
    }
}

apiClient.interceptors.response.use(
    (response) => {
        const res: ApiResponse<any> = response.data
        if (res && typeof res.success === 'boolean') {
            if (!res.success) {
                const apiError: ApiError | undefined = res.error                       // ← khai báo đúng chỗ
                const mapped = mapFieldErrors(apiError ?? null)
                return Promise.reject({
                    type: 'api',
                    fieldErrors: mapped.fieldErrors,
                    globalErrors: mapped.globalErrors,
                    code: apiError?.code ?? null,
                    message: apiError?.message ?? 'Lỗi từ server',
                    raw: apiError,
                })
            }
            return res.data
        }
        return response.data
    },

    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }
        const status = error.response?.status
        const apiError: ApiError | undefined = (error.response?.data as any)?.error
        const url = originalRequest.url

        if (status !== 401) {
            const mapped = mapFieldErrors(apiError ?? null)
            return Promise.reject({
                type: 'http',
                fieldErrors: mapped.fieldErrors,
                globalErrors: mapped.globalErrors,
                code: apiError?.code ?? null,
                message: apiError?.message ?? 'Đã có lỗi xảy ra',
                status,
                raw: apiError,
            })
        }

        if (isPublicEndpoint(url)) {
            const mapped = mapFieldErrors(apiError ?? null)
            return Promise.reject({
                type: 'api',
                fieldErrors: mapped.fieldErrors,
                globalErrors: mapped.globalErrors,
                code: apiError?.code ?? null,
                message: apiError?.message ?? 'Đã có lỗi xảy ra',
                status,
                raw: apiError,
            })
        }

        if (isPublicGetRequest(originalRequest)) {
            const mapped = mapFieldErrors(apiError ?? null)
            return Promise.reject({
                type: 'api',
                fieldErrors: mapped.fieldErrors,
                globalErrors: mapped.globalErrors,
                code: apiError?.code ?? null,
                message: apiError?.message ?? 'Đã có lỗi xảy ra',
                status,
                raw: apiError,
            })
        }

        if (originalRequest._retry) {
            logout()
            return Promise.reject(error)
        }

        if (url?.includes('/auth/refresh_token')) {
            logout()
            return Promise.reject(error)
        }

        originalRequest._retry = true

        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject, config: originalRequest })
            })
                .then(() => apiClient(originalRequest))
                .catch(err => Promise.reject(err))
        }

        isRefreshing = true

        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) {
            isRefreshing = false  // ✅ Reset trước khi logout
            logout()
            return Promise.reject(error)
        }

        try {

            const { data } = await axios.post<ApiResponse<LoginResponse>>(
                `${BASE_URL}/auth/refresh_token`,
                { refreshToken },
                { headers: { 'Content-Type': 'application/json' } }
            )

            if (!data.success || !data.data?.accessToken || !data.data?.refreshToken) {
                throw new Error('Invalid refresh response')
            }

            const { accessToken: newAccessToken, refreshToken: newRefreshToken } = data.data

            localStorage.setItem('accessToken', newAccessToken)
            localStorage.setItem('refreshToken', newRefreshToken)

            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`

            try {
                const { useAuthStore } = await import('@/stores/auth.store')
                await useAuthStore().fetchMe()
            } catch {
            }

            processQueue(null, newAccessToken)
            return apiClient(originalRequest)
        } catch (refreshError) {
            processQueue(refreshError)
            logout()
            return Promise.reject(refreshError)
        } finally {
            isRefreshing = false
        }
    }
)

export default apiClient