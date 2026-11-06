# Security: how Chapter 9 is implemented

This maps each section of *Security Notes for AppDev3* (Chapter 9) to the code in this project. It
lists where the project deliberately does something **different** from the chapter, and how to
demonstrate each point. The system runs on localhost; nothing here assumes a public server.

## 1. The chapter's checklist

| Chapter | What it asks for | Status | Where |
|---|---|---|---|
| 9.2.1 | Prevent unauthorised access (authentication) | Done | `SecurityConfig`, `AuthController`, `AppUserDetailsService` |
| 9.2.1 | Accountability (non-repudiation) | Done | `audit/*`: the Audit Log page |
| 9.2.1 | Protect against service interruptions (QoS) | Done | `LoginAttemptService`: lockout after 5 failures |
| 9.2.2 | Authorization / access control | Done | `SecurityConfig` rules, `PermissionPolicy`, `AccessRules`, `@PreAuthorize` |
| 9.2.2 | Data integrity and confidentiality | Done | `GlobalExceptionHandler`, public endpoints return projections, passwords never returned (`@JsonIgnore`), CSRF |
| 9.2.2 | Auditing | Done | `AuditInterceptor`, `AuditService`, `/admin/audit` |
| 9.3 | Users, roles, realms | Done | `User`, `Role`, `RoleSeeder`, custom roles with permissions |
| 9.4 | SSL / secure connection | Done, optional | `https` profile (section 3) |
| 9.5.2 | Form-based authentication | Done (JSON form) | `POST /api/auth/login`, the Vue sign-in page |
| 9.5.2 | "Remember me on this computer" | Done | `rememberMeServices` in `SecurityConfig` |
| 9.5.1 / 9.5.3 / 9.5.4 | Basic, client-certificate and digest authentication | Not used | The chapter's own text says Basic and form login are weak without SSL; we use form login |
| 9.6 | `Users` and `Roles` entities, `enabled` flag | Done | `domain/User` (`isActive`), `domain/Role` |
| 9.6 | `springSecurityFilterChain` | Done (automatic) | Spring Boot registers it, so no `web.xml` |
| 9.6 | `login.jsp` / `accessDenied.jsp` | Done (Vue pages) | `views/Auth/Signin.vue`, `views/Errors/AccessDenied.vue` |
| 9.7.1 | Namespace configuration | Java config | `SecurityConfig` |
| 9.7.2 | Password encoder | Done (BCrypt, not MD5) | `SecurityConfig.passwordEncoder()` |
| 9.7.3 | `requires-channel="https"` + port mapping | Done, optional | `HttpsRedirectConfig`, `requiresChannel` in `SecurityConfig` |
| 9.7.4 | Concurrent session control, `max-sessions="1"` | Done | `sessionAuthenticationStrategy`, `sessionConcurrency` |
| 9.7.4 | `HttpSessionEventPublisher` listener | Done | `SecurityConfig.httpSessionEventPublisher()` |
| 9.7.5 | Session fixation protection | Done (`migrateSession`) | `SessionFixationProtectionStrategy` |
| 9.7.6 | Method security | Done (`@PreAuthorize`) | `@EnableMethodSecurity`; controllers |
| 9.7.7 | Current user via `SecurityContextHolder` | Done | `AuthController.me()` |
| 9.8 | Users with different roles, appropriate messages on errors | Done | roles below; 401/403/429 JSON messages; the Access denied page |

## 2. Deliberate differences from the chapter

| Chapter | This project | Why |
|---|---|---|
| MD5 password hashes | **BCrypt** | MD5 is fast and unsalted. A leaked table is cracked in minutes. BCrypt is salted and slow. The idea the chapter teaches, never storing plain text, is unchanged. |
| XML (`ApplicationContextsecurty.xml`, `web.xml`) | Java configuration | Spring Boot's way of writing the same four sections. |
| `login.jsp` posting to `j_spring_security_check`, redirects | JSON `POST /api/auth/login` | The front end is a Vue single-page app. It needs a JSON answer, not an HTML redirect. |
| `<jdbc-user-service>` SQL queries | `AppUserDetailsService` | The same two lookups (user plus password and `enabled`, user plus role), through JPA. |
| `@Secured` / JSR-250 | `@PreAuthorize` | The chapter itself recommends expression annotations for anything beyond simple rules. |

