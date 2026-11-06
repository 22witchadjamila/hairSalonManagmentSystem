<template>
  <AdminLayout :menu-groups="customerMenu">
    <h1 class="text-2xl font-semibold text-gray-800">My Appointments</h1>

    <div v-if="!session.isCustomer.value" class="mt-4 rounded-lg border border-gray-200 p-6 text-sm text-gray-500">
      We don't know who you are yet &mdash;
      <router-link to="/login" class="text-brand-500 hover:text-brand-600">log in</router-link>
      or
      <router-link to="/book-appointment" class="text-brand-500 hover:text-brand-600">book an appointment</router-link>
      to see your bookings here.
    </div>

    <template v-else>
      <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading your appointments&hellip;</p>
      <p v-else-if="errorMessage" class="mt-4 text-sm text-error-600">{{ errorMessage }}</p>
      <p v-else-if="!appointments.length" class="mt-4 text-sm text-gray-500">
        No appointments yet.
        <router-link to="/book-appointment" class="text-brand-500 hover:text-brand-600">Book one now</router-link>.
      </p>

      <div v-else class="mt-6 space-y-3">
        <div
          v-for="appt in appointments"
          :key="appt.appointmentId"
          class="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-gray-200 p-4"
        >
          <div>
            <div class="font-medium text-gray-800">
              {{ appt.salonService.name }} with {{ appt.stylist.firstName }} {{ appt.stylist.lastName }}
            </div>
            <div class="mt-1 text-sm text-gray-500">
              {{ appt.appointmentDate }} &middot; {{ appt.startTime }} - {{ appt.endTime }}
            </div>
            <div class="mt-1 text-sm text-gray-500">
              R{{ appt.salonService.price.toFixed(2) }} &mdash; payable in store
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span :class="statusBadgeClass(appt.status)" class="rounded-full px-3 py-1 text-xs font-medium">
              {{ appt.status }}
            </span>
            <button
              v-if="appt.status === 'PENDING' || appt.status === 'CONFIRMED'"
              @click="cancel(appt)"
              :disabled="cancellingId === appt.appointmentId"
              class="text-sm font-medium text-error-600 hover:text-error-700 disabled:opacity-50"
            >
              {{ cancellingId === appt.appointmentId ? 'Cancelling…' : 'Cancel' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { customerMenu } from '@/config/menus'
import { appointmentsApi } from '@/api/appointments'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import type { Appointment, AppointmentStatus } from '@/api/types'

const session = useSession()
const appointments = ref<Appointment[]>([])
const loading = ref(true)
const errorMessage = ref('')
const cancellingId = ref<string | null>(null)

onMounted(async () => {
  if (!session.customer.value) {
    loading.value = false
    return
  }
  try {
    appointments.value = await appointmentsApi.byCustomer(session.customer.value.customerId)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load your appointments.'
  } finally {
    loading.value = false
  }
})

async function cancel(appt: Appointment) {
  cancellingId.value = appt.appointmentId
  try {
    const updated = await appointmentsApi.cancel(appt.appointmentId)
    const index = appointments.value.findIndex((a) => a.appointmentId === updated.appointmentId)
    if (index !== -1) appointments.value[index] = updated
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not cancel this appointment.'
  } finally {
    cancellingId.value = null
  }
}

function statusBadgeClass(status: AppointmentStatus) {
  switch (status) {
    case 'CONFIRMED':
    case 'COMPLETED':
      return 'bg-success-50 text-success-700'
    case 'CANCELLED':
    case 'NO_SHOW':
      return 'bg-error-50 text-error-700'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}
</script>
