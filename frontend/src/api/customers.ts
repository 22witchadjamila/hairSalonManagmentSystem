import { api, ApiError } from './client'
import type { Customer } from './types'

export interface CustomerRequest {
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  dateOfBirth?: string | null
}

export const customersApi = {
  get: (id: string) => api.get<Customer>(`/customers/${id}`),
  list: () => api.get<Customer[]>('/customers'),
  create: (payload: CustomerRequest) => api.post<Customer>('/customers', payload),
  update: (id: string, payload: CustomerRequest) => api.put<Customer>(`/customers/${id}`, payload),
  delete: (id: string) => api.delete<void>(`/customers/${id}`),

  /** Looks up a customer by email, or null if none exists yet (used by the pseudo-login flow). */
  async findByEmail(email: string): Promise<Customer | null> {
    try {
      return await api.get<Customer>(`/customers/email/${encodeURIComponent(email)}`)
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) return null
      throw err
    }
  },

  /** Finds an existing customer by email, or registers a new one - used by the booking form
   *  and by Signup, so booking never fails just because someone already has an account. */
  async findOrCreate(payload: CustomerRequest): Promise<Customer> {
    const existing = await customersApi.findByEmail(payload.email)
    if (existing) return existing
    return customersApi.create(payload)
  },
}
