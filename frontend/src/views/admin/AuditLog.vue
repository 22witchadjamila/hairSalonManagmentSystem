<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-2xl font-semibold text-gray-800">Audit Log</h1>
        <p class="mt-1 max-w-2xl text-sm text-gray-500">
          Who did what, and when. Every sign-in (and failed attempt), every change to payments,
          promotions, people, roles and passwords, and every time someone was blocked. Entries can't be
          edited or deleted. Only admins can read this page.
        </p>
      </div>
      <p class="text-xs text-gray-500" role="status" aria-live="polite">
        <template v-if="refreshing">Updating&hellip;</template>
        <template v-else-if="updatedAt">As of {{ updatedAt }}</template>
        <button type="button" class="ml-2 font-medium text-brand-600 hover:text-brand-700" :disabled="refreshing" @click="load(false)">
          Refresh
        </button>
      </p>
    </div>

    <div class="mt-4 flex flex-wrap items-center gap-3">
      <SectionSearch
        v-model="searchQuery"
        :total="visibleByOutcome.length"
        :shown="shownEvents.length"
        placeholder="Search person, action, address…"
      />
      <label class="sr-only" for="audit-outcome">Filter by result</label>
      <select id="audit-outcome" v-model="outcome" class="h-10 rounded-lg border border-gray-300 bg-transparent px-3 text-sm">
        <option value="">All results</option>
        <option value="SUCCESS">Worked</option>
        <option value="FAILED">Failed</option>
        <option value="DENIED">Blocked</option>
      </select>
    </div>

    <p v-if="errorMessage" role="alert" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>

    <div v-else class="mt-4 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">When</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Who</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">What</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Result</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">From</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="e in shownEvents" :key="e.auditId">
            <td class="whitespace-nowrap px-4 py-3 text-xs text-gray-500">{{ e.occurredAt.replace('T', ' ').slice(0, 19) }}</td>
            <td class="px-4 py-3">
              <span class="font-medium text-gray-800">{{ e.actor }}</span>
              <span v-if="e.role" class="block text-xs text-gray-400">{{ e.role }}</span>
            </td>
            <td class="px-4 py-3 text-gray-700">
              {{ e.action }}
              <span v-if="e.detail" class="block text-xs text-gray-500">{{ e.detail }}</span>
              <span class="block text-xs text-gray-400">{{ e.method }} {{ e.path }}</span>
            </td>
            <td class="px-4 py-3">
              <span :class="OUTCOME[e.outcome].cls" class="rounded-full px-2.5 py-1 text-xs font-medium">{{ OUTCOME[e.outcome].label }}</span>
              <span v-if="e.status" class="ml-1 text-xs text-gray-400">{{ e.status }}</span>
            </td>
            <td class="px-4 py-3 text-xs text-gray-500">{{ e.ip }}</td>
          </tr>
          <tr v-if="!shownEvents.length">
            <td colspan="5" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering || outcome">
                Nothing matches.
                <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="clearFilters">Clear filters</button>
              </template>
              <template v-else>Nothing recorded yet.</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-if="events.length >= LIMIT" class="mt-2 text-xs text-gray-400">Showing the latest {{ LIMIT }} entries.</p>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import { adminMenu } from '@/config/menus'
import { auditApi, type AuditEvent } from '@/api/audit'
import { ApiError } from '@/api/client'
import { useSectionSearch } from '@/composables/useSectionSearch'
import { useRefresh } from '@/composables/useRefresh'

const LIMIT = 300
const OUTCOME: Record<AuditEvent['outcome'], { label: string; cls: string }> = {
  SUCCESS: { label: 'Worked', cls: 'bg-success-50 text-success-700' },
  FAILED: { label: 'Failed', cls: 'bg-warning-50 text-warning-700' },
  DENIED: { label: 'Blocked', cls: 'bg-error-50 text-error-700' },
}

const events = ref<AuditEvent[]>([])
const loading = ref(true)
const refreshing = ref(false)
const updatedAt = ref('')
const errorMessage = ref('')
const outcome = ref<'' | AuditEvent['outcome']>('')

const visibleByOutcome = computed(() => (outcome.value ? events.value.filter((e) => e.outcome === outcome.value) : events.value))
const { query: searchQuery, results: shownEvents, isFiltering, reset: resetSearch } = useSectionSearch(
  visibleByOutcome,
  (e) => [e.actor, e.action, e.path, e.ip, e.role, e.detail],
)

function clearFilters() {
  outcome.value = ''
  resetSearch()
}

async function load(initial = true) {
  if (refreshing.value) return
  if (initial) loading.value = true
  refreshing.value = true
  try {
    events.value = await auditApi.latest(LIMIT)
    updatedAt.value = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    errorMessage.value = ''
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load the audit log.'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

onMounted(load)
useRefresh(() => load(false), 30_000)
</script>
