import type { AgeRating, MovieStatus } from './movie'
import type { GenreResponse } from './genre'

export interface RecommendationMovieResponse {
  id: number
  title: string
  description: string
  duration: number
  ageRating: AgeRating
  releaseDate: string
  showingEndDate?: string
  status: MovieStatus
  posterUrl?: string
  bannerUrl?: string
  director?: string
  actors?: string
  genres: GenreResponse[]
  rating: number
  ratingCount: number
  recommendationScore: number
  recommendationSource: string
}
