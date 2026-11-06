// Authentication state for the whole app.
//
// This replaces the old localStorage "pseudo-session". The difference matters: the previous
// version let the browser DECIDE who it was, which is not security at all - anyone could edit
// localStorage and become a customer. Now the browser only ever REPORTS what the server already
// decided, and the server re-checks on every protected request.
//
// The actual credential is the JSESSIONID cookie, managed by the browser. Nothing secret is
// held in here, so a cross-site-scripting bug cannot read a token out of it.
import { reactive, computed } from 'vue'
import { authApi, type SessionUser, type RegisterRequest } from '@/api/auth'
import type { Customer, Stylist } from '@/api/types'
import { ApiError } from '@/api/client'

interface SessionState {
  user: SessionUser | null
  /** False until the first /me call finishes, so guards never act on an unknown state. */
  ready: boolean
}

const state = reactive<SessionState>({ user: null, ready: false })

/** Where each built-in role lands after signing in, and where the guard sends a wrong-role visitor. */
export const HOME_FOR_ROLE: Record<string, string> = {
  CUSTOMER: '/customer',
  STYLIST: '/stylist',
  ADMIN: '/admin',
}

export const BUILT_IN_ROLES = ['CUSTOMER', 'STYLIST', 'ADMIN']

/**
 * The admin page for each permission module, in sidebar order. A custom-role login lands on the
 * first one it may view. (LOYALTY has no admin page of its own.)
 */
export const MODULE_PAGES: { module: string; path: string }[] = [
  { module: 'APPOINTMENTS', path: '/admin/appointments' },
  { module: 'CUSTOMERS', path: '/admin/customers' },
  { module: 'STYLISTS', path: '/admin/stylists' },
  { module: 'SERVICES', path: '/admin/services' },
  { module: 'PAYMENTS', path: '/admin/payments' },
  { module: 'PROMOTIONS', path: '/admin/promotions' },
  { module: 'INVENTORY', path: '/admin/inventory' },
  { module: 'FEEDBACK', path: '/admin/feedback' },
  { module: 'NOTIFICATIONS', path: '/admin/notifications' },
]

export type PermissionAction = 'VIEW' | 'CREATE' | 'EDIT' | 'DELETE'

function can(module: string, action: PermissionAction = 'VIEW'): boolean {
  // ADMIN can always do everything. Decided from the role, not the permissions list, so an
  // admin never loses buttons if that list is missing (e.g. an older backend that doesn't send it).
  if (state.user?.role === 'ADMIN') return true
  return state.user?.permissions?.includes(`${module}:${action}`) ?? false
}

function homeFor(user: SessionUser | null): string {
  if (!user) return '/'
  if (HOME_FOR_ROLE[user.role]) return HOME_FOR_ROLE[user.role]
  // Custom staff role: the first section it can see, or a page explaining it has none yet.
  return MODULE_PAGES.find((p) => can(p.module))?.path ?? '/staff/no-access'
}

let inFlight: Promise<void> | null = null

/**
 * Ask the server who we are. Called once on app start and after login/logout.
 * Concurrent callers share one request so several guards don't fan out into several calls.
 */
export function refreshSession(): Promise<void> {
  if (inFlight) return inFlight
  inFlight = authApi
    .me()
    .then((user) => {
      state.user = user
    })
    .catch((err) => {
      // 401 is the normal "not signed in" answer, not an error worth surfacing.
      if (!(err instanceof ApiError) || err.status !== 401) {
        console.warn('Could not read the session:', err)
      }
      state.user = null
    })
    .finally(() => {
      state.ready = true
      inFlight = null
    })
  return inFlight
}

export function useSession() {
  return {
    user: computed(() => state.user),
    ready: computed(() => state.ready),
    isAuthenticated: computed(() => state.user !== null),
    isCustomer: computed(() => state.user?.role === 'CUSTOMER'),
    isStylist: computed(() => state.user?.role === 'STYLIST'),
    isAdmin: computed(() => state.user?.role === 'ADMIN'),
    /** A staff login with a custom role (e.g. RECEPTION) - access comes from its permissions. */
    isCustomRole: computed(() => !!state.user && !BUILT_IN_ROLES.includes(state.user.role)),
    /** ADMIN or a custom staff role - anyone who works in the /admin pages. */
    isStaff: computed(() => !!state.user && (state.user.role === 'ADMIN' || !BUILT_IN_ROLES.includes(state.user.role))),
    /** Does the signed-in user's role allow this? A UI hint; the server enforces it regardless. */
    can,
    /** The customer this login books as. Null when signed out. */
    customerId: computed(() => state.user?.customerId ?? null),

    // --- compatibility with the pages written against the old pseudo-session ---
    // The customer dashboards read session.customer.value?.customerId / .firstName etc.
    // Rather than touch a dozen views, the signed-in user is projected into that shape.
    // The important change is invisible to them: this is now derived from the SERVER's
    // answer, not from whatever localStorage happened to contain.
    customer: computed<Customer | null>(() =>
      state.user?.role === 'CUSTOMER' && state.user.customerId
        ? ({
            customerId: state.user.customerId,
            firstName: state.user.firstName,
            lastName: state.user.lastName,
            email: state.user.email,
            phoneNumber: state.user.phoneNumber ?? '',
            dateOfBirth: state.user.dateOfBirth ?? null,
          } as Customer)
        : null,
    ),
    // Stylist logins are created by an admin and linked to a Stylist row; the server returns
    // that row's id, so the stylist dashboards read their own schedule, nobody else's.
    stylist: computed<Stylist | null>(() =>
      state.user?.role === 'STYLIST' && state.user.stylistId
        ? ({
            stylistId: state.user.stylistId,
            firstName: state.user.firstName,
            lastName: state.user.lastName,
            email: state.user.email,
            phoneNumber: state.user.phoneNumber ?? '',
            speciality: state.user.speciality ?? '',
            active: true,
          } as Stylist)
        : null,
    ),

    /** The dashboard this login belongs on. */
    home: computed(() => homeFor(state.user)),

    async login(username: string, password: string, rememberMe = false) {
      state.user = await authApi.login({ username, password, rememberMe })
      state.ready = true
      return state.user
    },

    async register(payload: RegisterRequest) {
      // The backend signs the new customer in as part of registering.
      state.user = await authApi.register(payload)
      state.ready = true
      return state.user
    },

    async logout() {
      try {
        await authApi.logout()
      } finally {
        // Clear locally even if the call failed - the cookie may already be gone.
        state.user = null
      }
    },

    /** The server ended the session (timeout, or signed in elsewhere): forget the user without a round trip. */
    endLocally() {
      state.user = null
      state.ready = true
    },

    refresh: refreshSession,

    // Kept so older pages keep compiling. Identity now comes from the server, so a page
    // can no longer declare who it is - these either defer to the real flow or no-op.
    setCustomer(_customer?: Customer) {
      /* no-op: the session is established by login/register, not by a page */
    },
    clearCustomer() {
      return this.logout()
    },
    setStylist(_stylist?: Stylist) {
      /* no-op */
    },
    clearStylist() {
      return this.logout()
    },
  }
}
