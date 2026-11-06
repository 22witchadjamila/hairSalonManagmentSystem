<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-gray-800">Stylists</h1>
      <button v-if="session.can('STYLISTS', 'CREATE')" @click="showForm = !showForm" class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
        {{ showForm ? 'Cancel' : '+ Add Stylist' }}
      </button>
    </div>
    <SectionSearch v-model="searchQuery" class="mt-4" :total="stylists.length" :shown="shownStylists.length" placeholder="Search name, email or speciality…" />


    <form v-if="showForm" @submit.prevent="create" class="mt-4 grid grid-cols-1 gap-3 rounded-lg border border-gray-200 p-4 sm:grid-cols-2 lg:grid-cols-5">
      <input v-model="form.firstName" placeholder="First name" required :class="inputClass" />
      <input v-model="form.lastName" placeholder="Last name" required :class="inputClass" />
      <input v-model="form.email" type="email" placeholder="Email" required :class="inputClass" />
      <input v-model="form.phoneNumber" placeholder="Phone number" required :class="inputClass" />
      <input v-model="form.speciality" placeholder="Speciality" required :class="inputClass" />
      <button type="submit" :disabled="submitting" class="col-span-full rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
        {{ submitting ? 'Adding…' : 'Add Stylist' }}
      </button>
    </form>

    <p v-if="errorMessage" role="alert" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="successMessage" role="status" class="mt-3 text-sm text-success-700">{{ successMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>

    <div v-else class="mt-6 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Name</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Speciality</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Email</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Status</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Login</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <template v-for="s in shownStylists" :key="s.stylistId">
            <tr>
              <td class="px-4 py-3 font-medium text-gray-800">{{ s.firstName }} {{ s.lastName }}</td>
              <td class="px-4 py-3 text-gray-600">{{ s.speciality }}</td>
              <td class="px-4 py-3 text-gray-600">{{ s.email }}</td>
              <td class="px-4 py-3">
                <span :class="s.active ? 'bg-success-50 text-success-700' : 'bg-gray-100 text-gray-600'" class="rounded-full px-2.5 py-1 text-xs font-medium">
                  {{ s.active ? 'Active' : 'Archived' }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-600">
                <span v-if="loginFor(s.stylistId)" class="font-mono text-xs">{{ loginFor(s.stylistId) }}</span>
                <button
                  v-else-if="accountFor !== s.stylistId"
                  type="button"
                  class="text-brand-500 hover:text-brand-600"
                  @click="openAccountForm(s.stylistId)"
                >
                  Add login
                </button>
              </td>
              <td class="px-4 py-3 text-right">
                <button v-if="s.active && session.can('STYLISTS', 'DELETE')" @click="remove(s)" class="text-error-600 hover:text-error-700">Delete</button>
                <button v-else-if="!s.active && session.can('STYLISTS', 'EDIT')" @click="restore(s)" class="font-medium text-brand-600 hover:text-brand-700">Restore</button>
              </td>
            </tr>

            <!-- Inline form: credentials only. Name and email are taken from this stylist's row
                 on the server, so a login cannot be attached to the wrong person by a typo. -->
            <tr v-if="accountFor === s.stylistId" class="bg-gray-50">
              <td colspan="6" class="px-4 py-4">
                <form class="flex flex-wrap items-end gap-3" @submit.prevent="createLogin(s.stylistId)">
                  <div>
                    <label :for="'acct-user-' + s.stylistId" class="mb-1 block text-xs font-medium text-gray-600">Username</label>
                    <input :id="'acct-user-' + s.stylistId" v-model="accountForm.username" required autocomplete="off" :class="inputClass" />
                  </div>
                  <div>
                    <label :for="'acct-pass-' + s.stylistId" class="mb-1 block text-xs font-medium text-gray-600">Temporary password</label>
                    <input
                      :id="'acct-pass-' + s.stylistId"
                      v-model="accountForm.password"
                      type="password"
                      required
                      minlength="12"
                      autocomplete="new-password"
                      :class="inputClass"
                    />
                  </div>
                  <button type="submit" :disabled="accountSubmitting"
                    class="h-10 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
                    {{ accountSubmitting ? 'Creating…' : 'Create login' }}
                  </button>
                  <button type="button" class="h-10 px-2 text-sm text-gray-500 hover:text-gray-700" @click="accountFor = null">
                    Cancel
                  </button>
                  <p class="w-full text-xs text-gray-500">
                    At least 12 characters. Hand it to {{ s.firstName }} in person &mdash; it is not shown again.
                  </p>
                  <p v-if="accountError" role="alert" class="w-full text-sm text-error-600">{{ accountError }}</p>
                </form>
              </td>
            </tr>
          </template>
          <tr v-if="!shownStylists.length">
            <td colspan="6" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering">No stylists match &ldquo;{{ searchQuery.trim() }}&rdquo;. <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="resetSearch">Clear search</button></template>
              <template v-else>No stylists yet.</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { adminMenu } from '@/config/menus'
import { stylistsApi } from '@/api/stylists'
import { adminApi, type StylistLogin } from '@/api/admin'
import { ApiError } from '@/api/client'
import type { Stylist } from '@/api/types'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import { useSectionSearch } from '@/composables/useSectionSearch'
import { useSession } from '@/composables/useSession'

const inputClass =
  'h-10 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

const stylists = ref<Stylist[]>([])
const session = useSession()
// Section search - this table only. The first field is the primary one and ranks highest.
const { query: searchQuery, results: shownStylists, isFiltering, reset: resetSearch } = useSectionSearch(stylists, (s) => [
  `${s.firstName} ${s.lastName}`, s.email, s.speciality, s.phoneNumber,
])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const form = reactive({ firstName: '', lastName: '', email: '', phoneNumber: '', speciality: '' })

// --- stylist logins (admin-only API) ---
const logins = ref<StylistLogin[]>([])
const accountFor = ref<string | null>(null)
const accountForm = reactive({ username: '', password: '' })
const accountSubmitting = ref(false)
const accountError = ref('')

function loginFor(stylistId: string) {
  return logins.value.find((l) => l.stylistId === stylistId)?.username ?? null
}

function openAccountForm(stylistId: string) {
  accountFor.value = stylistId
  accountError.value = ''
  Object.assign(accountForm, { username: '', password: '' })
}

async function createLogin(stylistId: string) {
  accountSubmitting.value = true
  accountError.value = ''
  try {
    const created = await adminApi.createStylistLogin(stylistId, accountForm.username, accountForm.password)
    logins.value = [...logins.value, created]
    accountFor.value = null
    // Don't keep the password in memory any longer than needed.
    Object.assign(accountForm, { username: '', password: '' })
  } catch (err) {
    accountError.value = err instanceof ApiError ? err.message : 'Could not create the login.'
  } finally {
    accountSubmitting.value = false
  }
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    // Stylist logins are user administration - ADMIN only - so a custom role skips that call.
    ;[stylists.value, logins.value] = await Promise.all([
      stylistsApi.list(),
      session.isAdmin.value ? adminApi.stylistLogins() : Promise.resolve([] as StylistLogin[]),
    ])
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load stylists.'
  } finally {
    loading.value = false
  }
}

async function create() {
  submitting.value = true
  errorMessage.value = ''
  try {
    await stylistsApi.create({ ...form })
    Object.assign(form, { firstName: '', lastName: '', email: '', phoneNumber: '', speciality: '' })
    showForm.value = false
    await load()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not add this stylist.'
  } finally {
    submitting.value = false
  }
}

async function remove(s: Stylist) {
  if (!confirm(`Delete ${s.firstName} ${s.lastName}? If they have appointments they will be archived instead, so the history is kept.`)) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const result = await stylistsApi.delete(s.stylistId)
    successMessage.value = result.message
    if (result.outcome === 'DELETED') {
      stylists.value = stylists.value.filter((x) => x.stylistId !== s.stylistId)
    } else {
      s.active = false
    }
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not delete this stylist.'
  }
}

async function restore(s: Stylist) {
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const restored = await stylistsApi.restore(s.stylistId)
    s.active = restored.active
    successMessage.value = `${s.firstName} ${s.lastName} can be booked again.`
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not restore this stylist.'
  }
}
</script>
