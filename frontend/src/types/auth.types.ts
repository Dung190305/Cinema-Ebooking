
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

// Loyalty account status
export type LoyaltyAccountStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'CLOSED';

// DTO từ backend LoyaltyAccountResponse
export interface LoyaltyAccountResponse {
    loyaltyAccountId: number;
    loyaltyNumber: string;
    totalSpending: number;     // BigDecimal -> number
    lifetimePoints: number;
    currentPoints: number;
    tierId: number;
    lastActivityDate: string;  // LocalDateTime -> ISO string
    joinedDate: string;
    status: LoyaltyAccountStatus;
}

// DTO từ backend LoyaltyAccountSummaryResponse
export interface LoyaltyAccountSummaryResponse {
    loyaltyAccountId: number;
    currentPoints: number;
    tierName: string;
} 

