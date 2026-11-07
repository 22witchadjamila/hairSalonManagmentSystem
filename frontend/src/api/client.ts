// Thin fetch wrapper for the Spring Boot backend. No auth headers - the backend has none yet.
//
// Relative on purpose. In production the built app is served by Spring Boot itself, so /api is
// same-origin; in dev, Vite proxies /api to localhost:8080 (see vite.config.ts). Same string
// works in both, nothing is hardcoded to a host or port, and no CORS preflight is involved.
const BASE_URL = '/api'

export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

/**
 * CSRF protection. The server hands the browser a token in the XSRF-TOKEN cookie, and every request
 * that CHANGES something must send it back in the X-XSRF-TOKEN header. A page on another site can
 * make the browser send the cookie, but cannot read it, so it cannot produce the header.
 */
function csrfToken(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/)
  return match ? decodeURIComponent(match[1]) : null
}

async function ensureCsrfToken(): Promise<string | null> {
  let token = csrfToken()
  if (!token) {
    // First request of the visit is a POST (e.g. signing in): ask for any page so the cookie is set.
    await fetch(`${BASE_URL}/auth/me`).catch(() => undefined)
    token = csrfToken()
  }
  return token
}

// Paths where a 401 is an ordinary answer rather than "your session ended".
const EXPECTED_401 = ['/auth/login', '/auth/register', '/auth/me']

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const method = (options.method ?? 'GET').toUpperCase()
  const csrf = method === 'GET' ? null : await ensureCsrfToken()

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
      ...options.headers,
    },
  })

  if (!res.ok) {
    // Backend's GlobalExceptionHandler always returns { status, message, timestamp } JSON on error.
    let message = `Request failed (${res.status})`
    try {
      const body = await res.json()
      if (body?.message) message = body.message
    } catch {
      // response wasn't JSON - fall back to the generic message above
    }
    // Signed out underneath a page that thought it was signed in (the session timed out, or this
    // account signed in on another device). Tell the app so it can send the person to sign in.
    if (res.status === 401 && !EXPECTED_401.includes(path)) {
      window.dispatchEvent(new CustomEvent('session-expired'))
    }
    throw new ApiError(res.status, message)
  }

  // A successful response can legitimately have no body (204, or a 200 from a void endpoint).
  // Calling res.json() on that throws a SyntaxError, which pages then reported as a FAILED
  // request - e.g. "Could not delete this user." after the user had in fact been deleted.
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}
