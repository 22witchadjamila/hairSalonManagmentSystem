<template>
  <AdminLayout :menu-groups="adminMenu">
    <h1 class="text-2xl font-semibold text-gray-800">Users &amp; Roles</h1>
    <p class="mt-1 text-sm text-gray-500">
      Sign-in accounts and their roles. Customers create their own accounts when they sign up; stylist
      logins are added from the Stylists page. Staff logins are either an admin or a custom role, which
      reaches only the areas ticked for it.
    </p>

    <p v-if="errorMessage" role="alert" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="successMessage" role="status" class="mt-3 text-sm text-success-700">{{ successMessage }}</p>

    <ChangePasswordForm class="mt-6" />

    <!-- Roles -->
    <div class="mt-8 flex items-center justify-between">
      <h2 class="text-lg font-semibold text-gray-800">Roles</h2>
    </div>

    <form v-if="showRoleForm" class="mt-3 rounded-lg border border-gray-200 p-4" @submit.prevent="saveRole">
      <p class="text-sm font-medium text-gray-800">{{ editingRole ? `Edit ${editingRole.name}` : 'New role' }}</p>
      <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div>
          <label for="role-name" class="mb-1 block text-xs text-gray-500">Name</label>
          <input
            id="role-name"
            v-model="roleForm.name"
            placeholder="e.g. RECEPTION"
            :disabled="!!editingRole"
            required
            :class="inputClass"
          />
        </div>
        <div>
          <label for="role-desc" class="mb-1 block text-xs text-gray-500">Description</label>
          <input id="role-desc" v-model="roleForm.description" placeholder="What this role is for" :class="inputClass" />
        </div>
      </div>

      <!-- Module x action grid. Users & Roles is never offered: a role that could manage roles
           could give itself anything. Ticking any action also grants View on that module. -->
      <fieldset class="mt-4">
        <legend class="text-xs text-gray-500">
          Permissions &mdash; a new role starts with none. Ticking Create, Edit or Delete also ticks View.
        </legend>
        <div class="mt-2 overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr>
                <th class="py-2 pr-4 text-left font-medium text-gray-500">Area</th>
                <th v-for="a in catalogue.actions" :key="a" class="px-3 py-2 text-center font-medium text-gray-500">
                  {{ ACTION_LABEL[a] ?? a }}
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="m in catalogue.modules" :key="m.key">
                <td class="py-2 pr-4 text-gray-700">{{ m.label }}</td>
                <td v-for="a in catalogue.actions" :key="a" class="px-3 py-2 text-center">
                  <input
                    type="checkbox"
                    class="h-4 w-4"
                    :aria-label="`${m.label}: ${ACTION_LABEL[a] ?? a}`"
                    :checked="roleForm.permissions.has(`${m.key}:${a}`)"
                    @change="toggle(m.key, a, ($event.target as HTMLInputElement).checked)"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </fieldset>

      <div class="mt-4 flex gap-2">
        <button
          type="submit"
          :disabled="submittingRole"
          class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
        >
          {{ submittingRole ? 'Saving…' : editingRole ? 'Save role' : 'Add role' }}
        </button>
        <button type="button" class="px-3 text-sm text-gray-500 hover:text-gray-700" @click="closeRoleForm">Cancel</button>
      </div>
    </form>

    <div class="mt-3 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Role</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Access</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Logins</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="r in roles" :key="r.roleId">
            <td class="px-4 py-3">
              <span class="font-medium text-gray-800">{{ r.name }}</span>
              <span v-if="r.description" class="block text-xs text-gray-500">{{ r.description }}</span>
            </td>
            <td class="px-4 py-3 text-xs text-gray-600">{{ describeAccess(r) }}</td>
            <td class="px-4 py-3 tabular-nums text-gray-600">{{ loginCount(r) }}</td>
            <td class="space-x-3 whitespace-nowrap px-4 py-3 text-right">
              <span v-if="isBuiltIn(r)" class="text-xs text-gray-400">Built-in</span>
              <template v-else>
                <button class="font-medium text-brand-600 hover:text-brand-700" @click="openRoleForm(r)">Edit</button>
                <button class="text-error-600 hover:text-error-700" @click="removeRole(r)">Delete</button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Users -->
    <div class="mt-10 flex items-center justify-between">
      <h2 class="text-lg font-semibold text-gray-800">Users</h2>
      <button class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600" @click="showUserForm = !showUserForm">
        {{ showUserForm ? 'Cancel' : '+ Add Staff Login' }}
      </button>
    </div>
    <!-- Staff logins only: a customer login needs a customer record (made at sign-up) and a
         stylist login needs a stylist record (made on the Stylists page). -->
    <form v-if="showUserForm" class="mt-3 grid grid-cols-1 gap-3 rounded-lg border border-gray-200 p-4 sm:grid-cols-2 lg:grid-cols-3" @submit.prevent="createUser">
      <input v-model="userForm.username" placeholder="Username" required autocomplete="off" :class="inputClass" />
      <input v-model="userForm.password" type="password" placeholder="Password (12+ characters)" minlength="12" required autocomplete="new-password" :class="inputClass" />
      <input v-model="userForm.email" type="email" placeholder="Email" required :class="inputClass" />
      <input v-model="userForm.firstName" placeholder="First name" required :class="inputClass" />
      <input v-model="userForm.lastName" placeholder="Last name" required :class="inputClass" />
      <div>
        <label for="user-role" class="sr-only">Role</label>
        <select id="user-role" v-model="userForm.roleId" :class="inputClass">
          <option value="">Admin (full access)</option>
          <option v-for="r in customRoles" :key="r.roleId" :value="r.roleId">{{ r.name }}</option>
        </select>
      </div>
      <button type="submit" :disabled="submittingUser" class="col-span-full rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
        {{ submittingUser ? 'Adding…' : 'Add staff login' }}
      </button>
    </form>

    <SectionSearch v-model="searchQuery" class="mt-4" :total="users.length" :shown="shownUsers.length" placeholder="Search username, name, email or role…" />

    <!-- Recovery route for a forgotten password: nothing sends email, so an admin sets a new one. -->
    <form v-if="resetFor" class="mt-4 flex flex-wrap items-end gap-3 rounded-lg border border-gray-200 p-4" @submit.prevent="doReset">
      <p class="w-full text-sm font-medium text-gray-800">Set a new password for {{ resetFor.username }}</p>
      <div class="min-w-64 flex-1">
        <label for="reset-pw" class="mb-1 block text-xs text-gray-500">New password</label>
        <input id="reset-pw" v-model="resetPw" type="text" autocomplete="off" required :class="inputClass" />
      </div>
      <button type="submit" :disabled="resetting || !resetPw" class="h-10 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
        {{ resetting ? 'Saving…' : 'Set password' }}
      </button>
      <button type="button" class="h-10 px-3 text-sm text-gray-500 hover:text-gray-700" @click="resetFor = null">Cancel</button>
      <p class="w-full text-xs text-gray-500">
        Staff passwords need 12+ characters, customers 8+, each with a letter and a number. Give it to the person in
        person - it is not shown again - and ask them to change it from their profile. They are signed out now.
      </p>
    </form>

    <div class="mt-4 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Username</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Name</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Email</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Role</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="u in shownUsers" :key="u.userId">
            <td class="px-4 py-3 font-medium text-gray-800">{{ u.username }}</td>
            <td class="px-4 py-3 text-gray-600">{{ u.firstName }} {{ u.lastName }}</td>
            <td class="px-4 py-3 text-gray-600">{{ u.email }}</td>
            <td class="px-4 py-3 text-gray-600">
              <!-- Staff logins can move between ADMIN and custom roles; customer and stylist
                   logins keep theirs. You can't change your own role. -->
              <select
                v-if="isStaffLogin(u) && !isMe(u)"
                :value="u.role?.roleId"
                :aria-label="`Role for ${u.username}`"
                class="h-9 rounded-lg border border-gray-300 bg-transparent px-2 text-sm"
                @change="changeRole(u, ($event.target as HTMLSelectElement).value)"
              >
                <option v-for="r in staffRoles" :key="r.roleId" :value="r.roleId">{{ r.name }}</option>
              </select>
              <span v-else>{{ u.role?.name ?? '—' }}</span>
            </td>
            <td class="px-4 py-3 text-right">
              <!-- The server refuses to delete your own account; don't offer it. -->
              <span v-if="isMe(u)" class="text-xs text-gray-400">You</span>
              <template v-else>
                <button class="mr-3 font-medium text-brand-600 hover:text-brand-700" @click="openReset(u)">Reset password</button>
                <button class="text-error-600 hover:text-error-700" @click="removeUser(u)">Delete</button>
              </template>
            </td>
          </tr>
          <tr v-if="!shownUsers.length">
            <td colspan="5" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering">
                No users match &ldquo;{{ searchQuery.trim() }}&rdquo;.
                <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="resetSearch">Clear search</button>
              </template>
              <template v-else>No users yet.</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import ChangePasswordForm from '@/components/account/ChangePasswordForm.vue'
