import { api } from './client'
import type { DiscountType, Promotion } from './types'

export interface PromotionRequest {
  code: string
  description?: string | null
  discountType: DiscountType
  discountValue: number
  startDate: string
  endDate: string
  usageLimit: number
}

/** Computed on the server from payment and booking rows - see PromotionOfferService. */
export interface PromotionStats {
  promotionId: string
  code: string
  /** PAID payments that used the code: "times used". */
  redeemed: number
  /** Bookings with the code that haven't been paid yet. They count against the limit for new bookings. */
  heldByBookings: number
  refunded: number
  usageLimit: number
  remaining: number
  status: 'SCHEDULED' | 'ACTIVE' | 'FULLY_USED' | 'EXPIRED' | 'ENDED'
}

export interface PromotionOffer {
  code: string
  description: string | null
  discountType: DiscountType
  discountValue: number
  validUntil: string
}

export interface PromotionQuote {
  valid: boolean
  message: string
  code: string | null
  price: number
  discount: number
  finalPrice: number
}

export const promotionsApi = {
  list: () => api.get<Promotion[]>('/promotions'),
  create: (payload: PromotionRequest) => api.post<Promotion>('/promotions', payload),
  delete: (id: string) => api.delete<void>(`/promotions/${id}`),
  update: (id: string, payload: PromotionRequest) => api.put<Promotion>(`/promotions/${id}`, payload),
  stats: () => api.get<PromotionStats[]>('/promotions/stats'),
  end: (id: string) => api.post<Promotion>(`/promotions/${id}/end`),
  reopen: (id: string) => api.post<Promotion>(`/promotions/${id}/reopen`),
  /** Public: promotions a customer can use today. */
  available: () => api.get<PromotionOffer[]>('/promotions/available'),
  /** Public: server-calculated price for a service with a code. */
  quote: (code: string, serviceId: string) =>
    api.get<PromotionQuote>(`/promotions/quote?code=${encodeURIComponent(code)}&serviceId=${encodeURIComponent(serviceId)}`),
}
