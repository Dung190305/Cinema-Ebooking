import apiClient from './axios';
import type { CreatePaymentRequest, CreatePaymentResponse } from '@/types/payment.types';

export const paymentApi = {
  create(body: CreatePaymentRequest) {
    return apiClient.post<CreatePaymentResponse>('/payments', body);
  },
  complete(paymentCode: string) {
    return apiClient.post(`/payments/${paymentCode}/complete`);
  },
  cancel(paymentCode: string) {
    return apiClient.post(`/payments/${paymentCode}/cancel`);
  },
};