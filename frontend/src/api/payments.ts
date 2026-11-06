import { api } from './client'
import type { Payment, PaymentMethod } from './types'

/** Totals computed by the server from the payment rows. Only PAID counts as revenue. */
export interface PaymentSummary {
  collected: number
  discountsGiven: number
  paidCount: number
  refunded: number
  refundedCount: number
  pendingCount: number
  failedCount: number
}

export const paymentsApi = {
  list: () => api.get<Payment[]>('/payments'),
  summary: () => api.get<PaymentSummary>('/payments/summary'),
  /**
   * Record an in-store payment. There is no amount field: the server prices it from the booked
   * service and applies any promotion itself, so nothing sent from here can change the price.
   * promoCode: undefined = use the code from the booking, '' = no promotion, 'X' = use code X.
   * redeemPoints: loyalty points to spend on this bill (R0.50 off each); the server checks the
   * balance, the minimum and that they don't exceed what is due.
   */
  record: (appointmentId: string, method: PaymentMethod, promoCode?: string, redeemPoints?: number | null) =>
    api.post<Payment>('/payments/record', {
      appointmentId,
      method,
      promoCode: promoCode ?? null,
      redeemPoints: redeemPoints || null,
    }),
  refund: (paymentId: string) => api.post<Payment>(`/payments/${paymentId}/refund`),
}
