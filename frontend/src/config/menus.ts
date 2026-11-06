// Sidebar menu definitions, one per dashboard section. Each dashboard page passes the
// matching menu into <AdminLayout :menu-groups="...">, so the sidebar always reflects
// which area of the app you're in (Customer / Stylist / Admin) instead of one global menu.
import {
  GridIcon,
  CalenderIcon,
  UserCircleIcon,
  UserGroupIcon,
  ChatIcon,
  DocsIcon,
  TaskIcon,
  BarChartIcon,
  FlagIcon,
  BoxCubeIcon,
  BellIcon,
  SettingsIcon,
  ListIcon,
  TableIcon,
  PageIcon,
  PieChartIcon,
  PlugInIcon,
} from '@/icons'

export const customerMenu = [
  {
    title: 'My Account',
    items: [
      { icon: GridIcon, name: 'Dashboard', path: '/customer' },
      { icon: TaskIcon, name: 'My Appointments', path: '/customer/appointments' },
      { icon: FlagIcon, name: 'Loyalty Rewards', path: '/customer/loyalty' },
      { icon: ChatIcon, name: 'My Feedback', path: '/customer/feedback' },
      { icon: UserCircleIcon, name: 'Profile', path: '/customer/profile' },
    ],
  },
]

export const stylistMenu = [
  {
    title: 'My Work',
    items: [
      { icon: GridIcon, name: 'Dashboard', path: '/stylist' },
      { icon: CalenderIcon, name: 'My Schedule', path: '/stylist/schedule' },
      { icon: TaskIcon, name: 'Appointments', path: '/stylist/appointments' },
      { icon: UserCircleIcon, name: 'Profile', path: '/stylist/profile' },
    ],
  },
]

// module / adminOnly let AdminLayout hide what a custom staff role can't open (the router and
// the server enforce the same rules - hiding a link is only so people aren't shown dead ends).
export const adminMenu = [
  {
    title: 'Overview',
    items: [{ icon: GridIcon, name: 'Dashboard', path: '/admin', adminOnly: true }],
  },
  {
    title: 'Salon Operations',
    items: [
      { icon: UserGroupIcon, name: 'Customers', path: '/admin/customers', module: 'CUSTOMERS' },
      { icon: UserCircleIcon, name: 'Stylists', path: '/admin/stylists', module: 'STYLISTS' },
      { icon: DocsIcon, name: 'Services', path: '/admin/services', module: 'SERVICES' },
      { icon: TaskIcon, name: 'Appointments', path: '/admin/appointments', module: 'APPOINTMENTS' },
    ],
  },
  {
    title: 'Business',
    items: [
      { icon: BarChartIcon, name: 'Payments', path: '/admin/payments', module: 'PAYMENTS' },
      { icon: FlagIcon, name: 'Promotions', path: '/admin/promotions', module: 'PROMOTIONS' },
      { icon: BoxCubeIcon, name: 'Inventory', path: '/admin/inventory', module: 'INVENTORY' },
      { icon: ChatIcon, name: 'Feedback', path: '/admin/feedback', module: 'FEEDBACK' },
      { icon: BellIcon, name: 'Notifications', path: '/admin/notifications', module: 'NOTIFICATIONS' },
    ],
  },
  {
    title: 'System',
    items: [
      { icon: SettingsIcon, name: 'Users & Roles', path: '/admin/users', adminOnly: true },
      { icon: ListIcon, name: 'Audit Log', path: '/admin/audit', adminOnly: true },
    ],
  },
]

// The original TailAdmin demo menu, kept only as AppSidebar's fallback default so the
// component still renders something sensible if a page forgets to pass menuGroups.
export const defaultMenu = [
  {
    title: 'Menu',
    items: [
      { icon: GridIcon, name: 'Dashboard', subItems: [{ name: 'Ecommerce', path: '/', pro: false }] },
      { icon: CalenderIcon, name: 'Calendar', path: '/calendar' },
      { icon: UserCircleIcon, name: 'User Profile', path: '/profile' },
      { name: 'Forms', icon: ListIcon, subItems: [{ name: 'Form Elements', path: '/form-elements', pro: false }] },
      { name: 'Tables', icon: TableIcon, subItems: [{ name: 'Basic Tables', path: '/basic-tables', pro: false }] },
      {
        name: 'Pages',
        icon: PageIcon,
        subItems: [
          { name: 'Blank Page', path: '/blank', pro: false },
          { name: '404 Page', path: '/error-404', pro: false },
        ],
      },
    ],
  },
  {
    title: 'Others',
    items: [
      {
        icon: PieChartIcon,
        name: 'Charts',
        subItems: [
          { name: 'Line Chart', path: '/line-chart', pro: false },
          { name: 'Bar Chart', path: '/bar-chart', pro: false },
        ],
      },
      { icon: PlugInIcon, name: 'Authentication', subItems: [{ name: 'Signin', path: '/login', pro: false }] },
    ],
  },
]

// Public marketing site nav (used by PublicLayout's header, not the sidebar).
export const publicNav = [
  { name: 'Home', path: '/' },
  { name: 'About', path: '/about' },
  { name: 'Services', path: '/services' },
  { name: 'Stylists', path: '/stylists' },
  { name: 'Products', path: '/products' },
  { name: 'Gallery', path: '/gallery' },
  { name: 'Contact', path: '/contact' },
]
