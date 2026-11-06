import { api } from './client'
import type { Role } from './types'

export interface RoleRequest {
  name?: string
  description?: string | null
  /** "MODULE:ACTION" strings. Omitted or empty = no access (least privilege). */
  permissions?: string[]
}

export interface PermissionCatalogue {
  modules: { key: string; label: string }[]
  actions: string[]
}

/** Built-in roles have fixed access and can't be edited or deleted. */
export const BUILT_IN_ROLES = ['ADMIN', 'CUSTOMER', 'STYLIST']

export const rolesApi = {
  list: () => api.get<Role[]>('/roles'),
  permissions: () => api.get<PermissionCatalogue>('/roles/permissions'),
  create: (payload: RoleRequest) => api.post<Role>('/roles', payload),
  update: (id: string, payload: RoleRequest) => api.put<Role>(`/roles/${id}`, payload),
  delete: (id: string) => api.delete<void>(`/roles/${id}`),
}
