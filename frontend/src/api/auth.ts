import { api } from './client'

/**
 * Authentication endpoints.
 *
 * Chapter 9.5.2 form-based authentication: the server answers a successful login by setting a
 * session cookie (JSESSIONID). The browser then attaches that cookie to every later request on
 * its own, so there is no token to store, and nothing sensitive is kept in JavaScript where a
 * cross-site-scripting bug could read it.
 */

export interface SessionUser {
  username: string
  firstName: string
  lastName: string
  email: string
  /** CUSTOMER, STYLIST, ADMIN, or the name of a custom staff role (e.g. RECEPTION). */
  role: string
  /** Set for CUSTOMER logins only - the Customer row this login books against. */
  customerId: string | null
  phoneNumber: string | null
  dateOfBirth: string | null
  /** Set for STYLIST logins only - the Stylist row this login works as. */
  stylistId: string | null
  speciality: string | null
  /**
   * "MODULE:ACTION" permissions (all of them for ADMIN, the role's ticked ones for a custom role,
   * none for customers and stylists). A UI hint only - the server checks every request itself.
   */
  permissions: string[]
}

export interface RegisterRequest {
  username: string
  password: string
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
}

export interface LoginRequest {
  username: string
  password: string
  /** "Remember me on this computer": stay signed in for 14 days even after the browser closes. */
  rememberMe?: boolean
}

export const authApi = {
  register: (payload: RegisterRequest) => api.post<SessionUser>('/auth/register', payload),
  login: (payload: LoginRequest) => api.post<SessionUser>('/auth/login', payload),
  logout: () => api.post<void>('/auth/logout'),
  /** Needs the current password. Every other sign-in for the account is ended. */
  changePassword: (currentPassword: string, newPassword: string) =>
    api.post<void>('/auth/change-password', { currentPassword, newPassword }),
  /** 401 when the cookie is missing or expired - callers treat that as "signed out". */
  me: () => api.get<SessionUser>('/auth/me'),
}
