<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-gray-800">Appointments</h1>
      <select v-model="statusFilter" class="h-10 rounded-lg border border-gray-300 bg-transparent px-3 text-sm">
        <option value="">All statuses</option>
        <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
      </select>
    </div>
    <!-- Works together with the status filter: it searches within the chosen status. -->
    <SectionSearch v-model="searchQuery" class="mt-4" :total="filtered.length" :shown="shownAppointments.length" placeholder="Search customer, stylist, service, date…" />


    <p v-if="errorMessage" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>

    <div v-else class="mt-6 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Customer</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Stylist</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Service</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">When</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Status</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="a in shownAppointments" :key="a.appointmentId">
            <td class="px-4 py-3 font-medium text-gray-800">{{ a.customer.firstName }} {{ a.customer.lastName }}</td>
            <td class="px-4 py-3 text-gray-600">{{ a.stylist.firstName }} {{ a.stylist.lastName }}</td>
            <td class="px-4 py-3 text-gray-600">{{ a.salonService.name }}</td>
            <td class="px-4 py-3 text-gray-600">{{ a.appointmentDate }} {{ a.startTime }}</td>
            <td class="px-4 py-3">
              <span class="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-medium text-gray-600">{{ a.status }}</span>
            </td>
            <td class="px-4 py-3 text-right space-x-3">
              <button
                v-if="a.status === 'PENDING' && session.can('APPOINTMENTS', 'EDIT')"
                @click="confirm(a)"
                class="font-medium text-brand-600 hover:text-brand-700"
              >Confirm</button>
              <button
                v-if="(a.status === 'PENDING' || a.status === 'CONFIRMED') && session.can('APPOINTMENTS', 'EDIT')"
                @click="complete(a)"
                class="text-success-600 hover:text-success-700"
              >Complete</button>
              <button
                v-if="(a.status === 'PENDING' || a.status === 'CONFIRMED') && session.can('APPOINTMENTS', 'EDIT')"
                @click="cancel(a)"
                class="text-error-600 hover:text-error-700"
              >Cancel</button>
            </td>
          </tr>
          <tr v-if="!shownAppointments.length">
            <td colspan="6" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering">No appointments match &ldquo;{{ searchQuery.trim() }}&rdquo;. <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="resetSearch">Clear search</button></template>
              <template v-else>No appointments match.</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { adminMenu } from '@/config/menus'
import { appointmentsApi } from '@/api/appointments'
import { ApiError } from '@/api/client'
import type { Appointment, AppointmentStatus } from '@/api/types'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import { useSectionSearch } from '@/composables/useSectionSearch'
import { useSession } from '@/composables/useSession'

const statuses: AppointmentStatus[] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW']

const appointments = ref<Appointment[]>([])
const loading = ref(true)
const errorMessage = ref('')
const statusFilter = ref<AppointmentStatus | ''>('')

const filtered = computed(() =>
  statusFilter.value ? appointments.value.filter((a) => a.status === statusFilter.value) : appointments.value,
)
const session = useSession()
// Section search - this table only. The first field is the primary one and ranks highest.
const { query: searchQuery, results: shownAppointments, isFiltering, reset: resetSearch } = useSectionSearch(filtered, (a) => [
  `${a.customer?.firstName} ${a.customer?.lastName}`,
  `${a.stylist?.firstName} ${a.stylist?.lastName}`,
  a.salonService?.name,
  a.appointmentDate,
  a.status,
  a.promoCode,
])

onMounted(load)

async function load() {
  loading.value = true
  try {
    appointments.value = await appointmentsApi.list()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load appointments.'
  } finally {
    loading.value = false
  }
}

async function confirm(a: Appointment) {
  try {
    const updated = await appointmentsApi.confirm(a.appointmentId)
    replace(updated)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not confirm this appointment.'
  }
}

async function complete(a: Appointment) {
  try {
    const updated = await appointmentsApi.complete(a.appointmentId)
    replace(updated)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not complete this appointment.'
  }
}

async function cancel(a: Appointment) {
  try {
    const updated = await appointmentsApi.cancel(a.appointmentId)
    replace(updated)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not cancel this appointment.'
  }
}

function replace(updated: Appointment) {
  const index = appointments.value.findIndex((a) => a.appointmentId === updated.appointmentId)
  if (index !== -1) appointments.value[index] = updated
}
</script>