import { adminMenu } from '@/config/menus'
import { usersApi } from '@/api/users'
import { rolesApi, BUILT_IN_ROLES, type PermissionCatalogue } from '@/api/roles'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import { useSectionSearch } from '@/composables/useSectionSearch'
import type { Role, User } from '@/api/types'

const inputClass =
  'h-10 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 disabled:bg-gray-50 disabled:text-gray-500'

const ACTION_LABEL: Record<string, string> = { VIEW: 'View', CREATE: 'Create', EDIT: 'Edit', DELETE: 'Delete' }
const BUILT_IN_ACCESS: Record<string, string> = {
  ADMIN: 'Everything, including users and roles',
  CUSTOMER: 'Their own bookings, feedback and loyalty',
  STYLIST: 'Their own schedule and appointments',
}

const users = ref<User[]>([])
const roles = ref<Role[]>([])
const catalogue = ref<PermissionCatalogue>({ modules: [], actions: [] })
const errorMessage = ref('')
const successMessage = ref('')
const session = useSession()

const { query: searchQuery, results: shownUsers, isFiltering, reset: resetSearch } = useSectionSearch(users, (u) => [
  u.username,
  `${u.firstName} ${u.lastName}`,
  u.email,
  u.role?.name,
])

const isBuiltIn = (r: Role) => BUILT_IN_ROLES.includes(r.name)
const customRoles = computed(() => roles.value.filter((r) => !isBuiltIn(r)))
/** Roles a staff login can be moved to. */
const staffRoles = computed(() => roles.value.filter((r) => r.name === 'ADMIN' || !isBuiltIn(r)))
const isStaffLogin = (u: User) => !!u.role && (u.role.name === 'ADMIN' || !isBuiltIn(u.role))
const isMe = (u: User) => u.username === session.user.value?.username
const loginCount = (r: Role) => users.value.filter((u) => u.role?.roleId === r.roleId).length

