<template>
  <AdminLayout :menu-groups="adminMenu">
    <h1 class="text-2xl font-semibold text-gray-800">Feedback</h1>

    <p v-if="errorMessage" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>
    <p v-else-if="!reviews.length" class="mt-4 text-sm text-gray-500">No feedback yet.</p>

    <div v-else class="mt-6 space-y-3">
      <div v-for="r in reviews" :key="r.reviewId" class="rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div class="font-medium text-gray-800">
            {{ r.customer.firstName }} {{ r.customer.lastName }}
            <span class="ml-2 text-warning-500">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
          </div>
          <span :class="r.verified ? 'bg-success-50 text-success-700' : 'bg-gray-100 text-gray-600'" class="rounded-full px-2.5 py-1 text-xs font-medium">
            {{ r.verified ? 'Verified' : 'Unverified' }}
          </span>
        </div>
        <p v-if="r.comment" class="mt-2 text-sm text-gray-600">{{ r.comment }}</p>
        <div class="mt-2 text-xs text-gray-400">{{ r.appointment.salonService.name }} &middot; {{ r.submittedAt.split('T')[0] }}</div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { adminMenu } from '@/config/menus'
import { feedbackApi } from '@/api/feedback'
import { ApiError } from '@/api/client'
import type { Feedback } from '@/api/types'

const reviews = ref<Feedback[]>([])
const loading = ref(true)
const errorMessage = ref('')

onMounted(async () => {
  try {
    reviews.value = await feedbackApi.list()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load feedback.'
  } finally {
    loading.value = false
  }
})
</script>
