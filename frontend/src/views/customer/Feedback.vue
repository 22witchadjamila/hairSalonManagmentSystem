<template>
  <AdminLayout :menu-groups="customerMenu">
    <h1 class="text-2xl font-semibold text-gray-800">My Feedback</h1>

    <div v-if="!session.isCustomer.value" class="mt-4 rounded-lg border border-gray-200 p-6 text-sm text-gray-500">
      We don't know who you are yet &mdash;
      <router-link to="/login" class="text-brand-500 hover:text-brand-600">log in</router-link>
      to see your feedback.
    </div>

    <template v-else>
      <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>
      <p v-else-if="errorMessage" class="mt-4 text-sm text-error-600">{{ errorMessage }}</p>

      <template v-else>
        <section v-if="pendingAppointments.length" class="mt-6">
          <h2 class="text-sm font-semibold text-gray-800">Awaiting your feedback</h2>
          <div class="mt-3 space-y-3">
            <div
              v-for="appt in pendingAppointments"
              :key="appt.appointmentId"
              class="rounded-lg border border-gray-200 p-4"
            >
              <div class="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <div class="font-medium text-gray-800">
                    {{ appt.salonService.name }} with {{ appt.stylist.firstName }} {{ appt.stylist.lastName }}
                  </div>
                  <div class="mt-1 text-sm text-gray-500">{{ appt.appointmentDate }}</div>
                </div>
                <button
                  v-if="formFor !== appt.appointmentId"
                  @click="openForm(appt.appointmentId)"
                  class="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Leave feedback
                </button>
              </div>

              <form v-if="formFor === appt.appointmentId" @submit.prevent="submit(appt.appointmentId)" class="mt-4 space-y-3">
                <div class="flex gap-1">
                  <button
                    v-for="star in 5"
                    :key="star"
                    type="button"
                    @click="rating = star"
                    class="text-2xl leading-none"
                    :class="star <= rating ? 'text-warning-500' : 'text-gray-300'"
                  >
                    &#9733;
                  </button>
                </div>
                <textarea
                  v-model="comment"
                  rows="2"
                  placeholder="How was your visit? (optional)"
                  class="w-full rounded-lg border border-gray-300 bg-transparent px-3 py-2 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10"
                />
                <div class="flex gap-3">
                  <button
                    type="submit"
                    :disabled="submitting || !rating"
                    class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
                  >
                    {{ submitting ? 'Submitting…' : 'Submit' }}
                  </button>
                  <button type="button" @click="closeForm" class="text-sm text-gray-500 hover:text-gray-700">
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        </section>

        <section class="mt-8">
          <h2 class="text-sm font-semibold text-gray-800">Your reviews</h2>
          <p v-if="!myFeedback.length" class="mt-2 text-sm text-gray-500">
            You haven't left any feedback yet.
          </p>
          <div v-else class="mt-3 space-y-3">
            <div
              v-for="f in myFeedback"
              :key="f.reviewId"
              class="rounded-lg border border-gray-200 p-4"
            >
              <div class="flex items-center justify-between">
                <div class="font-medium text-gray-800">
                  {{ f.appointment.salonService.name }}
                </div>
                <span class="text-warning-500">{{ '★'.repeat(f.rating) }}{{ '☆'.repeat(5 - f.rating) }}</span>
              </div>
              <p v-if="f.comment" class="mt-2 text-sm text-gray-600">{{ f.comment }}</p>
              <div class="mt-2 text-xs text-gray-400">{{ f.submittedAt.split('T')[0] }}</div>
            </div>
          </div>
        </section>
      </template>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { customerMenu } from '@/config/menus'
import { appointmentsApi } from '@/api/appointments'
import { feedbackApi } from '@/api/feedback'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import type { Appointment, Feedback } from '@/api/types'

const session = useSession()
const appointments = ref<Appointment[]>([])
const myFeedback = ref<Feedback[]>([])
const loading = ref(true)
const errorMessage = ref('')

const formFor = ref<string | null>(null)
const rating = ref(0)
const comment = ref('')
const submitting = ref(false)

const pendingAppointments = computed(() => {
  const reviewedIds = new Set(myFeedback.value.map((f) => f.appointment.appointmentId))
  return appointments.value.filter((a) => a.status === 'COMPLETED' && !reviewedIds.has(a.appointmentId))
})

onMounted(async () => {
  const customerId = session.customer.value?.customerId
  if (!customerId) {
    loading.value = false
    return
  }
  try {
    const [appts, feedback] = await Promise.all([
      appointmentsApi.byCustomer(customerId),
      feedbackApi.byCustomer(customerId),
    ])
    appointments.value = appts
    myFeedback.value = feedback
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load your feedback.'
  } finally {
    loading.value = false
  }
})

function openForm(appointmentId: string) {
  formFor.value = appointmentId
  rating.value = 0
  comment.value = ''
}

function closeForm() {
  formFor.value = null
}

async function submit(appointmentId: string) {
  const customerId = session.customer.value?.customerId
  if (!customerId || !rating.value) return
  submitting.value = true
  errorMessage.value = ''
  try {
    const created = await feedbackApi.create({
      appointmentId,
      customerId,
      rating: rating.value,
      comment: comment.value || null,
    })
    myFeedback.value = [created, ...myFeedback.value]
    closeForm()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not submit your feedback.'
  } finally {
    submitting.value = false
  }
}
</script>
