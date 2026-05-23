import { User } from 'lucide-vue-next';

export type UserRole = 'ADMIN' | 'USER'
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED'
export type UserGender = 'MALE' | 'FEMALE';

export interface LoginResponse {
    accessToken: string   // thay vì token
    refreshToken: string  // thêm mới
    role: UserRole
}
export interface RefreshTokenRequest {
    refreshToken: string
}

export interface LoginRequest {
    email: string
    password: string
}

export interface RegisterRequest {
    fullName: string
    email: string
    password: string
    phoneNumber: string
    dateOfBirth: string
    gender: UserGender
}
export interface UserProfile {
    id: number
    fullName: string
    email: string
    phoneNumber: string
    dateOfBirth: string;      // LocalDate -> ISO string
    gender: UserGender;
    avatarUrl: string
    role: UserRole
    status: UserStatus
}

export interface UpdateUserRequest {
    fullName?: string
    phoneNumber?: string
    dateOfBirth?: string  // ISO date
    gender?: UserGender
}

export interface UpdateAvatarRequest {
    avatarUrl: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}



