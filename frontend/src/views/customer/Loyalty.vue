<template>
  <AdminLayout :menu-groups="customerMenu">
    <h1 class="text-2xl font-semibold text-gray-800">Loyalty Rewards</h1>

    <div v-if="!session.isCustomer.value" class="mt-4 rounded-lg border border-gray-200 p-6 text-sm text-gray-500">
      We don't know who you are yet &mdash;
      <router-link to="/login" class="text-brand-500 hover:text-brand-600">log in</router-link>
      to see your rewards.
    </div>

    <template v-else>
      <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>
      <p v-else-if="errorMessage" role="alert" class="mt-4 text-sm text-error-600">{{ errorMessage }}</p>

      <template v-else-if="loyalty">
        <div class="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div class="rounded-xl border border-gray-200 p-5">
            <div class="text-sm text-gray-500">Tier</div>
            <div class="mt-1 text-xl font-semibold text-gray-800">{{ loyalty.tier }}</div>
            <div v-if="next" class="mt-1 text-xs text-gray-500">
              {{ next.needed }} more {{ next.needed === 1 ? 'point' : 'points' }} earned to reach {{ next.name }}
            </div>
            <div v-else-if="rules" class="mt-1 text-xs text-gray-500">You're at the top tier.</div>
          </div>
          <div class="rounded-xl border border-gray-200 p-5">
            <div class="text-sm text-gray-500">Points balance</div>
            <div class="mt-1 text-xl font-semibold text-gray-800">{{ loyalty.pointsBalance }}</div>
            <div v-if="rules" class="mt-1 text-xs text-gray-500">
              Worth R{{ (loyalty.pointsBalance * rules.randPerPoint).toFixed(2) }} off a bill
            </div>
          </div>
          <div class="rounded-xl border border-gray-200 p-5">
            <div class="text-sm text-gray-500">Lifetime earned</div>
            <div class="mt-1 text-xl font-semibold text-gray-800">{{ loyalty.totalEarned }}</div>
          </div>
        </div>
        <p class="mt-2 text-xs text-gray-400">
          {{ loyalty.totalRedeemed }} spent so far &middot; last updated
          {{ loyalty.lastUpdated ? loyalty.lastUpdated.split('T')[0] : 'never' }}
        </p>

        <div v-if="rules" class="mt-8 max-w-2xl rounded-xl border border-gray-200 p-5">
          <h2 class="text-sm font-semibold text-gray-800">How it works</h2>
          <ul class="mt-2 list-disc space-y-1.5 pl-5 text-sm text-gray-600">
            <li>
              <strong class="font-medium text-gray-800">Earn:</strong> 1 point for every R{{ rules.randSpentPerPoint }}
              you pay in the salon. Points are added when your payment is recorded &mdash; nothing to claim.
            </li>
            <li>
              <strong class="font-medium text-gray-800">Spend:</strong> each point takes R{{ rules.randPerPoint.toFixed(2) }}
              off your bill, from {{ rules.minRedeemPoints }} points (R{{ (rules.minRedeemPoints * rules.randPerPoint).toFixed(2) }}).
              Ask your stylist to use your points when you pay &mdash; they're taken off at the till, so they can't
              be used twice.
            </li>
            <li>
              <strong class="font-medium text-gray-800">Tiers</strong> follow the points you've ever earned: Silver from
              {{ rules.silverFrom }}, Gold from {{ rules.goldFrom }}, Platinum from {{ rules.platinumFrom }}.
              Spending points doesn't lower your tier.
            </li>
            <li>If a payment is refunded, the points it earned are taken back and the points it spent are returned.</li>
          </ul>
        </div>
      </template>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { customerMenu } from '@/config/menus'
import { loyaltyApi, type LoyaltyRules } from '@/api/loyalty'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import type { LoyaltyReward } from '@/api/types'

const session = useSession()
const loyalty = ref<LoyaltyReward | null>(null)
const rules = ref<LoyaltyRules | null>(null)
const loading = ref(true)
const errorMessage = ref('')

/** The next tier up and how many more earned points it needs; null at Platinum. */
const next = computed(() => {
  if (!loyalty.value || !rules.value) return null
  const earned = loyalty.value.totalEarned
  const steps = [
    { name: 'Silver', from: rules.value.silverFrom },
    { name: 'Gold', from: rules.value.goldFrom },
    { name: 'Platinum', from: rules.value.platinumFrom },
  ]
  const step = steps.find((s) => earned < s.from)
  return step ? { name: step.name, needed: step.from - earned } : null
})

onMounted(async () => {
  const customerId = session.customer.value?.customerId
  if (!customerId) {
    loading.value = false
    return
  }
  try {
    // The rules are an extra: if they can't be read the balance still shows.
    ;[loyalty.value, rules.value] = await Promise.all([
      loyaltyApi.getOrCreate(customerId),
      loyaltyApi.rules().catch(() => null),
    ])
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load your loyalty rewards.'
  } finally {
    loading.value = false
  }
})
</script>
