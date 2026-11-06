# Hair Salon Management System

A full-stack salon booking and management platform: a **Spring Boot** REST API paired with a **Vue 3** single-page application. Customers sign up and book appointments online and pay in store, stylists manage their own bookings, and administrators run every part of the business: services, staff, stock, promotions, payments, loyalty, feedback and user access. The API is secured with Spring Security (roles, permissions, sessions, CSRF protection, an audit trail).

---

## Contents

1. [What the System Does](#what-the-system-does)
2. [Tech Stack](#tech-stack)
3. [Architecture](#architecture)
4. [Security](#security)
5. [Domain Model](#domain-model)
6. [REST API](#rest-api)
7. [The Booking & In-Store Payment Workflow](#the-booking--in-store-payment-workflow)
8. [Getting Started](#getting-started)
9. [Testing](#testing)
10. [Documentation](#documentation)
11. [Repository Layout](#repository-layout)
12. [Team](#team)
13. [UML Class Diagram](#uml-class-diagram)

---

## What the System Does

The application serves three audiences, each with its own dashboard:

| Role | What they can do |
| --- | --- |
| **Customer** | Browse the public site, **sign up** (required before booking), book an appointment with an optional promotion code, track and cancel their own bookings, see their loyalty points and tier, receive confirmations and reminders in the notification bell, leave feedback, manage their profile and password |
| **Stylist** | Sign in with a login the admin creates for them, see their own schedule, **confirm, complete or cancel** their own bookings, manage their profile and password |
| **Admin** | Dashboards for Customers, Stylists, Services, Appointments, Payments, Promotions, Inventory, Feedback, Notifications, Users & Roles and the Audit Log; record payments and refunds, edit or end promotions, reset passwords |

The backend also has a **permission model for custom staff roles** (module × action, e.g. "Appointments: view, edit") that is enforced on every request. Users, roles and the audit log can never be granted to a custom role. The admin screen currently manages the three built-in roles.

Business rules live in the **service layer** and are enforced regardless of which client calls the API:

- Customers and stylists cannot be **double-booked** for overlapping time slots
- Appointments can only be made against **active** stylists and services; a service or stylist that has bookings is **archived** instead of deleted
- Appointment status changes are tracked (`PENDING → CONFIRMED → COMPLETED`, or `CANCELLED`); only pending or confirmed bookings can be cancelled, and a **paid** booking must be refunded first
- **Payments are priced by the server** from the booked service (a client can't set the price), one paid payment per appointment, with refunds
- **Promotions** are valid inside their date range and usage limit. A booking *holds* a use; a recorded payment *counts* it; a refund gives it back. Admins can edit a promotion, end it early or reopen it
- **Loyalty:** 1 point per R10 paid. Points are spent when staff record a payment (R0.50 each, minimum 50, never more than the bill). Tiers follow lifetime points: Silver from 500, Gold from 2,000, Platinum from 5,000. A refund reverses both
- Stock adjustments are checked, and low-stock products can be listed on demand
- Messages reach customers in-app: booking confirmations, automatic day-before reminders, cancellation notices and promotion announcements

---

## Tech Stack

### Backend

| | |
| --- | --- |
| Language | Java 22 |
| Framework | Spring Boot 3.5.4 |
| Security | Spring Security 6 (session cookie, BCrypt, CSRF token, method security) |
| Build | Maven |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL 8 (with an H2 in-memory profile) |
| Migrations | Flyway, with a backup gate (see [docs/DATABASE_MIGRATIONS.md](hairSalonManagmentSystem/docs/DATABASE_MIGRATIONS.md)) |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| API docs | springdoc-openapi 2.8.5 → `/swagger-ui.html` (admin sign-in required) |
| Testing | JUnit 5 + Mockito, 345 tests in 50 classes |

### Frontend

| | |
| --- | --- |
| Framework | Vue 3 (Composition API) |
| Language | TypeScript |
| Build | Vite |
| Styling | Tailwind CSS 4 |
| Routing | Vue Router 4 (history mode, with a role guard) |
| Base template | TailAdmin Vue Pro (the template's own files are not included in this repo) |

The source is in [`frontend/`](frontend/); the finished build is served from the backend's `static/` folder.

---

## Architecture

The backend is a **layered, DDD-flavoured** Spring Boot application under the `za.ac.cput` package. Every layer has exactly one job, so a change to "how a promotion discount is calculated" only ever touches the service layer, and a change to a JSON response shape only ever touches the web layer.

```
HTTP request
     │
     ▼
┌─────────────┐   Spring Security: who are you, and may you call this? (config/)
│  security   │   CSRF token, session, role + permission checks, audit trail
└─────────────┘
     │
     ▼
┌─────────────┐   thin: translates HTTP <-> service calls, no business logic
│ controller  │
└─────────────┘
     │
     ▼
┌─────────────┐   all business rules live here; depends on interfaces only
│  service    │──────────────┐
└─────────────┘              │
     │                       ▼
     ▼                 ┌──────────┐   static build*() methods, validation
┌─────────────┐        │ factory  │───────────┐
│ repository  │        └──────────┘           │
└─────────────┘                               ▼
     │                                  ┌──────────┐
     ▼                                  │   util   │  shared Helper
┌─────────────┐                         └──────────┘
│   database  │
└─────────────┘

Anything thrown along the way → exception/GlobalExceptionHandler → clean JSON error
```

### Package responsibilities

| Package | What lives here | Why it's separate |
| --- | --- | --- |
| `domain` | JPA `@Entity` classes built with the **Builder pattern**, with no public setters, so an entity is either fully valid or doesn't exist | The data model shouldn't know anything about HTTP or SQL |
| `domain/valueobject` | Small immutable types: `Email`, `PhoneNumber`, `Money`, `TimeSlot`, `DateRange` | Makes invalid data unconstructable: `Email.of("not-an-email")` throws immediately rather than flowing through the system |
| `domain/enums` | Fixed sets of named values, including the permission modules and actions | A typo like `"Pendnig"` becomes a compile error instead of a runtime bug |
| `factory` | One static `build*(...)` per entity; validates raw input via `util/Helper` and returns a built entity or `null` | Construction and validation in one place, not scattered across services |
| `repository` | One `interface XRepository extends JpaRepository<X, String>` plus derived and atomic queries | The only layer that knows SQL/JPA exists |
| `service` / `service/impl` | One `IXService` interface + `XServiceImpl` per entity over a generic `IService<T, ID>`, plus workflow services that need more than CRUD: `PaymentRecordingService`, `PromotionOfferService`, `PromotionAdminService`, `RoleAdminService`, `AuthService`, `SearchService`, `RetirementService`, `PasswordPolicy` | Business rules in one place; depending on the *interface* makes the suite mockable |
| `controller` | One `@RestController` per resource under `/api/<resource>` | Thin by design: an `if` in a controller usually belongs in a service |
| `config` | `SecurityConfig` and the pieces it uses (`PermissionPolicy`, `AccessRules`, `LoginAttemptService`, `SessionFreshnessFilter`, `CsrfCookieFilter`), first-run setup (`RoleSeeder`, `AdminBootstrap`), the reminder scheduler, the migration backup gate, `CorsConfig`, `SpaFallbackController` | Cross-cutting setup that belongs to no single resource |
| `audit` | The audit trail: `AuditEvent`, the interceptor that records every change, its labels and the admin-only endpoint | Accountability, kept apart from the business code it watches |
| `exception` | `ResourceNotFoundException`, `InvalidOperationException`, `ApiError`, and a `@RestControllerAdvice` handler | Every error returns the same JSON shape instead of a raw stack trace |
| `util` | `Helper.java`: shared email/phone validation, ID generation, discount and loyalty maths | One regex for "what's a valid email", not ten near-copies |
| `db/migration` (Java) | Flyway migrations: V2 renames four tables to the plural snake_case convention | Schema changes are versioned and repeatable |

### Error format

Every thrown exception is converted to one consistent body:

```json
{
  "status": 404,
  "message": "Customer not found with id: C-1042",
  "timestamp": "2026-09-20T14:31:07.882"
}
```

---

## Security

Built to the course's Chapter 9 notes (Spring Security). [docs/SECURITY.md](hairSalonManagmentSystem/docs/SECURITY.md) maps every section of the chapter to the code, lists the deliberate differences, and has a "how to demonstrate it" table.

| Area | How |
| --- | --- |
| Authentication | JSON form login, **BCrypt** password hashes (instead of the chapter's MD5), HttpOnly + SameSite session cookie, optional "Remember me" (14 days) |
| Authorization | **Deny by default**: every `/api` path is admin-only unless explicitly opened. Customers and stylists reach only their own records (`@PreAuthorize` ownership checks) |
| Sessions | One session per account; a new session id at every sign-in (fixation protection); idle timeout 30 minutes |
| Protection | CSRF token on every change request; **lockout** after 5 wrong passwords (15 minutes); password rules; the API docs are admin-only |
| Accountability | An **audit log** (admin only, append-only): sign-ins and failures, payments, refunds, promotions, role and password changes, and every blocked attempt |
| Accounts | Customers sign up; stylist logins are created by an admin; the first admin comes from environment variables (nothing hard-coded). Users can change their password; admins can reset one |
| Transport | Optional HTTPS mode on localhost (`https` profile) |

---

## Domain Model

**13 entities**, each with a repository, and all but the audit trail with a full repository → service → controller stack:

| Entity | Represents |
| --- | --- |
| `Customer` | People who register and book appointments |
| `Stylist` | Salon staff who provide services: name, specialisation |
| `SalonService` | A service on the menu (cut, colour, treatment) with price, duration and category |
| `Appointment` | A scheduled session between a customer and a stylist: date, time slot, status, optional promotion code |
| `Payment` | A recorded in-store payment against an appointment: method, price, discount (promotion and loyalty points), status (paid / refunded) |
| `Promotion` | A discount code with a date range and usage limit; can be ended early |
| `LoyaltyReward` | Points balance, lifetime points and tier for a customer |
| `ProductInventory` | Retail/back-bar stock levels, with low-stock reporting and a "for sale" flag for the public shop |
| `Feedback` | Customer reviews tied to a customer and appointment |
| `Notification` | In-app messages to customers (confirmations, reminders, cancellations, promotions) |
| `User` | A sign-in account (customer, stylist, admin or custom staff role) |
| `Role` | A named role with, for custom roles, a set of permissions (`role_permissions`) |
| `AuditEvent` | One line of the audit trail: who, what, when, from where, and the result |

**5 value objects**: `Email`, `PhoneNumber`, `Money`, `TimeSlot`, `DateRange`.
Each exposes its raw form via `@JsonValue`, so they serialise as plain strings and numbers (`"email": "jane@example.com"`), not nested objects, which keeps the frontend's TypeScript interfaces simple.

**11 enums**: `AppointmentStatus`, `PaymentStatus`, `PaymentMethod`, `ServiceCategory`, `DiscountType`, `LoyaltyTier`, `NotificationType`, `NotificationStatus`, `NotificationChannel`, `PermissionModule`, `PermissionAction`.

---

## REST API

Base URL: `http://localhost:8080/api` · Interactive docs: `http://localhost:8080/swagger-ui.html` (sign in as an admin first)

Most resources follow the standard shape (`POST` create, `GET /{id}`, `GET` list, `DELETE /{id}`), with the access rules above applied. Notable endpoints:

| Resource | Base path | Notable endpoints |
| --- | --- | --- |
| Auth | `/api/auth` | `POST /register`, `POST /login`, `POST /logout`, `POST /change-password`, `GET /me` |
| Appointments | `/api/appointments` | `POST /book`, `POST /{id}/confirm`, `POST /{id}/cancel`, `POST /{id}/complete`, `GET /customer/{customerId}`, `GET /stylist/{stylistId}` |
| Customers | `/api/customers` | `GET /email/{email}`, `PUT /{id}` |
| Stylists | `/api/stylists` | `GET /active` (public), `PUT /{id}`, `POST /{id}/restore` |
| Services | `/api/services` | `GET /active` (public), `GET /category/{category}`, `PUT /{id}`, `POST /{id}/restore` |
| Payments | `/api/payments` | `POST /record` (priced by the server; optional promotion code and loyalty points), `POST /{id}/refund`, `GET /summary`, `GET /appointment/{appointmentId}` |
| Promotions | `/api/promotions` | `GET /available` and `GET /quote` (public), `GET /stats`, `PUT /{id}`, `POST /{id}/end`, `POST /{id}/reopen` |
| Loyalty rewards | `/api/loyalty-rewards` | `GET /customer/{customerId}`, `POST /customer/{customerId}/add-points` (admin), `GET /rules` |
| Products | `/api/products` | `GET /shop` (public), `GET /low-stock`, `POST /{id}/adjust-stock`, `POST /{id}/for-sale`, `PUT /{id}` |
| Feedback | `/api/feedback` | `GET /public`, `GET /customer/{customerId}` |
| Notifications | `/api/notifications` | `POST /promotion`, `GET /customer/{customerId}` |
| Users | `/api/users` | `GET /username/{username}`, `PUT /{id}/role`, `PUT /{id}/password` (admin reset) |
| Roles | `/api/roles` | `GET /permissions`, `PUT /{id}`, `GET /name/{name}` |
| Admin | `/api/admin` | `POST /stylists/{stylistId}/account`, `GET /stylist-accounts` |
| Search | `/api/search` | `GET ?q=` (results limited to what the role may see) |
| Audit | `/api/audit` | `GET ?limit=` (admin only; entries can't be changed or deleted) |

---

## The Booking & In-Store Payment Workflow

**There is no online payment, by design.** Money changes hands at the salon. The flow is:

1. A customer **signs up or signs in** (required), then books through the public site → `POST /api/appointments/book` → the appointment is created with status **`PENDING`**. They can add a promotion code; the booking *holds* one use of it but doesn't count it yet.
2. The customer sees the price on the confirmation screen and under "My Appointments", always labelled **payable in store**.
3. The stylist (or an admin) **confirms** the booking → status **`CONFIRMED`**, and the customer gets an in-app message. A background job also creates a **reminder** for confirmed bookings the day before. No email or SMS provider is involved; the customer's notification bell simply reads the messages.
4. **Cancelling:** the customer, their stylist or an admin can cancel a pending or confirmed booking; the customer is told when staff cancel. A paid booking must be refunded first.
5. After the visit the booking is marked **`COMPLETED`**, and staff **record the payment** (`POST /api/payments/record`). The server prices it from the service, applies the booked promotion code (counting one use), and can take off loyalty points the customer chooses to spend. The customer then earns points on what they actually paid.
6. A **refund** reverses all of it: the promotion use and the points go back as they were.

---

## Getting Started

### Prerequisites

- **JDK 22**
- **Maven 3.9+** (or IntelliJ IDEA, which bundles it)
- **MySQL 8** running on `localhost:3306` *(optional, see the H2 profile below)*
- **Node.js 20+** and npm, only if you are rebuilding the frontend (needs the full TailAdmin project, see [frontend/README.md](frontend/README.md))

### 1. Create the first admin

There is no built-in admin account. On first start, if no admin exists, one is created from two environment variables:

```bash
export SALON_ADMIN_USERNAME=admin
export SALON_ADMIN_PASSWORD=choose-a-12-plus-char-password-1
```

On Windows PowerShell:

```powershell
$env:SALON_ADMIN_USERNAME="admin"; $env:SALON_ADMIN_PASSWORD="choose-a-12-plus-char-password-1"
```

The password needs 12 or more characters, with a letter and a number. It is only read when no admin exists yet.

### 2. Choose a database

Credentials are read from environment variables and are **never** hardcoded. `application.properties` defaults the database name to `hair_salon_db` and creates it on first run if it doesn't exist.

```bash
export DB_NAME=hair_salon_db
export DB_USERNAME=salon_app
export DB_PASSWORD=your_password_here
```

On Windows PowerShell:

```powershell
$env:DB_NAME="hair_salon_db"; $env:DB_USERNAME="salon_app"; $env:DB_PASSWORD="your_password_here"
```

**No MySQL installed?** Start with the H2 in-memory profile instead. Nothing to install, data resets on every restart, and the H2 console is at `/h2-console`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

> **Starting against an existing MySQL database:** the app refuses to start when a schema change is waiting until you confirm you have a backup (`SALON_DB_BACKUP_CONFIRMED=<version>`). Steps and the rollback script are in [docs/DATABASE_MIGRATIONS.md](hairSalonManagmentSystem/docs/DATABASE_MIGRATIONS.md). A fresh or H2 database is never blocked.

### 3. Run the backend

From `hairSalonManagmentSystem/`:

```bash
mvn spring-boot:run
```

Open **`http://localhost:8080`**. The backend serves the finished website and the API together: the built frontend lives in `src/main/resources/static`. Sign in with the admin account, add some Services and Stylists, and customers can sign up from the public site. Stylist logins are created by the admin on the Stylists page.

### 4. HTTPS on localhost *(optional)*

The `https` profile serves the app over HTTPS with a self-signed certificate. Setup steps are in [docs/SECURITY.md](hairSalonManagmentSystem/docs/SECURITY.md).

### Developing the frontend

| Mode | How | When to use it |
| --- | --- | --- |
| **Backend only** *(default)* | `mvn spring-boot:run`, then open `localhost:8080`. The backend serves the already-built site. | Running or demonstrating the system |
| **Two dev servers** | Backend on `:8080`, frontend `npm run dev` on `:5173` with Vite proxying `/api` to the backend. Hot-reloads on every save. | Day-to-day frontend development *(needs the full TailAdmin project, which isn't in this repo)* |
| **Rebuild into the backend** | `npm run build`, then copy the resulting `dist/` into `src/main/resources/static` | Updating the served site after frontend changes |

> Vue Router runs in history mode, so refreshing on a route like `/admin/customers` sends a real request for that path. `SpaFallbackController` forwards any dot-less path to `index.html` so the router can take over client-side, while real `/api/**` routes still win. Spring always matches the most specific registered pattern first.

---

## Testing

```bash
mvn test
```

**345 tests in 50 classes, all passing** (JUnit 5 + Mockito, plus a Flyway migration test that runs the table renames against an H2 database shaped like the old schema). They cover the services, controllers, factories, security rules, password policy, lockout, audit labels, search ranking and the migration.

---

## Documentation

| Document | What it covers |
| --- | --- |
| [docs/SECURITY.md](hairSalonManagmentSystem/docs/SECURITY.md) | Chapter 9 mapped to the code, deliberate differences from the notes, how to demonstrate each feature, known limits |
| [docs/DATABASE_MIGRATIONS.md](hairSalonManagmentSystem/docs/DATABASE_MIGRATIONS.md) | The Flyway table renames, taking a backup, rehearsing on a copy, rollback |
| [frontend/README.md](frontend/README.md) | The frontend source: what's included, where to look, how the app is served |

---

## Repository Layout

```
hairSalonManagmentSystem/                  <- this repo
├── hairSalonManagmentSystem/              the Spring Boot REST API
│   ├── docs/                              SECURITY.md, DATABASE_MIGRATIONS.md
│   ├── src/main/java/za/ac/cput/
│   │   ├── audit/           audit trail (entity, service, interceptor, endpoint)
│   │   ├── config/          security, first-run setup, scheduler, CORS, SPA fallback
│   │   ├── controller/      15 @RestControllers under /api/*
│   │   ├── domain/          12 JPA entities
│   │   │   ├── enums/       11 enums
│   │   │   └── valueobject/ 5 immutable value objects
│   │   ├── exception/       ApiError + GlobalExceptionHandler
│   │   ├── factory/         static build*() methods
│   │   ├── repository/      Spring Data repositories
│   │   ├── service/         interfaces, generic IService<T, ID> and the workflow services
│   │   │   └── impl/        12 implementations
│   │   ├── util/            Helper.java
│   │   └── Main.java
│   ├── src/main/java/db/migration/   Flyway migrations (Java)
│   ├── src/main/resources/  application*.properties, db/rollback, static/ (the built frontend)
│   ├── src/test/java/       50 test classes, 345 tests
│   └── pom.xml
├── frontend/                              the Vue 3 SPA source (see frontend/README.md)
│   └── src/
│       ├── api/             one typed module per resource + shared client.ts (CSRF, errors)
│       ├── components/      layout, account and admin components, public-site components
│       ├── composables/     useSession (who is signed in, role, permissions), search, refresh
│       ├── config/          menus.ts (sidebar menus per role), site content
│       ├── router/          route definitions with a role guard
│       └── views/
│           ├── public/      Home, About, Services, Stylists, Products, Gallery, Contact, BookAppointment
│           ├── Auth/        Signin (with "Remember me"), Signup
│           ├── customer/    customer dashboard pages
│           ├── stylist/     stylist dashboard pages
│           └── admin/       the admin dashboards, including the Audit Log
├── README.md
└── assignment1UML.png
```

---

## Team

**Leader**
- *Marc Kabala* — System coordination & architecture

**Contributors**

| Member | Student № | Entity ownership |
| --- | --- | --- |
| Marc Kabala | 230701876 | Customer |
| Dayyaan Francis | 222277343 | Appointment |
| Will Koeries | 240160711 | Stylist |
| Witcha Francisco | 222894822 | Service |
| Reece Josephs | 218152701 | Payment |

---

## UML Class Diagram

The original design from Assignment 1. The system has grown since (audit trail, permissions, loyalty and promotion workflows), but the core entities and their relationships are unchanged.

<img width="1980" height="1800" alt="assignment1UML" src="https://github.com/user-attachments/assets/860ca28a-36cb-4f49-9b79-9fb5def4e2a6" />
