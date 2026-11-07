<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-gray-800">Customers</h1>
      <button v-if="session.can('CUSTOMERS', 'CREATE')" @click="showForm = !showForm" class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
        {{ showForm ? 'Cancel' : '+ Add Customer' }}
      </button>
    </div>
    <SectionSearch v-model="searchQuery" class="mt-4" :total="customers.length" :shown="shownCustomers.length" placeholder="Search name, email or phone…" />
    <p v-if="successMessage" role="status" class="mt-3 text-sm text-success-700">{{ successMessage }}</p>

    <!-- Staff correction. Payments recorded in the app already award their own points. -->
    <form v-if="awarding" class="mt-4 flex flex-wrap items-end gap-3 rounded-lg border border-gray-200 p-4" @submit.prevent="award">
      <p class="w-full text-sm font-medium text-gray-800">Add loyalty points for {{ awarding.firstName }} {{ awarding.lastName }}</p>
      <div>
        <label for="award-amount" class="mb-1 block text-xs text-gray-500">Amount spent (R)</label>
        <input id="award-amount" v-model.number="awardAmount" type="number" min="0.01" step="0.01" required :class="inputClass" />
      </div>
      <p class="text-sm text-gray-600">
        <template v-if="awardPoints > 0">= {{ awardPoints }} {{ awardPoints === 1 ? 'point' : 'points' }}</template>
        <template v-else>Needs at least R{{ rules?.randSpentPerPoint ?? 10 }} to earn a point.</template>
      </p>
      <button type="submit" :disabled="awardingNow || awardPoints < 1" class="h-10 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
        {{ awardingNow ? 'Adding…' : 'Add points' }}
      </button>
      <button type="button" class="h-10 px-3 text-sm text-gray-500 hover:text-gray-700" @click="awarding = null">Cancel</button>
      <p class="w-full text-xs text-gray-500">
        Use this for sales taken outside the app, or a goodwill gesture. A payment recorded on the Payments page
        has already earned its points, so adding the same visit here would count it twice.
      </p>
    </form>


    <form v-if="showForm" @submit.prevent="create" class="mt-4 grid grid-cols-1 gap-3 rounded-lg border border-gray-200 p-4 sm:grid-cols-2 lg:grid-cols-4">
      <input v-model="form.firstName" placeholder="First name" required :class="inputClass" />
      <input v-model="form.lastName" placeholder="Last name" required :class="inputClass" />
      <input v-model="form.email" type="email" placeholder="Email" required :class="inputClass" />
      <input v-model="form.phoneNumber" placeholder="Phone number" required :class="inputClass" />
      <button type="submit" :disabled="submitting" class="col-span-full rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
        {{ submitting ? 'Adding…' : 'Add Customer' }}
      </button>
    </form>

    <p v-if="errorMessage" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>

    <div v-else class="mt-6 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Name</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Email</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Phone</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Registered</th>
            <th v-if="session.can('LOYALTY')" class="px-4 py-3 text-left font-medium text-gray-500">Loyalty</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="c in shownCustomers" :key="c.customerId">
            <td class="px-4 py-3 font-medium text-gray-800">{{ c.firstName }} {{ c.lastName }}</td>
            <td class="px-4 py-3 text-gray-600">{{ c.email }}</td>
            <td class="px-4 py-3 text-gray-600">{{ c.phoneNumber }}</td>
            <td class="px-4 py-3 text-gray-600">{{ c.registeredAt.split('T')[0] }}</td>
            <td v-if="session.can('LOYALTY')" class="px-4 py-3 text-gray-600">
              <template v-if="accountOf(c)">{{ accountOf(c)!.pointsBalance }} pts <span class="text-xs text-gray-400">{{ accountOf(c)!.tier }}</span></template>
              <template v-else>&mdash;</template>
            </td>
            <td class="space-x-3 whitespace-nowrap px-4 py-3 text-right">
              <button v-if="session.can('LOYALTY', 'EDIT')" @click="openAward(c)" class="font-medium text-brand-600 hover:text-brand-700">Add points</button>
              <button v-if="session.can('CUSTOMERS', 'DELETE')" @click="remove(c)" class="text-error-600 hover:text-error-700">Delete</button>
            </td>
          </tr>
          <tr v-if="!shownCustomers.length">
            <td colspan="6" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering">No customers match &ldquo;{{ searchQuery.trim() }}&rdquo;. <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="resetSearch">Clear search</button></template>
              <template v-else>No customers yet.</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { adminMenu } from '@/config/menus'
