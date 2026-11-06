import { api } from './client'

/** Admin-only endpoints. The server answers 403 to anyone without the ADMIN role. */

export interface StylistLogin {
  stylistId: string
  username: string
}

export const adminApi = {
  stylistLogins: () => api.get<StylistLogin[]>('/admin/stylist-accounts'),
  /** Name and email are taken from the Stylist row on the server - only credentials are sent. */
  createStylistLogin: (stylistId: string, username: string, password: string) =>
    api.post<StylistLogin>(`/admin/stylists/${stylistId}/account`, { username, password }),
}
