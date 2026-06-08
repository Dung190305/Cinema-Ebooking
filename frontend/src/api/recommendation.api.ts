import apiClient from './axios'
import type { RecommendationMovieResponse } from '@/types/recommendation.types'

export const recommendationApi = {
  getMyRecommendations: (limit = 10) =>
    apiClient.get<RecommendationMovieResponse[]>('/recommendations/me', {
      params: { limit },
    }),
}