import { customersApi } from '@/api/customers'
import { loyaltyApi, type LoyaltyRules } from '@/api/loyalty'
import { ApiError } from '@/api/client'
import type { Customer, LoyaltyReward } from '@/api/types'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import { useSectionSearch } from '@/composables/useSectionSearch'
import { useSession } from '@/composables/useSession'

const inputClass =
  'h-10 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

const customers = ref<Customer[]>([])
const session = useSession()
// Section search - this table only. The first field is the primary one and ranks highest.
const { query: searchQuery, results: shownCustomers, isFiltering, reset: resetSearch } = useSectionSearch(customers, (c) => [
  `${c.firstName} ${c.lastName}`, c.email, c.phoneNumber,
])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Loyalty accounts by customer id. A customer who has never opened their loyalty page has no
// account yet - shown as a dash - and gets one on their first point.
const accounts = ref<Record<string, LoyaltyReward>>({})
const rules = ref<LoyaltyRules | null>(null)
const accountOf = (c: Customer) => accounts.value[c.customerId] ?? null

const awarding = ref<Customer | null>(null)
const awardAmount = ref<number | null>(null)
const awardingNow = ref(false)
const awardPoints = computed(() => {
  const per = rules.value?.randSpentPerPoint ?? 10
  return awardAmount.value && awardAmount.value > 0 ? Math.floor(awardAmount.value / per) : 0
})

function openAward(c: Customer) {
  awarding.value = c
  awardAmount.value = null
  successMessage.value = ''
  errorMessage.value = ''
}

async function award() {
  if (!awarding.value || !awardAmount.value) return
  awardingNow.value = true
  errorMessage.value = ''
  try {
    const result = await loyaltyApi.addPoints(awarding.value.customerId, awardAmount.value)
    accounts.value = { ...accounts.value, [awarding.value.customerId]: result }
    successMessage.value = `Added ${awardPoints.value} points for ${awarding.value.firstName} ${awarding.value.lastName}. Balance ${result.pointsBalance}, ${result.tier}.`
    awarding.value = null
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not add points.'
  } finally {
    awardingNow.value = false
  }
}

const form = reactive({ firstName: '', lastName: '', email: '', phoneNumber: '' })

onMounted(load)

async function load() {
  loading.value = true
  try {
    customers.value = await customersApi.list()
    // Loyalty is extra: a role that can see customers but not loyalty just doesn't get the column.
    if (session.can('LOYALTY')) {
      const [list, r] = await Promise.all([loyaltyApi.list().catch(() => []), loyaltyApi.rules().catch(() => null)])
      accounts.value = Object.fromEntries(list.map((a) => [a.customer.customerId, a]))
      rules.value = r
    }
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load customers.'
  } finally {
    loading.value = false
  }
}

async function create() {
  submitting.value = true
  errorMessage.value = ''
  try {
    await customersApi.create({ ...form })
    Object.assign(form, { firstName: '', lastName: '', email: '', phoneNumber: '' })
    showForm.value = false
    await load()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not add this customer.'
  } finally {
    submitting.value = false
  }
}

async function remove(c: Customer) {
  if (!confirm(`Delete ${c.firstName} ${c.lastName}?`)) return
  try {
    await customersApi.delete(c.customerId)
    customers.value = customers.value.filter((x) => x.customerId !== c.customerId)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not delete this customer.'
  }
}
</script>
