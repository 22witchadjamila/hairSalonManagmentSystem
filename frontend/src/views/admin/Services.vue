<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-gray-800">Services</h1>
      <button v-if="session.can('SERVICES', 'CREATE')" @click="showForm ? closeForm() : openCreateForm()" class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
        {{ showForm ? 'Cancel' : '+ Add Service' }}
      </button>
    </div>
    <SectionSearch v-model="searchQuery" class="mt-4" :total="services.length" :shown="shownServices.length" placeholder="Search name or category…" />


    <form v-if="showForm" @submit.prevent="save" class="mt-4 grid grid-cols-1 gap-3 rounded-lg border border-gray-200 p-4 sm:grid-cols-2 lg:grid-cols-5">
      <input v-model="form.name" placeholder="Name" required :class="inputClass" />
      <input v-model="form.description" placeholder="Description" :class="inputClass" />
      <input v-model.number="form.durationMinutes" type="number" min="1" placeholder="Duration (min)" required :class="inputClass" />
      <input v-model.number="form.price" type="number" min="0.01" step="0.01" placeholder="Price (R)" required :class="inputClass" />
      <select v-model="form.category" required :class="inputClass">
        <option value="" disabled>Category</option>
        <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
      </select>
      <button type="submit" :disabled="submitting" class="col-span-full rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
        {{ submitting ? 'Saving…' : editingId ? 'Save Changes' : 'Add Service' }}
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
            <th class="px-4 py-3 text-left font-medium text-gray-500">Category</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Duration</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Price</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Status</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="s in shownServices" :key="s.serviceId">
            <td class="px-4 py-3 font-medium text-gray-800">{{ s.name }}</td>
            <td class="px-4 py-3 text-gray-600">{{ s.category }}</td>
            <td class="px-4 py-3 text-gray-600">{{ s.durationMinutes }} min</td>
            <td class="px-4 py-3 text-gray-600">R{{ s.price.toFixed(2) }}</td>
            <td class="px-4 py-3">
              <span :class="s.active ? 'bg-success-50 text-success-700' : 'bg-gray-100 text-gray-600'" class="rounded-full px-2.5 py-1 text-xs font-medium">
                {{ s.active ? 'Active' : 'Archived' }}
              </span>
            </td>
            <td class="px-4 py-3 text-right space-x-3">
              <button v-if="session.can('SERVICES', 'EDIT')" @click="openEditForm(s)" class="font-medium text-brand-600 hover:text-brand-700">Edit</button>
              <button v-if="s.active && session.can('SERVICES', 'DELETE')" @click="remove(s)" class="text-error-600 hover:text-error-700">Delete</button>
              <button v-else-if="!s.active && session.can('SERVICES', 'EDIT')" @click="restore(s)" class="font-medium text-brand-600 hover:text-brand-700">Restore</button>
            </td>
          </tr>
          <tr v-if="!shownServices.length">
            <td colspan="6" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering">No services match &ldquo;{{ searchQuery.trim() }}&rdquo;. <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="resetSearch">Clear search</button></template>
              <template v-else>No services yet.</template>
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
import { servicesApi } from '@/api/services'
import { ApiError } from '@/api/client'
import type { SalonService, ServiceCategory } from '@/api/types'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import { useSectionSearch } from '@/composables/useSectionSearch'
import { useSession } from '@/composables/useSession'

const inputClass =
  'h-10 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

const categories: ServiceCategory[] = ['HAIRCUT', 'COLOURING', 'STYLING', 'TREATMENT', 'EXTENSIONS', 'OTHER']

const services = ref<SalonService[]>([])
const session = useSession()
// Section search - this table only. The first field is the primary one and ranks highest.
const { query: searchQuery, results: shownServices, isFiltering, reset: resetSearch } = useSectionSearch(services, (s) => [
  s.name, s.category,
])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const editingId = ref<string | null>(null)
const errorMessage = ref('')
const successMessage = ref('')

const emptyForm = { name: '', description: '', durationMinutes: 30, price: 0, category: '' as ServiceCategory | '' }
const form = reactive({ ...emptyForm })

onMounted(load)

async function load() {
  loading.value = true
  try {
    services.value = await servicesApi.list()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load services.'
  } finally {
    loading.value = false
  }
}

function openCreateForm() {
  editingId.value = null
  Object.assign(form, emptyForm)
  showForm.value = true
}

function openEditForm(s: SalonService) {
  editingId.value = s.serviceId
  Object.assign(form, {
    name: s.name,
    description: s.description ?? '',
    durationMinutes: s.durationMinutes,
    price: s.price,
    category: s.category,
  })
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
}

async function save() {
  if (!form.category) return
  submitting.value = true
  errorMessage.value = ''
  try {
    if (editingId.value) {
      await servicesApi.update(editingId.value, { ...form, category: form.category })
    } else {
      await servicesApi.create({ ...form, category: form.category })
    }
    closeForm()
    await load()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not save this service.'
  } finally {
    submitting.value = false
  }
}

async function remove(s: SalonService) {
  if (!confirm(`Delete ${s.name}? If it has bookings it will be archived instead, so its history is kept.`)) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    // The server deletes an unused service, archives a booked one, and says which.
    const result = await servicesApi.delete(s.serviceId)
    successMessage.value = result.message
    if (result.outcome === 'DELETED') {
      services.value = services.value.filter((x) => x.serviceId !== s.serviceId)
    } else {
      s.active = false
    }
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not delete this service.'
  }
}

async function restore(s: SalonService) {
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const restored = await servicesApi.restore(s.serviceId)
    s.active = restored.active
    successMessage.value = `${s.name} is active again and visible to customers.`
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not restore this service.'
  }
}
</script>
