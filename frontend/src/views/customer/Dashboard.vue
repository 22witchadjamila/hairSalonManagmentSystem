<template>
  <AdminLayout :menu-groups="customerMenu">
    <div v-if="!session.isCustomer.value" class="mt-4 rounded-lg border border-gray-200 p-6 text-sm text-gray-500">
      We don't know who you are yet &mdash;
      <router-link to="/login" class="text-brand-500 hover:text-brand-600">log in</router-link>
      or
      <router-link to="/book-appointment" class="text-brand-500 hover:text-brand-600">book an appointment</router-link>
      to see your dashboard.
    </div>

    <template v-else>
      <h1 class="text-2xl font-semibold text-gray-800">
        Welcome back, {{ session.customer.value?.firstName }}
      </h1>

      <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading your dashboard&hellip;</p>
      <p v-else-if="errorMessage" class="mt-4 text-sm text-error-600">{{ errorMessage }}</p>

      <template v-else>
        <div class="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div class="rounded-xl border border-gray-200 p-5">
            <div class="text-sm text-gray-500">Loyalty tier</div>
            <div class="mt-1 text-xl font-semibold text-gray-800">{{ loyalty?.tier ?? 'BRONZE' }}</div>
            <div class="mt-1 text-sm text-gray-500">{{ loyalty?.pointsBalance ?? 0 }} points</div>
          </div>
          <div class="rounded-xl border border-gray-200 p-5">
            <div class="text-sm text-gray-500">Total appointments</div>
            <div class="mt-1 text-xl font-semibold text-gray-800">{{ appointments.length }}</div>
          </div>
          <div class="rounded-xl border border-gray-200 p-5">
            <div class="text-sm text-gray-500">Next appointment</div>
            <div class="mt-1 text-xl font-semibold text-gray-800">
              {{ nextAppointment ? nextAppointment.appointmentDate : 'None booked' }}
            </div>
            <div v-if="nextAppointment" class="mt-1 text-sm text-gray-500">
              {{ nextAppointment.startTime }} &middot; {{ nextAppointment.salonService.name }}
            </div>
          </div>
        </div>

        <div class="mt-6 flex flex-wrap gap-3">
          <router-link
            to="/book-appointment"
            class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
          >
            Book another appointment
          </router-link>
          <router-link
            to="/customer/appointments"
            class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            View all appointments
          </router-link>
        </div>

        <h2 class="mt-8 text-sm font-semibold text-gray-800">Notifications</h2>
        <p v-if="!notifications.length" class="mt-2 text-sm text-gray-500">
          Nothing yet &mdash; we'll let you know here once your booking is confirmed.
        </p>
        <div v-else class="mt-3 space-y-2">
          <div
            v-for="n in notifications"
            :key="n.notificationId"
            class="rounded-lg border border-gray-200 p-3 text-sm"
          >
            <div class="text-gray-800">{{ n.message }}</div>
            <div class="mt-1 text-xs text-gray-400">{{ (n.sentAt ?? n.scheduledAt).replace('T', ' ').split('.')[0] }}</div>
          </div>
        </div>
      </template>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { customerMenu } from '@/config/menus'
import { appointmentsApi } from '@/api/appointments'
import { loyaltyApi } from '@/api/loyalty'
import { notificationsApi } from '@/api/notifications'
import { useRefresh } from '@/composables/useRefresh'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import type { Appointment, LoyaltyReward, Notification } from '@/api/types'

const session = useSession()
const appointments = ref<Appointment[]>([])
const loyalty = ref<LoyaltyReward | null>(null)
const notifications = ref<Notification[]>([])
const loading = ref(true)
const errorMessage = ref('')

const nextAppointment = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return appointments.value
    .filter((a) => a.appointmentDate >= today && (a.status === 'PENDING' || a.status === 'CONFIRMED'))
    .sort((a, b) => `${a.appointmentDate}${a.startTime}`.localeCompare(`${b.appointmentDate}${b.startTime}`))[0]
})

// Staff confirm a booking while this page is open: refresh the booking list (its status changes)
// and the messages, without a reload. Quiet - a failed refresh keeps what is already on screen.
useRefresh(async () => {
  const customerId = session.customer.value?.customerId
  if (!customerId || loading.value) return
  try {
    ;[appointments.value, notifications.value] = await Promise.all([
      appointmentsApi.byCustomer(customerId),
      notificationsApi.byCustomer(customerId),
    ])
  } catch {
    /* keep showing what we have */
  }
})

onMounted(async () => {
  const customerId = session.customer.value?.customerId
  if (!customerId) {
    loading.value = false
    return
  }
  try {
    const [appts, reward, notifs] = await Promise.all([
      appointmentsApi.byCustomer(customerId),
      loyaltyApi.getOrCreate(customerId),
      notificationsApi.byCustomer(customerId),
    ])
    appointments.value = appts
    loyalty.value = reward
    notifications.value = notifs
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load your dashboard.'
  } finally {
    loading.value = false
  }
})
</script>
