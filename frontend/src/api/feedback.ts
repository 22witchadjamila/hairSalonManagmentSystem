import { api } from './client'
import type { Feedback } from './types'

export interface FeedbackRequest {
  appointmentId: string
  customerId: string
  rating: number
  comment?: string | null
}

export interface PublicReview {
  reviewId: string
  rating: number
  comment: string
  submittedAt: string
  reviewerName: string
}

export const feedbackApi = {
  list: () => api.get<Feedback[]>('/feedback'),
  /** Public: verified reviews, reviewer's first name + initial only. */
  publicReviews: () => api.get<PublicReview[]>('/feedback/public'),
  byCustomer: (customerId: string) => api.get<Feedback[]>(`/feedback/customer/${customerId}`),
  create: (payload: FeedbackRequest) => api.post<Feedback>('/feedback', payload),
  delete: (id: string) => api.delete<void>(`/feedback/${id}`),
}
