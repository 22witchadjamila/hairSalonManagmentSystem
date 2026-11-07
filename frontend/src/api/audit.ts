import { api } from './client'

/** One line of the audit trail: who did what, when, from where, and whether it worked. */
export interface AuditEvent {
  auditId: string
  occurredAt: string
  /** The signed-in username, the name typed on a failed sign-in, or "anonymous". */
  actor: string
  role: string | null
  action: string
  method: string
  path: string
  status: number | null
  outcome: 'SUCCESS' | 'FAILED' | 'DENIED'
  ip: string | null
  detail: string | null
}

export const auditApi = {
  /** Newest first. Admin only; there is no way to edit or delete an entry. */
  latest: (limit = 300) => api.get<AuditEvent[]>(`/audit?limit=${limit}`),
}
