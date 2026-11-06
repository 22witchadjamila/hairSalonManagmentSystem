<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-2xl font-semibold text-gray-800">Promotions</h1>
        <p class="mt-1 max-w-2xl text-sm text-gray-500">
          <strong class="font-medium text-gray-700">Used</strong> counts payments that applied the code (a refund gives
          the use back). <strong class="font-medium text-gray-700">Booked</strong> is bookings holding the code that
          haven't been paid yet &mdash; they count against the limit, so an offer can't be over-promised.
          Cancelled bookings release their hold.
        </p>
      </div>
      <button
        v-if="session.can('PROMOTIONS', 'CREATE')"
        class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
        @click="showForm && !editing ? closeForm() : openCreate()"
      >
        {{ showForm && !editing ? 'Cancel' : '+ Add Promotion' }}
      </button>
    </div>

    <!-- One form for adding and editing. Edits never touch the usage count. -->
    <form
      v-if="showForm"
      novalidate
      class="mt-4 grid grid-cols-1 gap-3 rounded-lg border border-gray-200 p-4 sm:grid-cols-2 lg:grid-cols-4"
      @submit.prevent="save"
    >
      <p class="col-span-full text-sm font-medium text-gray-800">
        {{ editing ? `Edit ${editing.code}` : 'New promotion' }}
        <span v-if="editing" class="ml-2 font-normal text-gray-500">
          Used {{ statsFor(editing).redeemed }} {{ statsFor(editing).redeemed === 1 ? 'time' : 'times' }} &mdash; editing keeps that count.
        </span>
      </p>
      <div>
        <label for="promo-code" class="mb-1 block text-xs text-gray-500">Code</label>
        <input id="promo-code" v-model="form.code" placeholder="e.g. SUMMER20" :class="inputClass" maxlength="20" />
      </div>
      <div>
        <label for="promo-desc" class="mb-1 block text-xs text-gray-500">Description (optional)</label>
        <input id="promo-desc" v-model="form.description" :class="inputClass" maxlength="255" />
      </div>
      <div>
        <label for="promo-type" class="mb-1 block text-xs text-gray-500">Discount type</label>
        <select id="promo-type" v-model="form.discountType" :class="inputClass">
          <option value="PERCENTAGE">Percentage</option>
          <option value="FIXED_AMOUNT">Fixed amount (R)</option>
        </select>
      </div>
      <div>
        <label for="promo-value" class="mb-1 block text-xs text-gray-500">
          {{ form.discountType === 'PERCENTAGE' ? 'Discount (%)' : 'Discount (R)' }}
        </label>
        <input id="promo-value" v-model.number="form.discountValue" type="number" min="0.01" step="0.01" :class="inputClass" />
      </div>
      <div>
        <label for="promo-start" class="mb-1 block text-xs text-gray-500">Start date</label>
        <input id="promo-start" v-model="form.startDate" type="date" :class="inputClass" />
      </div>
      <div>
        <label for="promo-end" class="mb-1 block text-xs text-gray-500">End date</label>
        <input id="promo-end" v-model="form.endDate" type="date" :min="form.startDate || undefined" :class="inputClass" />
      </div>
      <div>
        <label for="promo-limit" class="mb-1 block text-xs text-gray-500">Usage limit</label>
        <input id="promo-limit" v-model.number="form.usageLimit" type="number" :min="editing ? Math.max(1, editing.usageCount) : 1" step="1" :class="inputClass" />
      </div>
      <div class="flex items-end gap-2">
        <button
          type="submit"
          :disabled="submitting"
          class="h-10 flex-1 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
        >
          {{ submitting ? 'Saving…' : editing ? 'Save changes' : 'Add promotion' }}
        </button>
        <button type="button" class="h-10 px-3 text-sm text-gray-500 hover:text-gray-700" @click="closeForm">Cancel</button>
      </div>
      <ul v-if="formErrors.length" role="alert" class="col-span-full list-disc pl-5 text-sm text-error-600">
        <li v-for="e in formErrors" :key="e">{{ e }}</li>
      </ul>
    </form>

    <div class="mt-4 flex flex-wrap items-center justify-between gap-3">
      <SectionSearch
        v-model="searchQuery"
        :total="promotions.length"
        :shown="shownPromotions.length"
        placeholder="Search code or description…"
      />
      <p class="text-xs text-gray-500" role="status" aria-live="polite">
        <template v-if="refreshing">Updating&hellip;</template>
        <template v-else-if="updatedAt">Figures as of {{ updatedAt }}</template>
        <button type="button" class="ml-2 font-medium text-brand-600 hover:text-brand-700" :disabled="refreshing" @click="load(false)">
          Refresh
        </button>
      </p>
    </div>

    <p v-if="errorMessage" role="alert" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="successMessage" role="status" class="mt-3 text-sm text-success-700">{{ successMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>

    <div v-else class="mt-4 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Code</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Discount</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Valid</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500" title="Payments that applied this code. A refund gives the use back.">Used</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500" title="Bookings with this code that haven't been paid yet.">Booked</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Remaining</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Status</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="p in shownPromotions" :key="p.promotionId">
            <td class="px-4 py-3">
              <span class="font-medium text-gray-800">{{ p.code }}</span>
              <span v-if="p.description" class="block text-xs text-gray-500">{{ p.description }}</span>
            </td>
            <td class="px-4 py-3 text-gray-600">
              {{ p.discountType === 'PERCENTAGE' ? `${p.discountValue}%` : `R${p.discountValue.toFixed(2)}` }}
            </td>
            <td class="px-4 py-3 text-gray-600">{{ p.startDate }} to {{ p.endDate }}</td>
            <td class="px-4 py-3 tabular-nums text-gray-800">
              {{ statsFor(p).redeemed }} <span class="text-gray-400">of {{ p.usageLimit }}</span>
              <span v-if="statsFor(p).refunded" class="block text-xs text-gray-400">{{ statsFor(p).refunded }} refunded</span>
            </td>
            <td class="px-4 py-3 tabular-nums text-gray-600">{{ statsFor(p).heldByBookings }}</td>
            <td class="px-4 py-3 tabular-nums text-gray-600">{{ statsFor(p).remaining }}</td>
            <td class="px-4 py-3">
              <span :class="STATUS[statsFor(p).status].cls" class="rounded-full px-2.5 py-1 text-xs font-medium">
                {{ STATUS[statsFor(p).status].label }}
              </span>
            </td>
            <td class="space-x-3 whitespace-nowrap px-4 py-3 text-right">
              <template v-if="session.can('PROMOTIONS', 'EDIT')">
                <button class="font-medium text-brand-600 hover:text-brand-700" @click="openEdit(p)">Edit</button>
                <button
                  v-if="!p.ended && ['ACTIVE', 'SCHEDULED', 'FULLY_USED'].includes(statsFor(p).status)"
                  class="text-warning-700 hover:text-warning-800"
                  @click="endNow(p)"
                >End now</button>
                <button
                  v-else-if="p.ended && p.endDate >= today"
                  class="font-medium text-brand-600 hover:text-brand-700"
                  @click="reopen(p)"
                >Reopen</button>
              </template>
              <button v-if="session.can('PROMOTIONS', 'DELETE')" class="text-error-600 hover:text-error-700" @click="remove(p)">Delete</button>
            </td>
          </tr>
          <tr v-if="!shownPromotions.length">
            <td colspan="8" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering">
                No promotions match &ldquo;{{ searchQuery.trim() }}&rdquo;.
                <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="resetSearch">Clear search</button>
              </template>
              <template v-else>No promotions yet.</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import { adminMenu } from '@/config/menus'
import { promotionsApi, type PromotionStats } from '@/api/promotions'
import { ApiError } from '@/api/client'
import { useSectionSearch } from '@/composables/useSectionSearch'
import { useSession } from '@/composables/useSession'
import type { DiscountType, Promotion } from '@/api/types'

const inputClass =
  'h-10 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

/** Labels for the status the SERVER computes (PromotionOfferService.statusOf). */
const STATUS: Record<PromotionStats['status'], { label: string; cls: string }> = {
  ACTIVE: { label: 'Active', cls: 'bg-success-50 text-success-700' },
  SCHEDULED: { label: 'Scheduled', cls: 'bg-gray-100 text-gray-600' },
  FULLY_USED: { label: 'Fully used', cls: 'bg-warning-50 text-warning-700' },
  EXPIRED: { label: 'Expired', cls: 'bg-gray-100 text-gray-600' },
  ENDED: { label: 'Ended early', cls: 'bg-gray-100 text-gray-600' },
}
const REFRESH_MS = 30_000

const session = useSession()
const promotions = ref<Promotion[]>([])
const stats = ref<Record<string, PromotionStats>>({})
const loading = ref(true)
const refreshing = ref(false)
const updatedAt = ref('')
const submitting = ref(false)
const showForm = ref(false)
const editing = ref<Promotion | null>(null)
const errorMessage = ref('')
const successMessage = ref('')
const formErrors = ref<string[]>([])
const today = new Date().toLocaleDateString('en-CA') // YYYY-MM-DD, local time

const { query: searchQuery, results: shownPromotions, isFiltering, reset: resetSearch } = useSectionSearch(
  promotions,
  (p) => [p.code, p.description],
)

const emptyForm = () => ({
  code: '',
  description: '',
  discountType: 'PERCENTAGE' as DiscountType,
  discountValue: null as number | null,
  startDate: today,
  endDate: '',
  usageLimit: 100 as number | null,
})
const form = reactive(emptyForm())

/** Stats for one row; until the first stats call returns, fall back to the stored fields. */
function statsFor(p: Promotion): PromotionStats {
  return (
    stats.value[p.promotionId] ?? {
      promotionId: p.promotionId,
      code: p.code,
      redeemed: p.usageCount,
      heldByBookings: 0,
      refunded: 0,
      usageLimit: p.usageLimit,
      remaining: Math.max(0, p.usageLimit - p.usageCount),
      status: p.ended ? 'ENDED' : 'ACTIVE',
    }
  )
}

/**
 * Promotions and their figures are fetched together, so the table never mixes a fresh list with
 * stale counts. Runs on load, every 30 s while the tab is visible, when the tab regains focus,
 * and after every change - so "Used" moves soon after a payment is recorded anywhere.
 */
async function load(initial = true) {
  if (refreshing.value) return
  if (initial) loading.value = true
  refreshing.value = true
  try {
    // The figures are extra: if they can't be loaded the table still shows, using the stored counts.
    const [list, figures] = await Promise.all([promotionsApi.list(), promotionsApi.stats().catch(() => [])])
    promotions.value = list.sort((a, b) => b.startDate.localeCompare(a.startDate))
    stats.value = Object.fromEntries(figures.map((s) => [s.promotionId, s]))
    updatedAt.value = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    if (initial) errorMessage.value = ''
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load promotions.'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

let timer: ReturnType<typeof setInterval> | undefined
function onVisible() {
  if (document.visibilityState === 'visible') load(false)
}
onMounted(() => {
  load()
  timer = setInterval(() => document.visibilityState === 'visible' && load(false), REFRESH_MS)
  document.addEventListener('visibilitychange', onVisible)
  window.addEventListener('focus', onVisible)
})
onBeforeUnmount(() => {
  clearInterval(timer)
  document.removeEventListener('visibilitychange', onVisible)
  window.removeEventListener('focus', onVisible)
})

function openCreate() {
  Object.assign(form, emptyForm())
  editing.value = null
  formErrors.value = []
  showForm.value = true
}

function openEdit(p: Promotion) {
  editing.value = p
  Object.assign(form, {
    code: p.code,
    description: p.description ?? '',
    discountType: p.discountType,
    discountValue: p.discountValue,
    startDate: p.startDate,
    endDate: p.endDate,
    usageLimit: p.usageLimit,
  })
  formErrors.value = []
  showForm.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function closeForm() {
  showForm.value = false
  editing.value = null
  formErrors.value = []
}

/** Same rules as PromotionAdminService, checked first so mistakes show without a round trip. */
function validate(): string[] {
  const errors: string[] = []
  const code = form.code.trim().toUpperCase()
  if (!/^[A-Z0-9_-]{3,20}$/.test(code)) errors.push('The code must be 3 to 20 letters, numbers, - or _ (no spaces).')
  const value = Number(form.discountValue)
  if (!form.discountValue || !(value > 0)) errors.push('The discount must be more than zero.')
  else if (form.discountType === 'PERCENTAGE' && value > 100) errors.push("A percentage discount can't be more than 100%.")
  else if (Math.abs(Math.round(value * 100) - value * 100) > 1e-6) errors.push('The discount can have at most two decimal places.')
  if (!form.startDate || !form.endDate) errors.push('Choose a start date and an end date.')
  else if (form.endDate < form.startDate) errors.push("The end date can't be before the start date.")
  else if (!editing.value && form.endDate < today) errors.push('The end date has already passed, so this promotion would never run.')
  const limit = Number(form.usageLimit)
  if (!Number.isInteger(limit) || limit < 1) errors.push('The usage limit must be a whole number, at least 1.')
  else if (editing.value && limit < editing.value.usageCount)
    errors.push(`The usage limit can't be lower than the ${editing.value.usageCount} uses already counted.`)
  return errors
}

async function save() {
  formErrors.value = validate()
  if (formErrors.value.length) return
  submitting.value = true
  successMessage.value = ''
  const payload = {
    code: form.code.trim().toUpperCase(),
    description: form.description.trim() || null,
    discountType: form.discountType,
    discountValue: Number(form.discountValue),
    startDate: form.startDate,
    endDate: form.endDate,
    usageLimit: Number(form.usageLimit),
  }
  try {
    if (editing.value) {
      await promotionsApi.update(editing.value.promotionId, payload)
      successMessage.value = `Promotion ${payload.code} updated.`
    } else {
      await promotionsApi.create(payload)
      successMessage.value = `Promotion ${payload.code} added.`
    }
    closeForm()
    await load(false)
  } catch (err) {
    // The server re-validates; show its sentence if it disagrees (e.g. a duplicate code).
    formErrors.value = [err instanceof ApiError ? err.message : 'Could not save this promotion.']
  } finally {
    submitting.value = false
  }
}

async function act(fn: () => Promise<unknown>, done: string, failed: string) {
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await fn()
    successMessage.value = done
    await load(false)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : failed
  }
}

function endNow(p: Promotion) {
  if (!confirm(`End ${p.code} now? It stops working immediately, including for bookings that already hold it. You can reopen it later.`)) return
  act(() => promotionsApi.end(p.promotionId), `${p.code} has ended.`, 'Could not end this promotion.')
}

function reopen(p: Promotion) {
  act(() => promotionsApi.reopen(p.promotionId), `${p.code} is running again.`, 'Could not reopen this promotion.')
}

function remove(p: Promotion) {
  if (!confirm(`Delete promotion ${p.code}? Promotions that have been used or booked can only be ended.`)) return
  act(() => promotionsApi.delete(p.promotionId), `Promotion ${p.code} deleted.`, 'Could not delete this promotion.')
}
</script>
