# Frontend (Vue 3 + TypeScript + Tailwind CSS)

This folder holds the source code of the Judith Hair Salon frontend: the public website and the
customer, stylist and admin dashboards. The backend (the Spring Boot project in
`../hairSalonManagmentSystem`) serves the finished, built version of this code, so **you don't need
anything in this folder to run the application**. It is here so the code can be read.

## What is and isn't in this folder

The dashboards are built on **TailAdmin Vue Pro**, a paid template. Its files are licensed and are
**not included**. This folder contains only the code written for this project:

- all of `src/api/`, `src/views/`, `src/composables/` and `src/config/`
- the pages, the router and its role guard, the public-site components, and the form and search components
- `src/assets/site-theme.css`: the public site's own fonts, colours and utility classes
- the project setup files (`package.json`, `vite.config.ts`, the `tsconfig` files, `eslint.config.ts`)

Because the template files are missing, `npm run dev` and `npm run build` **won't work from this folder
alone**. The files the code builds on (roughly: the app shell `main.ts` and `main.css`, the sidebar and
header components, the icon set, and a few small layout helpers) come with the template. The finished build
is in `../hairSalonManagmentSystem/src/main/resources/static`.

## Running the application

You need a JDK (22 or newer) and either IntelliJ IDEA or Maven.

1. Open `../hairSalonManagmentSystem` in IntelliJ and run the class `za.ac.cput.Main`, with:
   - **Active profiles:** `h2` (an in-memory database, so no MySQL is needed; its data disappears when the app stops)
   - **Environment variables:** `SALON_ADMIN_USERNAME` and `SALON_ADMIN_PASSWORD`. These create the first
     admin account. The password needs 12 or more characters, with a letter and a number.
     Tick "Include system environment variables".

   Or from a terminal, in `../hairSalonManagmentSystem`:
   ```
   mvn spring-boot:run -Dspring-boot.run.profiles=h2
   ```
   (with the two environment variables set first).
2. Open **http://localhost:8080**.
3. Sign in with the admin account, then use **Services** and **Stylists** to add some data.
   Customers sign up themselves on the **Sign Up** page. Stylist logins are created by the admin on the
   Stylists page.

To use MySQL instead, leave out the `h2` profile and set `DB_NAME` (default `hair_salon_db`),
`DB_USERNAME` (default `salon_app`) and `DB_PASSWORD`. See `../hairSalonManagmentSystem/docs/DATABASE_MIGRATIONS.md`.

## Where to look

| To see... | Open |
|---|---|
| How the frontend talks to the backend (the only place that calls `/api`) | `src/api/client.ts` and the other files in `src/api/` |
| Who is signed in, their role and permissions | `src/composables/useSession.ts` |
| Pages, and the role check before each one (customer / stylist / admin / custom role) | `src/router/index.ts` |
| Public website pages | `src/views/public/` |
| Dashboards | `src/views/customer/`, `src/views/stylist/`, `src/views/admin/` |
| Sign-in (with "Remember me"), sign-up, access denied | `src/views/Auth/`, `src/views/Errors/AccessDenied.vue` |
| Search (header search and per-table search) | `src/components/layout/header/SearchBar.vue`, `src/composables/useSectionSearch.ts` |
| Change-password form | `src/components/account/ChangePasswordForm.vue` |
| Audit log page | `src/views/admin/AuditLog.vue` |
| Sidebar menus per role | `src/config/menus.ts` |

How security works on the server side, mapped to the course notes, is in
`../hairSalonManagmentSystem/docs/SECURITY.md`.
