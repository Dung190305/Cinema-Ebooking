
import apiClient from '@/api/axios'
import type {
  AcquireSeatLockRequest,
  AcquireLockResponse,
  ReleaseSeatLockRequest,
} from '@/types/seatlock'

export const seatLockApi = {
  async acquireLocks(data: AcquireSeatLockRequest): Promise<AcquireLockResponse> {
    const response = await apiClient.post('/seat-locks/acquire', data)
    return response
  },

  async releaseLocks(data: ReleaseSeatLockRequest): Promise<void> {
    await apiClient.post('/seat-locks/release', data)
  },

  async checkLock(seatId: number, currentUserId: number) {
    const response = await apiClient.get(`/seat-locks/check/${seatId}`, {
      params: { currentUserId },
    })
    return response
  },
}