import { api } from './client'
import type { Notification } from './types'

export const notificationsApi = {
  list: () => api.get<Notification[]>('/notifications'),
  /** Everything a customer should see: their own booking messages plus recent promotion messages. */
  byCustomer: (customerId: string) => api.get<Notification[]>(`/notifications/customer/${customerId}`),
  /**
   * A promotion message for every customer. In-app is the only channel that is connected -
   * nothing sends email, SMS or push - so that is all this offers.
   */
  sendPromotion: (message: string) =>
    api.post<Notification>('/notifications/promotion', { message, channel: 'IN_APP' }),
  delete: (id: string) => api.delete<void>(`/notifications/${id}`),
}
