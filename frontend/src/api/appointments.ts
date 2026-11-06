import { api } from './client'
import type { Appointment } from './types'

export interface BookAppointmentRequest {
  customerId: string
  stylistId: string
  /** Matches the service_id column. (Was salonServiceId; the server still accepts that.) */
  serviceId: string
  date: string // ISO yyyy-MM-dd
  startTime: string // HH:mm:ss
  notes?: string | null
  /** Optional - validated by the server, applied when the in-store payment is recorded. */
  promoCode?: string | null
}

export const appointmentsApi = {
  book: (payload: BookAppointmentRequest) => api.post<Appointment>('/appointments/book', payload),
  byCustomer: (customerId: string) => api.get<Appointment[]>(`/appointments/customer/${customerId}`),
  byStylist: (stylistId: string) => api.get<Appointment[]>(`/appointments/stylist/${stylistId}`),
  list: () => api.get<Appointment[]>('/appointments'),
  confirm: (id: string) => api.post<Appointment>(`/appointments/${id}/confirm`),
  cancel: (id: string) => api.post<Appointment>(`/appointments/${id}/cancel`),
  complete: (id: string) => api.post<Appointment>(`/appointments/${id}/complete`),
}
