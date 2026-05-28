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