## 3. Running over HTTPS on localhost (9.4 / 9.7.3)

Optional. The normal `http://localhost:8080` way of running is unchanged.

1. Create a self-signed certificate once, in the project folder. The `*.p12` pattern is in
   `.gitignore`, so it is never committed:
   ```
   keytool -genkeypair -alias salon -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore localhost-dev.p12 -validity 365 -dname "CN=localhost" -ext SAN=dns:localhost,ip:127.0.0.1
   ```
   It asks for a keystore password. Remember it.
2. In the IntelliJ run configuration add:
   - environment variable `SALON_SSL_KEYSTORE_PASSWORD=<that password>`
   - active profile `https` (or add `https` to the program argument `--spring.profiles.active=...`)
3. Start the app and open **https://localhost:8443**. The browser warns that the certificate is
   self-signed. That's expected for a local certificate, so choose "continue".
4. `http://localhost:8080` still answers, but only to send you to `https://localhost:8443`.

In this mode every request must be secure (`requires-channel="https"`), the session cookie is
marked `Secure`, and the HTTP-to-HTTPS port mapping is the chapter's `<port-mapping>`.

## 4. How to demonstrate each feature

| Feature | Try this | You should see |
|---|---|---|
| Authentication | Sign in with a wrong password | "Incorrect username or password." (the same for a wrong password and an unknown user) |
| Roles / access control | Sign in as a customer, open `/admin` | The **Access denied** page |
| Server-side check | As a customer: `GET /api/payments` | 403 "You do not have permission to perform this action." |
| Lockout | Five wrong passwords for one username, then the right one | "Too many failed sign-in attempts. Try again in 15 minute(s)." (HTTP 429) |
| One session per user (9.7.4) | Sign in as the same user in two browsers, then click in the first | The first is taken to sign-in: "You were signed out, or this account signed in somewhere else." |
| Session fixation (9.7.5) | Compare `JSESSIONID` before and after signing in (browser dev tools, Application tab) | A different value |
| Remember me | Tick the box and sign in; the session cookie disappears when the browser closes, but `salon-remember-me` stays | Still signed in for up to 14 days |
| CSRF | `POST` to any `/api` endpoint without the `X-XSRF-TOKEN` header | 403 "Your page is out of date." |
| Password rules | Sign up with `Password123`, or `abc` | Refused with the reason |
| Change password | Profile page, "Change my password" | Other devices are signed out; remembered sign-ins stop working |
| Admin reset | Users & Roles, "Reset password" | That person is signed out and can use the new password |
| Audit log | Admin, **Audit Log** | Sign-ins, failures, locks, payments, refunds, role and password changes, blocked attempts: who, what, when, from where |
| API docs | Open `/swagger-ui.html` signed out | 401 |
| Method security | `@PreAuthorize` on controllers, for example `AppointmentController.cancel` | Customers cancel only their own bookings |

## 5. Roles

| Role | Can |
|---|---|
| **CUSTOMER** | Book, see and cancel their own appointments, their own profile, loyalty and messages. |
| **STYLIST** | See their own schedule; confirm, complete and cancel their own bookings; their profile. |
| **ADMIN** | Everything, including users, roles, the audit log and API docs. |
| Custom roles | Only the areas an admin ticks (module x action). They can **never** be given users, roles or the audit log. |

## 6. Known limits

These are real and deliberate, and are worth stating if asked:

- **Lockout is in memory.** A restart clears all locks. Someone could lock a known username out for
  15 minutes by failing on purpose; it clears by itself and the audit log records it.
- **No "forgot password" email.** Nothing sends email, so an admin resets a forgotten password
  (Users & Roles, "Reset password").
- **The audit log has no retention limit.** It only grows. A real system would archive old rows.
- **Remember-me cookies can't be revoked one by one.** They expire after 14 days, or immediately when the
  password is changed or reset. Restarting the app without `SALON_REMEMBER_ME_KEY` set also invalidates them.
- **No multi-factor authentication, no CAPTCHA, no password-breach checking** beyond a short
  list of very common passwords.
- **HTTPS uses a self-signed certificate.** It encrypts the connection but doesn't prove the server's identity.
  That's fine on your own machine.
