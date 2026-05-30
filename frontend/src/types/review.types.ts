export type ReviewStatus = 'ACTIVE' | 'HIDDEN'
export type ReviewSentiment = 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE'
export type ReviewDecision = 'APPROVED' | 'REJECTED' | 'SPOILER_WARNING'

export interface ReviewResponse {
  reviewId: number
  userId: number
  userName: string
  movieId: number
  bookingId: number
  rating: number
  comment: string
  finalText: string
  sentiment: ReviewSentiment
  decision: ReviewDecision
  status: ReviewStatus
  isSpoiler: boolean
  spoilerConf: number
  createdAt: string
  editedAt: string | null
  edited: boolean
}

export interface CreateReviewRequest {
  userId: number
  bookingCode: string
  movieId: number
  rating: number
  comment: string
}

export interface UpdateReviewRequest {
  userId: number
  rating: number
  comment: string
}

export interface MyReviewDetail {
  reviewId: number
  userId: number
  userName: string
  movieId: number
  bookingId: number
  rating: number
  comment: string
  finalText: string
}

export interface MyReviewResponse {
  hasReview: boolean
  review: MyReviewDetail | null
}

export interface TicketCheckResponse {
  hasCheckedInTicket: boolean
  checkedInCount: number
  latestBookingCode: string | null
  latestBookingId: number | null
}
