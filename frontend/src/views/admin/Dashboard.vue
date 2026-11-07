<template>
  <AdminLayout :menu-groups="adminMenu">
    <h1 class="text-2xl font-semibold text-gray-800">Admin Dashboard</h1>

    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading overview&hellip;</p>
    <p v-else-if="errorMessage" class="mt-4 text-sm text-error-600">{{ errorMessage }}</p>

    <template v-else>
      <div class="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div class="rounded-xl border border-gray-200 p-5">
          <div class="text-sm text-gray-500">Customers</div>
          <div class="mt-1 text-2xl font-semibold text-gray-800">{{ customers.length }}</div>
        </div>
        <div class="rounded-xl border border-gray-200 p-5">
          <div class="text-sm text-gray-500">Stylists</div>
          <div class="mt-1 text-2xl font-semibold text-gray-800">{{ stylists.length }}</div>
        </div>
        <div class="rounded-xl border border-gray-200 p-5">
          <div class="text-sm text-gray-500">Appointments</div>
          <div class="mt-1 text-2xl font-semibold text-gray-800">{{ appointments.length }}</div>
        </div>
        <div class="rounded-xl border border-gray-200 p-5">
          <!-- From GET /payments/summary: the server totals the actual payment rows, and only
               PAID ones count as revenue - pending, failed and refunded are reported apart. -->
          <div class="text-sm text-gray-500">Revenue collected</div>
          <div class="mt-1 text-2xl font-semibold text-gray-800 tabular-nums">R{{ (summary?.collected ?? 0).toFixed(2) }}</div>
          <div v-if="summary" class="mt-1 text-xs text-gray-500">
            {{ summary.paidCount }} paid
            <template v-if="summary.refundedCount"> &middot; {{ summary.refundedCount }} refunded</template>
            <template v-if="summary.pendingCount"> &middot; {{ summary.pendingCount }} pending</template>
            <template v-if="summary.discountsGiven > 0"> &middot; R{{ summary.discountsGiven.toFixed(2) }} in discounts</template>
          </div>
        </div>
      </div>

      <div class="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div>
          <h2 class="text-sm font-semibold text-gray-800">Recent appointments</h2>
          <p v-if="!recentAppointments.length" class="mt-2 text-sm text-gray-500">No appointments yet.</p>
          <div v-else class="mt-3 space-y-3">
            <div
              v-for="appt in recentAppointments"
              :key="appt.appointmentId"
              class="flex items-center justify-between rounded-lg border border-gray-200 p-3 text-sm"
            >
              <div>
                <div class="font-medium text-gray-800">
                  {{ appt.customer.firstName }} {{ appt.customer.lastName }} &mdash; {{ appt.salonService.name }}
                </div>
                <div class="mt-0.5 text-gray-500">
                  {{ appt.appointmentDate }} &middot; {{ appt.startTime }} with {{ appt.stylist.firstName }}
                </div>
              </div>
              <span class="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-medium text-gray-600">
                {{ appt.status }}
              </span>
            </div>
          </div>
        </div>

        <div>
          <h2 class="text-sm font-semibold text-gray-800">Low stock</h2>
          <p v-if="!lowStock.length" class="mt-2 text-sm text-gray-500">Nothing running low.</p>
          <div v-else class="mt-3 space-y-3">
            <div
              v-for="product in lowStock"
              :key="product.productId"
              class="flex items-center justify-between rounded-lg border border-warning-200 bg-warning-50 p-3 text-sm"
            >
              <div class="font-medium text-gray-800">{{ product.name }}</div>
              <div class="text-gray-600">{{ product.stockQuantity }} left (reorder at {{ product.reorderLevel }})</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { adminMenu } from '@/config/menus'
import { customersApi } from '@/api/customers'
import { stylistsApi } from '@/api/stylists'
import { appointmentsApi } from '@/api/appointments'
import { paymentsApi, type PaymentSummary } from '@/api/payments'
import { productsApi } from '@/api/products'
import { ApiError } from '@/api/client'
import type { Appointment, Customer, ProductInventory, Stylist } from '@/api/types'

const customers = ref<Customer[]>([])
const stylists = ref<Stylist[]>([])
const appointments = ref<Appointment[]>([])
const summary = ref<PaymentSummary | null>(null)
const lowStock = ref<ProductInventory[]>([])
const loading = ref(true)
const errorMessage = ref('')


const recentAppointments = computed(() =>
  [...appointments.value]
    .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
    .slice(0, 5),
)

onMounted(async () => {
  try {
    const [c, s, a, p, low] = await Promise.all([
      customersApi.list(),
      stylistsApi.list(),
      appointmentsApi.list(),
      paymentsApi.summary(),
      productsApi.lowStock(),
    ])
    customers.value = c
    stylists.value = s
    appointments.value = a
    summary.value = p
    lowStock.value = low
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load the dashboard.'
  } finally {
    loading.value = false
  }
})
</script>
