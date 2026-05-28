export type PaymentMethod = 'MOMO' | 'VNPAY' | 'ZALOPAY';
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'EXPIRED' | 'CANCELED';

export interface CreatePaymentRequest {
  bookingId: number;
  method: PaymentMethod;
  callbackUrl: string;
}

export interface CreatePaymentResponse {
  paymentCode: string;
    paymentUrl?: string; 
  expiredAt: string;
}

export interface PaymentCompleteResponse {
  status: PaymentStatus;   // "SUCCESS" | "FAILED" | "EXPIRED" ...
  bookingId?: number;
  showtimeId?: number;
  message?: string;
  transactionId?: string;
}