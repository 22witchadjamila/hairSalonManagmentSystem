import { createRouter, createWebHistory } from 'vue-router'
import { useSession } from '@/composables/useSession'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { left: 0, top: 0 }
  },
  routes: [
    // ---------- Public (marketing) pages - PublicLayout ----------
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/public/Home.vue'),
      meta: { title: 'Home', section: 'public' },
    },
    {
      path: '/about',
      name: 'About',
      component: () => import('../views/public/About.vue'),
      meta: { title: 'About', section: 'public' },
    },
    {
      path: '/services',
      name: 'Services',
      component: () => import('../views/public/Services.vue'),
      meta: { title: 'Services', section: 'public' },
    },
    {
      path: '/stylists',
      name: 'PublicStylists',
      component: () => import('../views/public/Stylists.vue'),
      meta: { title: 'Stylists', section: 'public' },
    },
    {
      path: '/products',
      name: 'Products',
      component: () => import('../views/public/Products.vue'),
      meta: { title: 'Products', section: 'public' },
    },
    {
      path: '/gallery',
      name: 'Gallery',
      component: () => import('../views/public/Gallery.vue'),
      meta: { title: 'Gallery', section: 'public' },
    },
    {
      path: '/contact',
      name: 'Contact',
      component: () => import('../views/public/Contact.vue'),
      meta: { title: 'Contact', section: 'public' },
    },
    {
      path: '/book-appointment',
      name: 'BookAppointment',
      component: () => import('../views/public/BookAppointment.vue'),
      // requiresAuth drives the guard below. The server enforces the same rule on
      // POST /api/appointments/book - this only saves the visitor from filling in a
      // form that was always going to be rejected.
      meta: { title: 'Book Appointment', section: 'public', requiresAuth: true },
    },

    // ---------- Auth - FullScreenLayout (own full-page template) ----------
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Auth/Signin.vue'),
      meta: { title: 'Login', section: 'auth' },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Auth/Signup.vue'),
      meta: { title: 'Register', section: 'auth' },
    },

    // ---------- Customer dashboard - AdminLayout + customerMenu ----------
    {
      path: '/customer',
      name: 'CustomerDashboard',
      component: () => import('../views/customer/Dashboard.vue'),
      meta: { title: 'My Dashboard', section: 'customer' },
    },
    {
      path: '/customer/appointments',
      name: 'CustomerAppointments',
      component: () => import('../views/customer/Appointments.vue'),
      meta: { title: 'My Appointments', section: 'customer' },
    },
    {
      path: '/customer/loyalty',
      name: 'CustomerLoyalty',
      component: () => import('../views/customer/Loyalty.vue'),
      meta: { title: 'Loyalty Rewards', section: 'customer' },
    },
    {
      path: '/customer/feedback',
      name: 'CustomerFeedback',
      component: () => import('../views/customer/Feedback.vue'),
      meta: { title: 'My Feedback', section: 'customer' },
    },
    {
      path: '/customer/profile',
      name: 'CustomerProfile',
      component: () => import('../views/customer/Profile.vue'),
      meta: { title: 'Profile', section: 'customer' },
    },

    // ---------- Stylist dashboard - AdminLayout + stylistMenu ----------
    {
      path: '/stylist',
      name: 'StylistDashboard',
      component: () => import('../views/stylist/Dashboard.vue'),
      meta: { title: 'My Dashboard', section: 'stylist' },
    },
    {
      path: '/stylist/schedule',
      name: 'StylistSchedule',
      component: () => import('../views/stylist/Schedule.vue'),
      meta: { title: 'My Schedule', section: 'stylist' },
    },
    {
      path: '/stylist/appointments',
      name: 'StylistAppointments',
      component: () => import('../views/stylist/Appointments.vue'),
      meta: { title: 'Appointments', section: 'stylist' },
    },
    {
      path: '/stylist/profile',
      name: 'StylistProfile',
      component: () => import('../views/stylist/Profile.vue'),
      meta: { title: 'Profile', section: 'stylist' },
    },

    // ---------- Admin dashboard - AdminLayout + adminMenu ----------
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: () => import('../views/admin/Dashboard.vue'),
      meta: { title: 'Admin Dashboard', section: 'admin' },
    },
    {
      path: '/admin/customers',
      name: 'AdminCustomers',
      component: () => import('../views/admin/Customers.vue'),
      meta: { title: 'Customers', section: 'admin', module: 'CUSTOMERS' },
    },
    {
      path: '/admin/stylists',
      name: 'AdminStylists',
      component: () => import('../views/admin/Stylists.vue'),
      meta: { title: 'Stylists', section: 'admin', module: 'STYLISTS' },
    },
    {
      path: '/admin/services',
      name: 'AdminServices',
      component: () => import('../views/admin/Services.vue'),
      meta: { title: 'Services', section: 'admin', module: 'SERVICES' },
    },
    {
      path: '/admin/appointments',
      name: 'AdminAppointments',
      component: () => import('../views/admin/Appointments.vue'),
      meta: { title: 'Appointments', section: 'admin', module: 'APPOINTMENTS' },
    },
    {
      path: '/admin/payments',
      name: 'AdminPayments',
      component: () => import('../views/admin/Payments.vue'),
      meta: { title: 'Payments', section: 'admin', module: 'PAYMENTS' },
    },
    {
      path: '/admin/promotions',
      name: 'AdminPromotions',
      component: () => import('../views/admin/Promotions.vue'),
      meta: { title: 'Promotions', section: 'admin', module: 'PROMOTIONS' },
    },
    {
      path: '/admin/inventory',
      name: 'AdminInventory',
      component: () => import('../views/admin/Inventory.vue'),
      meta: { title: 'Inventory', section: 'admin', module: 'INVENTORY' },
    },
    {
      path: '/admin/feedback',
      name: 'AdminFeedback',
      component: () => import('../views/admin/Feedback.vue'),
      meta: { title: 'Feedback', section: 'admin', module: 'FEEDBACK' },
    },
    {
      path: '/admin/notifications',
      name: 'AdminNotifications',
      component: () => import('../views/admin/Notifications.vue'),
      meta: { title: 'Notifications', section: 'admin', module: 'NOTIFICATIONS' },
    },
    {
      path: '/admin/users',
      name: 'AdminUsers',
      component: () => import('../views/admin/Users.vue'),
      meta: { title: 'Users & Roles', section: 'admin' },
    },
    {
      // No `module`: the audit log is admin-only and can't be granted to a custom role.
      path: '/admin/audit',
      name: 'AdminAudit',
      component: () => import('../views/admin/AuditLog.vue'),
      meta: { title: 'Audit Log', section: 'admin' },
    },

    // The chapter's accessDenied.jsp: a signed-in person opened a page that belongs to another role.
    {
      path: '/access-denied',
      name: 'AccessDenied',
      component: () => import('../views/Errors/AccessDenied.vue'),
      meta: { title: 'Access denied', section: 'public' },
    },
    // A custom staff role that has not been given any permissions yet.
    {
      path: '/staff/no-access',
      name: 'StaffNoAccess',
      component: () => import('../views/admin/NoAccess.vue'),
      meta: { title: 'No access yet', section: 'staff' },
    },
    // ---------- Fallback ----------
    {
      path: '/:pathMatch(.*)*',
      name: '404 Error',
      component: () => import('../views/Errors/FourZeroFour.vue'),
      meta: { title: '404 Error', section: 'public' },
    },
  ],
})

