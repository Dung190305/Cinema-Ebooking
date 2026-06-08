// src/types/movie.types.ts
import type { GenreResponse } from './genre'

export type AgeRating = 'P' | 'T13' | 'T16' | 'T18'
export type MovieStatus = 'COMING_SOON' | 'NOW_SHOWING' | 'ENDED'

export interface MovieResponse {
  id: number
  title: string
  description: string
  duration: number
  ageRating: AgeRating
  releaseDate: string // ISO date
  showingEndDate: string | null
  status: MovieStatus
  posterUrl: string
  bannerUrl: string
  trailerUrl: string
  director: string
  actors: string
  genres: GenreResponse[]
  rating: number | null
  ratingCount: number
}

export interface CreateMovieRequest {
  title: string
  description: string
  duration: number
  ageRating: AgeRating
  releaseDate: string
  showingEndDate: string | null
  posterUrl: string
  bannerUrl: string
  trailerUrl: string
  director: string
  actors: string
  genreIds: number[]
}

export interface UpdateMovieRequest {
  title: string
  description: string
  duration: number
  ageRating: AgeRating
  releaseDate: string
  showingEndDate: string | null
  posterUrl: string
  bannerUrl: string
  trailerUrl: string
  director: string
  actors: string
  genreIds: number[]
}
