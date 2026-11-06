<template>
  <AdminLayout :menu-groups="stylistMenu">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-gray-800">Appointments</h1>
      <select
        v-if="session.stylist.value"
        v-model="statusFilter"
        class="h-10 rounded-lg border border-gray-300 bg-transparent px-3 text-sm"
      >
        <option value="">All statuses</option>
        <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
      </select>
    </div>

    <!-- Identity comes from the server session. A stylist login without a linked stylist
         record (e.g. the record was removed) is the only way to land here. -->
    <p v-if="!session.stylist.value" class="mt-4 text-sm text-gray-500">
      This login isn't linked to a stylist record. Ask the salon administrator to check your
      email address on the Stylists page.
    </p>

    <template v-else>
      <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>
      <p v-else-if="errorMessage" class="mt-4 text-sm text-error-600">{{ errorMessage }}</p>

      <!-- Separate from the load error above: a refused action must not hide the table. -->
      <p v-if="actionMessage" role="alert" class="mt-4 text-sm text-error-600">{{ actionMessage }}</p>
      <p v-if="doneMessage" role="status" class="mt-4 text-sm text-success-700">{{ doneMessage }}</p>

      <div v-if="!loading && !errorMessage" class="mt-6 overflow-x-auto rounded-lg border border-gray-200">
        <table class="min-w-full divide-y divide-gray-200 text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left font-medium text-gray-500">Customer</th>
              <th class="px-4 py-3 text-left font-medium text-gray-500">Service</th>
              <th class="px-4 py-3 text-left font-medium text-gray-500">When</th>
              <th class="px-4 py-3 text-left font-medium text-gray-500">Status</th>
              <th class="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="a in filtered" :key="a.appointmentId">
              <td class="px-4 py-3 font-medium text-gray-800">{{ a.customer.firstName }} {{ a.customer.lastName }}</td>
              <td class="px-4 py-3 text-gray-600">{{ a.salonService.name }}</td>
              <td class="px-4 py-3 text-gray-600">{{ a.appointmentDate }} {{ a.startTime }}</td>
              <td class="px-4 py-3">
                <span class="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-medium text-gray-600">{{ a.status }}</span>
              </td>
              <td class="space-x-3 whitespace-nowrap px-4 py-3 text-right">
                <!-- Confirming tells the customer their booking is on. -->
                <button
                  v-if="a.status === 'PENDING'"
                  @click="confirmBooking(a)"
                  :disabled="busyId === a.appointmentId"
                  class="font-medium text-brand-600 hover:text-brand-700 disabled:opacity-50"
                >Confirm</button>
                <button
                  v-if="a.status === 'PENDING' || a.status === 'CONFIRMED'"
                  @click="complete(a)"
                  :disabled="busyId === a.appointmentId"
                  class="font-medium text-success-600 hover:text-success-700 disabled:opacity-50"
                >Complete</button>
                <!-- The customer is told it was cancelled. A paid booking has to be refunded first. -->
                <button
                  v-if="a.status === 'PENDING' || a.status === 'CONFIRMED'"
                  @click="cancelBooking(a)"
                  :disabled="busyId === a.appointmentId"
                  class="text-error-600 hover:text-error-700 disabled:opacity-50"
                >Cancel</button>
              </td>
            </tr>
            <tr v-if="!filtered.length">
              <td colspan="5" class="px-4 py-6 text-center text-gray-400">No appointments match.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { stylistMenu } from '@/config/menus'
import { appointmentsApi } from '@/api/appointments'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import type { Appointment, AppointmentStatus, Stylist } from '@/api/types'

const statuses: AppointmentStatus[] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW']

const session = useSession()
const appointments = ref<Appointment[]>([])
const loading = ref(false)
const errorMessage = ref('')
const statusFilter = ref<AppointmentStatus | ''>('')
const busyId = ref<string | null>(null)

const filtered = computed(() =>
  statusFilter.value ? appointments.value.filter((a) => a.status === statusFilter.value) : appointments.value,
)

onMounted(async () => {
  if (session.stylist.value) await load()
})

watch(
  () => session.stylist.value,
  (isStylist) => {
    if (isStylist) load()
  },
)

async function load() {
  const stylistId = session.stylist.value?.stylistId
  if (!stylistId) return
  loading.value = true
  errorMessage.value = ''
  try {
    appointments.value = await appointmentsApi.byStylist(stylistId)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load appointments.'
  } finally {
    loading.value = false
  }
}

const actionMessage = ref('')
const doneMessage = ref('')

/** One path for every status change: replace the row with what the server returns, or show why not. */
async function change(a: Appointment, action: () => Promise<Appointment>, done: string) {
  busyId.value = a.appointmentId
  actionMessage.value = ''
  doneMessage.value = ''
  try {
    const updated = await action()
    const index = appointments.value.findIndex((x) => x.appointmentId === updated.appointmentId)
    if (index !== -1) appointments.value[index] = updated
    doneMessage.value = done
  } catch (err) {
    actionMessage.value = err instanceof ApiError ? err.message : 'Could not update this appointment.'
  } finally {
    busyId.value = null
  }
}

const who = (a: Appointment) => `${a.customer.firstName} ${a.customer.lastName}`

function confirmBooking(a: Appointment) {
  change(a, () => appointmentsApi.confirm(a.appointmentId), `Confirmed ${who(a)}'s booking. They've been told.`)
}

function complete(a: Appointment) {
  change(a, () => appointmentsApi.complete(a.appointmentId), `Marked ${who(a)}'s appointment as done.`)
}

function cancelBooking(a: Appointment) {
  if (!window.confirm(`Cancel ${who(a)}'s ${a.salonService.name} on ${a.appointmentDate} at ${a.startTime}? They'll be told.`)) return
  change(a, () => appointmentsApi.cancel(a.appointmentId), `Cancelled ${who(a)}'s booking. They've been told.`)
}
</script>