export default router

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title} | Judith Hair Salon`
  next()
})

/**
 * Chapter 9 Section C, <intercept-url>, applied in the browser.
 *
 * This is CONVENIENCE, not security. A guard in JavaScript can be bypassed by anyone with
 * developer tools, which is precisely why SecurityConfig enforces the same rules server-side -
 * a customer who forces their way onto /admin sees an empty dashboard, because every admin API
 * call answers 401/403. What the guard buys is a sensible experience: you are sent to the right
 * place BEFORE a page loads and fails.
 *
 * Each dashboard section maps to the one role allowed in it, mirroring hasRole() on the server.
 * Admin pages also name their permission module (meta.module), so custom staff roles reach
 * exactly the pages their permissions cover - mirroring PermissionPolicy on the server.
 */
const ROLE_FOR_SECTION: Record<string, string> = {
  customer: 'CUSTOMER',
  stylist: 'STYLIST',
  admin: 'ADMIN',
}

router.beforeEach(async (to) => {
  const section = to.meta.section as string | undefined
  const staffArea = section === 'admin' || section === 'staff'
  const requiredRole = section ? ROLE_FOR_SECTION[section] : undefined
  // The booking page is public-facing but customer-only.
  const neededRole = requiredRole ?? (to.meta.requiresAuth ? 'CUSTOMER' : undefined)
  if (!neededRole && !staffArea) return true

  const session = useSession()
  // On a cold load (deep link, refresh) we have not asked the server yet.
  if (!session.ready.value) await session.refresh()

  if (!session.isAuthenticated.value) {
    // Booking sends newcomers to sign-up; staff dashboards to login, since staff never self-register.
    const path = to.meta.requiresAuth ? '/register' : '/login'
    return { path, query: { redirect: to.fullPath } }
  }

  // Custom staff roles (e.g. RECEPTION) may open an admin page only if their role can VIEW that
  // page's module. Pages with no module - the dashboard, Users & Roles - stay ADMIN-only.
  if (staffArea && session.isCustomRole.value) {
    const module = to.meta.module as string | undefined
    if (section === 'staff' || (module && session.can(module))) return true
    return to.path === session.home.value ? true : { path: '/access-denied', query: { from: to.fullPath } }
  }
  if (section === 'staff') return session.home.value

  if (session.user.value?.role !== neededRole) {
    // Signed in, wrong role: the chapter's accessDenied.jsp, with a button to the dashboard they DO
    // belong on. (Silently bouncing them there left people wondering why the link did nothing.)
    return { path: '/access-denied', query: { from: to.fullPath } }
  }
  return true
})

/**
 * The server ended the session underneath an open page: it timed out, or this account signed in on
 * another device (only one session per account is allowed). Forget the user and explain at sign-in.
 */
window.addEventListener('session-expired', () => {
  const session = useSession()
  if (!session.isAuthenticated.value) return   // already signed out - nothing to explain
  session.endLocally()
  router.push({ path: '/login', query: { expired: '1', redirect: router.currentRoute.value.fullPath } })
})
