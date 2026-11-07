import { api } from './client'
import type { SalonService, ServiceCategory } from './types'

export interface SalonServiceRequest {
  name: string
  description?: string | null
  durationMinutes: number
  price: number
  category: ServiceCategory
}

export interface RetirementResult {
  outcome: 'DELETED' | 'ARCHIVED'
  message: string
}

export const servicesApi = {
  listActive: () => api.get<SalonService[]>('/services/active'),
  list: () => api.get<SalonService[]>('/services'),
  get: (id: string) => api.get<SalonService>(`/services/${id}`),
  create: (payload: SalonServiceRequest) => api.post<SalonService>('/services', payload),
  update: (id: string, payload: SalonServiceRequest) => api.put<SalonService>(`/services/${id}`, payload),
  /** Deletes an unused service, archives a booked one - the result says which. */
  delete: (id: string) => api.delete<RetirementResult>(`/services/${id}`),
  restore: (id: string) => api.post<SalonService>(`/services/${id}/restore`),
}
