// composables/useMovieReview.ts
import { ref, computed } from 'vue'
import { reviewApi } from '@/api/review.api'
import { bookingApi } from '@/api/booking.api'
import { useAuthStore } from '@/stores/auth.store'
import type { ReviewResponse, CreateReviewRequest } from '@/types/review.types'
import type { BookingDetailResponse } from '@/types/booking.types'

export function useMovieReview(movieId: number) {
  const authStore = useAuthStore()

  const reviews = ref<ReviewResponse[]>([])
  const verifiedBooking = ref<BookingDetailResponse | null>(null)
  const loading = ref(false)
  const submitting = ref(false)
  const submittingError = ref<string | null>(null)
  const currentPage = ref(0)
  const totalPages = ref(0)
  const hasVerifiedTicket = ref(false)

  const averageRating = computed(() => {
    if (!reviews.value.length) return 0
    const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
    return sum / reviews.value.length
  })

  const totalReviews = computed(() => reviews.value.length)

  const isLoggedIn = computed(() => authStore.isLoggedIn)

  const existingReview = computed(() => {
    if (!authStore.user) return null
    return reviews.value.find(r => r.userId === authStore.user!.id) ?? null
  })

  async function fetchReviews(page = 0) {
    loading.value = true
    try {
      const data = await reviewApi.getByMovieId(movieId, { page, size: 20, sort: 'createdAt,desc' })
      reviews.value = data.content
      currentPage.value = data.pageable?.pageNumber ?? 0
      totalPages.value = data.totalPages
    } catch (err: any) {
      console.error('Failed to fetch reviews:', err)
    } finally {
      loading.value = false
    }
  }

  async function checkVerifiedTicket() {
    if (!authStore.user) {
      hasVerifiedTicket.value = false
      verifiedBooking.value = null
      return
    }

    try {
      const res = await bookingApi.getListByUser({
        userId: authStore.user.id,
        status: 'CONFIRMED',
        size: 50,
      })

      console.log('[checkVerifiedTicket] movieId cần tìm:', movieId)
      console.log('[checkVerifiedTicket] Response raw:', JSON.stringify(res))
      console.log('[checkVerifiedTicket] Response type:', typeof res)
      console.log('[checkVerifiedTicket] Response keys:', res && Object.keys(res))

      const content = (res as any).content ?? res
      console.log('[checkVerifiedTicket] content type:', typeof content, Array.isArray(content) ? 'array' : 'not array')
      console.log('[checkVerifiedTicket] Tổng booking CONFIRMED:', Array.isArray(content) ? content.length : 'N/A')
      if (Array.isArray(content)) {
        content.forEach((b: any, i: number) => {
          console.log(`[checkVerifiedTicket] Booking[${i}] id=${b.bookingId} movieId=${b.movieId} status=${b.status} showtime=${b.showtime}`)
        })
      }

      const now = new Date()

      const eligibleBooking = (Array.isArray(content) ? content : []).find((b: any) => {
        if (b.status !== 'CONFIRMED') return false
        const bMovieId = b.movieId ?? (b as any).movie?.id
        console.log(`[checkVerifiedTicket] So sánh: b.movieId=${bMovieId} === movieId=${movieId} → ${bMovieId === movieId}`)
        if (bMovieId !== movieId) return false
        const showtimeDate = new Date(b.showtime)
        console.log(`[checkVerifiedTicket] Showtime: ${showtimeDate.toISOString()} < now: ${now.toISOString()} → ${showtimeDate < now}`)
        return showtimeDate < now
      })

      console.log('[checkVerifiedTicket] eligibleBooking:', eligibleBooking)

      if (eligibleBooking) {
        hasVerifiedTicket.value = true
        verifiedBooking.value = { bookingId: eligibleBooking.bookingId } as BookingDetailResponse
      } else {
        hasVerifiedTicket.value = false
        verifiedBooking.value = null
      }
    } catch (err: any) {
      console.error('[checkVerifiedTicket] LỖI:', err?.status, err?.data, err?.message, err)
      hasVerifiedTicket.value = false
      verifiedBooking.value = null
    }
  }

  async function submitReview(rating: number, comment: string) {
    if (!authStore.user || !verifiedBooking.value) return

    submitting.value = true
    submittingError.value = null
    try {
      if (existingReview.value) {
        await reviewApi.update(existingReview.value.reviewId, {
          userId: authStore.user.id,
          rating,
          comment,
        })
      } else {
        const body: CreateReviewRequest = {
          userId: authStore.user.id,
          bookingId: verifiedBooking.value.bookingId,
          movieId,
          rating,
          comment,
        }
        await reviewApi.create(body)
      }
      await fetchReviews()
    } catch (err: any) {
      submittingError.value = err?.message || 'Không thể gửi đánh giá. Vui lòng thử lại.'
      throw err
    } finally {
      submitting.value = false
    }
  }

  async function deleteReview() {
    if (!authStore.user || !existingReview.value) return
    try {
      await reviewApi.delete(existingReview.value.reviewId, authStore.user.id)
      await fetchReviews()
    } catch (err: any) {
      console.error('Failed to delete review:', err)
    }
  }

  return {
    reviews,
    verifiedBooking,
    loading,
    submitting,
    submittingError,
    currentPage,
    totalPages,
    hasVerifiedTicket,
    averageRating,
    totalReviews,
    isLoggedIn,
    existingReview,
    fetchReviews,
    checkVerifiedTicket,
    submitReview,
    deleteReview,
  }
}
