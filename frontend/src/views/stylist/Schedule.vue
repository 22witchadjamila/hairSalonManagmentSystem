<template>
  <AdminLayout :menu-groups="stylistMenu">
    <h1 class="text-2xl font-semibold text-gray-800">My Schedule</h1>

    <!-- No backend auth yet, so "logging in" as a stylist is just picking your name. -->
    <!-- Identity comes from the server session. A stylist login without a linked stylist
         record (e.g. the record was removed) is the only way to land here. -->
    <p v-if="!session.stylist.value" class="mt-4 text-sm text-gray-500">
      This login isn't linked to a stylist record. Ask the salon administrator to check your
      email address on the Stylists page.
    </p>

    <template v-else>
      <div class="mt-4 flex items-center justify-between">
        <p class="text-sm text-gray-500">
          Showing schedule for <span class="font-medium text-gray-800">{{ session.stylist.value?.firstName }} {{ session.stylist.value?.lastName }}</span>
        </p>
      </div>

      <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading appointments&hellip;</p>
      <p v-else-if="errorMessage" class="mt-4 text-sm text-error-600">{{ errorMessage }}</p>
      <p v-else-if="!myAppointments.length" class="mt-4 text-sm text-gray-500">
        No appointments scheduled.
      </p>

      <div v-else class="mt-6 space-y-3">
        <div
          v-for="appt in myAppointments"
          :key="appt.appointmentId"
          class="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-gray-200 p-4"
        >
          <div>
            <div class="font-medium text-gray-800">
              {{ appt.salonService.name }} for {{ appt.customer.firstName }} {{ appt.customer.lastName }}
            </div>
            <div class="mt-1 text-sm text-gray-500">
              {{ appt.appointmentDate }} &middot; {{ appt.startTime }} - {{ appt.endTime }}
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span class="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-600">
              {{ appt.status }}
            </span>
            <button
              v-if="appt.status === 'PENDING' || appt.status === 'CONFIRMED'"
              @click="complete(appt)"
              :disabled="busyId === appt.appointmentId"
              class="text-sm font-medium text-success-600 hover:text-success-700 disabled:opacity-50"
            >
              Mark complete
            </button>
          </div>
        </div>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { stylistMenu } from '@/config/menus'
import { appointmentsApi } from '@/api/appointments'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import type { Appointment, Stylist } from '@/api/types'

const session = useSession()
const myAppointments = ref<Appointment[]>([])
const loading = ref(false)
const errorMessage = ref('')
const busyId = ref<string | null>(null)

onMounted(async () => {
  if (session.stylist.value) await loadAppointments()
})

watch(
  () => session.stylist.value,
  (isStylist) => {
    if (isStylist) loadAppointments()
  },
)

async function loadAppointments() {
  const stylistId = session.stylist.value?.stylistId
  if (!stylistId) return
  loading.value = true
  errorMessage.value = ''
  try {
    myAppointments.value = await appointmentsApi.byStylist(stylistId)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load appointments.'
  } finally {
    loading.value = false
  }
}

async function complete(appt: Appointment) {
  busyId.value = appt.appointmentId
  try {
    const updated = await appointmentsApi.complete(appt.appointmentId)
    const index = myAppointments.value.findIndex((a) => a.appointmentId === updated.appointmentId)
    if (index !== -1) myAppointments.value[index] = updated
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not update this appointment.'
  } finally {
    busyId.value = null
  }
}
</script>