function describeAccess(r: Role): string {
  if (isBuiltIn(r)) return BUILT_IN_ACCESS[r.name] ?? ''
  if (!r.permissions?.length) return 'No access yet'
  return catalogue.value.modules
    .map((m) => {
      const actions = catalogue.value.actions.filter((a) => r.permissions.includes(`${m.key}:${a}`))
      return actions.length ? `${m.label} (${actions.map((a) => (ACTION_LABEL[a] ?? a).toLowerCase()).join(', ')})` : ''
    })
    .filter(Boolean)
    .join(' · ')
}

// ---------------------------------------------------------------- roles

const showRoleForm = ref(false)
const submittingRole = ref(false)
const editingRole = ref<Role | null>(null)
const roleForm = reactive({ name: '', description: '', permissions: new Set<string>() })

function openRoleForm(r?: Role) {
  editingRole.value = r ?? null
  roleForm.name = r?.name ?? ''
  roleForm.description = r?.description ?? ''
  roleForm.permissions = new Set(r?.permissions ?? [])
  showRoleForm.value = true
}

function closeRoleForm() {
  showRoleForm.value = false
  editingRole.value = null
}

/** Mirrors the server: any action implies View; removing View removes the rest of that module. */
function toggle(module: string, action: string, on: boolean) {
  const next = new Set(roleForm.permissions)
  if (on) {
    next.add(`${module}:${action}`)
    next.add(`${module}:VIEW`)
  } else if (action === 'VIEW') {
    catalogue.value.actions.forEach((a) => next.delete(`${module}:${a}`))
  } else {
    next.delete(`${module}:${action}`)
  }
  roleForm.permissions = next
}

