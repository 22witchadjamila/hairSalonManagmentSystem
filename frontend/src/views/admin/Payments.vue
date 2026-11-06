<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-2xl font-semibold text-gray-800">Payments</h1>
        <p class="mt-1 text-sm text-gray-500">
          Customers pay in the salon. Record each payment when you receive it &mdash; the price comes
          from the booked service and any promotion is applied automatically.
        </p>
      </div>
      <button
        v-if="session.can('PAYMENTS', 'CREATE')"
        @click="showForm = !showForm"
        class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
      >
        {{ showForm ? 'Cancel' : '+ Record payment' }}
      </button>
    </div>
    <SectionSearch v-model="searchQuery" class="mt-4" :total="payments.length" :shown="shownPayments.length" placeholder="Search customer, service, code, status…" />


    <!-- Record an in-store payment. No amount field: the server prices it. -->
    <form v-if="showForm" @submit.prevent="record" class="mt-4 grid grid-cols-1 gap-3 rounded-lg border border-gray-200 p-4 sm:grid-cols-4">
      <div class="sm:col-span-2">
        <label for="pay-appt" class="mb-1 block text-xs font-medium text-gray-600">Appointment</label>
        <select id="pay-appt" v-model="form.appointmentId" required :class="inputClass" @change="onPickAppointment">
          <option value="" disabled>Choose an unpaid appointment</option>
          <option v-for="a in payable" :key="a.appointmentId" :value="a.appointmentId">
            {{ a.appointmentDate }} {{ a.startTime.slice(0, 5) }} &middot; {{ a.customer.firstName }} {{ a.customer.lastName }}
            &middot; {{ a.salonService.name }} (R{{ a.salonService.price.toFixed(2) }})
          </option>
        </select>
        <p v-if="!payable.length" class="mt-1 text-xs text-gray-500">Every confirmed or completed appointment is already paid.</p>
      </div>
      <div>
        <label for="pay-method" class="mb-1 block text-xs font-medium text-gray-600">Paid by</label>
        <select id="pay-method" v-model="form.method" required :class="inputClass">
          <option value="CASH">Cash</option>
          <option value="CARD">Card</option>
          <option value="EFT">EFT</option>
          <option value="VOUCHER">Voucher</option>
        </select>
      </div>
      <div>
        <label for="pay-promo" class="mb-1 block text-xs font-medium text-gray-600">Promotion code</label>
        <input id="pay-promo" v-model="form.promoCode" placeholder="None" :class="inputClass" />
      </div>
      <div v-if="account && rules && account.pointsBalance >= rules.minRedeemPoints">
        <label for="pay-points" class="mb-1 block text-xs font-medium text-gray-600">
          Use loyalty points ({{ account.pointsBalance }} available)
        </label>
        <input id="pay-points" v-model.number="form.redeemPoints" type="number" step="1"
          :min="rules.minRedeemPoints" :max="account.pointsBalance" placeholder="None" :class="inputClass" />
      </div>
      <p v-if="account && rules && account.pointsBalance < rules.minRedeemPoints" class="text-xs text-gray-500">
        {{ account.pointsBalance }} loyalty points &mdash; {{ rules.minRedeemPoints }} are needed to use them.
      </p>
      <p class="text-xs text-gray-500 sm:col-span-3">
        <template v-if="selected">
          Price R{{ selected.salonService.price.toFixed(2) }}<template v-if="form.promoCode.trim()">, less {{ form.promoCode.trim().toUpperCase() }} &mdash; the discount is calculated when you record it</template><template v-if="form.redeemPoints && rules">, less R{{ (form.redeemPoints * rules.randPerPoint).toFixed(2) }} for {{ form.redeemPoints }} points</template>.
        </template>
      </p>
      <button type="submit" :disabled="submitting || !form.appointmentId"
        class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
        {{ submitting ? 'Recording…' : 'Record as paid' }}
      </button>
    </form>

    <p v-if="errorMessage" role="alert" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="successMessage" role="status" class="mt-3 text-sm text-success-700">{{ successMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>

    <div v-else class="mt-6 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Customer</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Service</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Price</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Discount</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Paid</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Method</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Status</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">When</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="p in shownPayments" :key="p.paymentId">
            <td class="px-4 py-3 font-medium text-gray-800">{{ p.appointment.customer.firstName }} {{ p.appointment.customer.lastName }}</td>
            <td class="px-4 py-3 text-gray-600">{{ p.appointment.salonService.name }}</td>
            <td class="px-4 py-3 text-gray-600 tabular-nums">R{{ p.amount.toFixed(2) }}</td>
            <td class="px-4 py-3 text-gray-600 tabular-nums">
              <template v-if="p.discount > 0">&minus;R{{ p.discount.toFixed(2) }} <span class="text-xs text-gray-400">{{ p.promotion?.code }}</span><span v-if="p.loyaltyPointsRedeemed" class="block text-xs text-gray-400">{{ p.loyaltyPointsRedeemed }} pts</span></template>
              <template v-else>&mdash;</template>
            </td>
            <td class="px-4 py-3 font-medium text-gray-800 tabular-nums">R{{ p.finalAmount.toFixed(2) }}</td>
            <td class="px-4 py-3 text-gray-600">{{ p.method }}</td>
            <td class="px-4 py-3">
              <span :class="statusClass[p.status]" class="rounded-full px-2.5 py-1 text-xs font-medium">{{ statusLabel[p.status] }}</span>
            </td>
            <td class="px-4 py-3 text-xs text-gray-500">{{ p.paidAt ? p.paidAt.replace('T', ' ').slice(0, 16) : '—' }}</td>
            <td class="px-4 py-3 text-right">
              <button v-if="p.status === 'PAID' && session.can('PAYMENTS', 'EDIT')" @click="refund(p)" class="text-error-600 hover:text-error-700">Refund</button>
            </td>
          </tr>
          <tr v-if="!shownPayments.length">
            <td colspan="9" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering">No payments match &ldquo;{{ searchQuery.trim() }}&rdquo;. <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="resetSearch">Clear search</button></template>
              <template v-else>No payments recorded yet.</template>
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
import { paymentsApi } from '@/api/payments'
import { appointmentsApi } from '@/api/appointments'
import { ApiError } from '@/api/client'
import type { Appointment, LoyaltyReward, Payment, PaymentMethod, PaymentStatus } from '@/api/types'
import { loyaltyApi, type LoyaltyRules } from '@/api/loyalty'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import { useSectionSearch } from '@/composables/useSectionSearch'
import { useSession } from '@/composables/useSession'

const inputClass =
  'h-10 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

const payments = ref<Payment[]>([])
const appointments = ref<Appointment[]>([])
const loading = ref(true)
const errorMessage = ref('')
const successMessage = ref('')
const showForm = ref(false)
const submitting = ref(false)
const form = reactive({ appointmentId: '', method: 'CASH' as PaymentMethod, promoCode: '', redeemPoints: null as number | null })
// The chosen customer's loyalty account, for the "use points" field. Only loaded for roles that may
// view loyalty - others simply don't see the field.
const account = ref<LoyaltyReward | null>(null)
const rules = ref<LoyaltyRules | null>(null)

// Colour plus a word - status is never conveyed by colour alone.
const statusLabel: Record<PaymentStatus, string> = { PAID: 'Paid', PENDING: 'Pending', REFUNDED: 'Refunded', FAILED: 'Failed' }
const statusClass: Record<PaymentStatus, string> = {
  PAID: 'bg-success-50 text-success-700',
  PENDING: 'bg-warning-50 text-warning-700',
  REFUNDED: 'bg-gray-100 text-gray-600',
  FAILED: 'bg-error-50 text-error-700',
}

const sortedPayments = computed(() =>
  [...payments.value].sort((a, b) => (b.paidAt ?? '').localeCompare(a.paidAt ?? '')),
)
const session = useSession()
// Section search - this table only. The first field is the primary one and ranks highest.
const { query: searchQuery, results: shownPayments, isFiltering, reset: resetSearch } = useSectionSearch(sortedPayments, (p) => [
  `${p.appointment?.customer?.firstName} ${p.appointment?.customer?.lastName}`,
  p.appointment?.salonService?.name,
  p.promotion?.code,
  p.status,
  p.method,
  p.paidAt?.slice(0, 10),
])

/** Appointments that can still be paid: not cancelled / no-show, and no PAID payment yet. */
const payable = computed(() => {
  const paid = new Set(payments.value.filter((p) => p.status === 'PAID').map((p) => p.appointment.appointmentId))
  return appointments.value
    .filter((a) => !['CANCELLED', 'NO_SHOW'].includes(a.status) && !paid.has(a.appointmentId))
    .sort((a, b) => b.appointmentDate.localeCompare(a.appointmentDate))
})

const selected = computed(() => appointments.value.find((a) => a.appointmentId === form.appointmentId) ?? null)

/** Pre-fill the code the customer chose when booking; staff can clear or change it. */
function prefillPromo() {
  form.promoCode = selected.value?.promoCode ?? ''
}

async function onPickAppointment() {
  prefillPromo()
  form.redeemPoints = null
  account.value = null
  const customerId = selected.value?.customer?.customerId
  if (!customerId || !session.can('LOYALTY')) return
  try {
    ;[account.value, rules.value] = await Promise.all([
      loyaltyApi.getOrCreate(customerId),
      rules.value ? Promise.resolve(rules.value) : loyaltyApi.rules(),
    ])
  } catch {
    account.value = null   // not essential: the payment can still be recorded without points
  }
}

async function load() {
  try {
    ;[payments.value, appointments.value] = await Promise.all([
      paymentsApi.list(),
      session.can('APPOINTMENTS') ? appointmentsApi.list() : Promise.resolve([] as Appointment[]),
    ])
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load payments.'
  } finally {
    loading.value = false
  }
}

async function record() {
  submitting.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    // '' means "no promotion"; the server re-validates any code and does the arithmetic.
    const p = await paymentsApi.record(form.appointmentId, form.method, form.promoCode.trim(), form.redeemPoints)
    payments.value = [p, ...payments.value]
    successMessage.value = `Recorded R${p.finalAmount.toFixed(2)} from ${p.appointment.customer.firstName} ${p.appointment.customer.lastName}.`
    Object.assign(form, { appointmentId: '', method: 'CASH', promoCode: '', redeemPoints: null })
    account.value = null
    showForm.value = false
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not record this payment.'
  } finally {
    submitting.value = false
  }
}

async function refund(p: Payment) {
  if (!confirm(`Refund R${p.finalAmount.toFixed(2)} to ${p.appointment.customer.firstName}?`)) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const updated = await paymentsApi.refund(p.paymentId)
    payments.value = payments.value.map((x) => (x.paymentId === updated.paymentId ? updated : x))
    successMessage.value = 'Payment marked as refunded.'
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not refund this payment.'
  }
}

onMounted(load)
</script>
