<template>
  <AdminLayout :menu-groups="stylistMenu">
    <!-- Identity comes from the server session. A stylist login without a linked stylist
         record (e.g. the record was removed) is the only way to land here. -->
    <p v-if="!session.stylist.value" class="mt-4 text-sm text-gray-500">
      This login isn't linked to a stylist record. Ask the salon administrator to check your
      email address on the Stylists page.
    </p>

    <template v-else>
      <div class="flex items-center justify-between">
        <h1 class="text-2xl font-semibold text-gray-800">
          Welcome back, {{ session.stylist.value?.firstName }}
        </h1>
      </div>

      <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading your schedule&hellip;</p>
      <p v-else-if="errorMessage" class="mt-4 text-sm text-error-600">{{ errorMessage }}</p>

      <template v-else>
        <div class="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div class="rounded-xl border border-gray-200 p-5">
            <div class="text-sm text-gray-500">Today</div>
            <div class="mt-1 text-xl font-semibold text-gray-800">{{ todayAppointments.length }} {{ todayAppointments.length === 1 ? 'appointment' : 'appointments' }}</div>
          </div>
          <div class="rounded-xl border border-gray-200 p-5">
            <div class="text-sm text-gray-500">Next 7 days</div>
            <div class="mt-1 text-xl font-semibold text-gray-800">{{ upcomingCount }} {{ upcomingCount === 1 ? 'appointment' : 'appointments' }}</div>
          </div>
        </div>

        <h2 class="mt-8 text-sm font-semibold text-gray-800">Today's schedule</h2>
        <p v-if="!todayAppointments.length" class="mt-2 text-sm text-gray-500">Nothing booked today.</p>
        <div v-else class="mt-3 space-y-3">
          <div
            v-for="appt in todayAppointments"
            :key="appt.appointmentId"
            class="flex items-center justify-between rounded-lg border border-gray-200 p-4"
          >
            <div>
              <div class="font-medium text-gray-800">
                {{ appt.salonService.name }} for {{ appt.customer.firstName }} {{ appt.customer.lastName }}
              </div>
              <div class="mt-1 text-sm text-gray-500">{{ appt.startTime }} - {{ appt.endTime }}</div>
            </div>
            <span class="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-600">
              {{ appt.status }}
            </span>
          </div>
        </div>

        <router-link
          to="/stylist/schedule"
          class="mt-6 inline-block rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          View full schedule
        </router-link>
      </template>
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
import type { Appointment, Stylist } from '@/api/types'

const session = useSession()
const myAppointments = ref<Appointment[]>([])
const loading = ref(false)
const errorMessage = ref('')

const today = new Date().toISOString().split('T')[0]
const in7Days = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]

const todayAppointments = computed(() =>
  myAppointments.value
    .filter((a) => a.appointmentDate === today && a.status !== 'CANCELLED')
    .sort((a, b) => a.startTime.localeCompare(b.startTime)),
)

const upcomingCount = computed(
  () =>
    myAppointments.value.filter(
      (a) => a.appointmentDate >= today && a.appointmentDate <= in7Days && a.status !== 'CANCELLED',
    ).length,
)

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
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load your schedule.'
  } finally {
    loading.value = false
  }
}
</script>
