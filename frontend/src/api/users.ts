import { api } from './client'
import type { User } from './types'

/**
 * Creates a STAFF login: ADMIN when roleId is empty, otherwise the chosen custom role.
 * Stylist logins are made on the Stylists page; customers sign up.
 */
export interface UserRequest {
  username: string
  password: string
  email: string
  firstName: string
  lastName: string
  roleId?: string | null
}

export const usersApi = {
  list: () => api.get<User[]>('/users'),
  create: (payload: UserRequest) => api.post<User>('/users', payload),
  /** Move a staff login to ADMIN or a custom role. Their open session ends. */
  changeRole: (id: string, roleId: string) => api.put<User>(`/users/${id}/role`, { roleId }),
  /** Admin: set someone else's password. Their open sessions end. Not for your own account. */
  resetPassword: (id: string, newPassword: string) => api.put<void>(`/users/${id}/password`, { newPassword }),
  delete: (id: string) => api.delete<void>(`/users/${id}`),
}
