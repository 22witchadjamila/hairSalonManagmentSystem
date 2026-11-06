<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-2xl font-semibold text-gray-800">Notifications</h1>
        <p class="mt-1 max-w-2xl text-sm text-gray-500">
          Messages appear in each customer's notification bell and on their dashboard.
          <strong class="font-medium text-gray-700">Confirmations</strong> go out when you confirm a booking, and
          <strong class="font-medium text-gray-700">reminders</strong> are created automatically for confirmed
          bookings the day before (or the same day, if it was confirmed late).
          Email and SMS aren't connected, so nothing is sent outside the app.
        </p>
      </div>
      <button
        v-if="session.can('NOTIFICATIONS', 'CREATE')"
        class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
        @click="showForm = !showForm"
      >
        {{ showForm ? 'Cancel' : '+ Send Promotion Message' }}
      </button>
    </div>

    <form v-if="showForm" class="mt-4 rounded-lg border border-gray-200 p-4" @submit.prevent="send">
      <label for="promo-message" class="mb-1 block text-xs text-gray-500">
        Message &mdash; shown to every customer for 30 days
      </label>
      <textarea
        id="promo-message"
        v-model="message"
        :maxlength="MAX_LENGTH"
        rows="2"
        required
        placeholder="e.g. 20% off colour this weekend with code SUMMER20"
        class="w-full rounded-lg border border-gray-300 bg-transparent px-3 py-2 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10"
      ></textarea>
      <div class="mt-1 flex items-center justify-between text-xs text-gray-400">
        <span>{{ message.length }} / {{ MAX_LENGTH }}</span>
      </div>
      <button
        type="submit"
        :disabled="submitting || !message.trim()"
        class="mt-3 rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
      >
        {{ submitting ? 'Sending…' : 'Send to all customers' }}
      </button>
    </form>

    <p v-if="errorMessage" role="alert" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="successMessage" role="status" class="mt-3 text-sm text-success-700">{{ successMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>
    <p v-else-if="!notifications.length" class="mt-4 text-sm text-gray-500">No notifications yet.</p>

    <div v-else class="mt-6 space-y-3">
      <div v-for="n in sorted" :key="n.notificationId" class="flex items-start justify-between gap-4 rounded-lg border border-gray-200 p-4">
        <div class="min-w-0">
          <div class="font-medium text-gray-800">{{ n.message }}</div>
          <div class="mt-1 text-xs text-gray-400">
            {{ TYPE_LABEL[n.type] ?? n.type }} &middot; to {{ recipient(n) }} &middot;
            {{ (n.sentAt ?? n.scheduledAt).replace('T', ' ').slice(0, 16) }}
          </div>
        </div>
        <span :class="n.status === 'SENT' ? 'bg-success-50 text-success-700' : 'bg-gray-100 text-gray-600'" class="shrink-0 rounded-full px-2.5 py-1 text-xs font-medium">
          {{ STATUS_LABEL[n.status] ?? n.status }}
        </span>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { adminMenu } from '@/config/menus'
import { notificationsApi } from '@/api/notifications'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import type { Notification } from '@/api/types'

const MAX_LENGTH = 280
const TYPE_LABEL: Record<string, string> = {
  APPOINTMENT_CONFIRMED: 'Booking confirmed',
  APPOINTMENT_REMINDER: 'Reminder',
  PROMOTION: 'Promotion',
  LOYALTY_UPDATE: 'Loyalty',
  GENERAL: 'Message',
}
const STATUS_LABEL: Record<string, string> = { SENT: 'Delivered', SCHEDULED: 'Not sent', FAILED: 'Failed' }

const session = useSession()
const notifications = ref<Notification[]>([])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const message = ref('')

/** Newest first. */
const sorted = computed(() =>
  [...notifications.value].sort((a, b) => (b.sentAt ?? b.scheduledAt).localeCompare(a.sentAt ?? a.scheduledAt)),
)

/** A message about a booking goes to that customer; one with no booking is for everyone. */
function recipient(n: Notification): string {
  const c = n.appointment?.customer
  return c ? `${c.firstName} ${c.lastName}` : 'all customers'
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    notifications.value = await notificationsApi.list()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load notifications.'
  } finally {
    loading.value = false
  }
}

async function send() {
  submitting.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await notificationsApi.sendPromotion(message.value.trim())
    message.value = ''
    showForm.value = false
    successMessage.value = 'Sent. Every customer will see it in their notification bell.'
    await load()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not send this message.'
  } finally {
    submitting.value = false
  }
}
</script>
