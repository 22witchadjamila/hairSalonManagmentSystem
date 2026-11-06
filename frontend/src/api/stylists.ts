import { api } from './client'
import type { Stylist } from './types'
import type { RetirementResult } from './services'

export interface StylistRequest {
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  speciality: string
}

/** What the public site may know about a stylist. */
export interface PublicStylist {
  stylistId: string
  firstName: string
  lastName: string
  speciality: string
}

export const stylistsApi = {
  /** Public: names and speciality only (no email or phone). */
  listActive: () => api.get<PublicStylist[]>('/stylists/active'),
  list: () => api.get<Stylist[]>('/stylists'),
  get: (id: string) => api.get<Stylist>(`/stylists/${id}`),
  create: (payload: StylistRequest) => api.post<Stylist>('/stylists', payload),
  update: (id: string, payload: StylistRequest) => api.put<Stylist>(`/stylists/${id}`, payload),
  /** Deletes an unused stylist, archives one with appointments - the result says which. */
  delete: (id: string) => api.delete<RetirementResult>(`/stylists/${id}`),
  restore: (id: string) => api.post<Stylist>(`/stylists/${id}/restore`),
}