async function saveRole() {
  submittingRole.value = true
  errorMessage.value = ''
  successMessage.value = ''
  const payload = {
    name: roleForm.name,
    description: roleForm.description,
    permissions: [...roleForm.permissions],
  }
  try {
    if (editingRole.value) {
      await rolesApi.update(editingRole.value.roleId, payload)
      successMessage.value = `Role ${editingRole.value.name} saved. Its users get the new access on their next action.`
    } else {
      const created = await rolesApi.create(payload)
      successMessage.value = payload.permissions.length
        ? `Role ${created.name} added.`
        : `Role ${created.name} added with no access yet - edit it to choose what it can do.`
    }
    closeRoleForm()
    await loadRoles()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not save this role.'
  } finally {
    submittingRole.value = false
  }
}

async function removeRole(r: Role) {
  if (!confirm(`Delete the ${r.name} role?`)) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await rolesApi.delete(r.roleId)
    roles.value = roles.value.filter((x) => x.roleId !== r.roleId)
    successMessage.value = `Role ${r.name} deleted.`
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not delete this role.'
  }
}

// ---------------------------------------------------------------- users

const showUserForm = ref(false)
const submittingUser = ref(false)
const emptyUser = () => ({ username: '', password: '', email: '', firstName: '', lastName: '', roleId: '' })
const userForm = reactive(emptyUser())

onMounted(async () => {
  await Promise.all([loadUsers(), loadRoles(), loadCatalogue()])
})

async function loadUsers() {
  try {
    users.value = await usersApi.list()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load users.'
  }
}

async function loadRoles() {
  try {
    roles.value = await rolesApi.list()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load roles.'
  }
}

async function loadCatalogue() {
  try {
    catalogue.value = await rolesApi.permissions()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load the permission list.'
  }
}

async function createUser() {
  submittingUser.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const created = await usersApi.create({ ...userForm, roleId: userForm.roleId || null })
    successMessage.value = `Login ${created.username} created as ${created.role?.name ?? 'ADMIN'}.`
    Object.assign(userForm, emptyUser())
    showUserForm.value = false
    await loadUsers()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not add this user.'
  } finally {
    submittingUser.value = false
  }
}

async function changeRole(u: User, roleId: string) {
  if (roleId === u.role?.roleId) return
  const target = roles.value.find((r) => r.roleId === roleId)
  errorMessage.value = ''
  successMessage.value = ''
  if (!confirm(`Move ${u.username} to ${target?.name}? They'll be signed out and get the new access when they sign back in.`)) {
    await loadUsers() // put the select back
    return
  }
  try {
    await usersApi.changeRole(u.userId, roleId)
    successMessage.value = `${u.username} is now ${target?.name}.`
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not change this role.'
  }
  await loadUsers()
}

const resetFor = ref<User | null>(null)
const resetPw = ref('')
const resetting = ref(false)

function openReset(u: User) {
  resetFor.value = u
  resetPw.value = ''
  errorMessage.value = ''
  successMessage.value = ''
}

async function doReset() {
  if (!resetFor.value) return
  resetting.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await usersApi.resetPassword(resetFor.value.userId, resetPw.value)
    successMessage.value = `${resetFor.value.username}'s password was reset and they were signed out.`
    resetFor.value = null
    resetPw.value = ''
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not reset this password.'
  } finally {
    resetting.value = false
  }
}

async function removeUser(u: User) {
  if (!confirm(`Delete the login ${u.username}? Their bookings and records are kept.`)) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await usersApi.delete(u.userId)
    users.value = users.value.filter((x) => x.userId !== u.userId)
    successMessage.value = `Login ${u.username} deleted.`
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not delete this user.'
  }
}
</script>
