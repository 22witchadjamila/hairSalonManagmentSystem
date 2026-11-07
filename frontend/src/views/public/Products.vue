<template>
  <PublicLayout>
    <section class="site-section pb-0">
      <div class="site-container max-w-3xl">
        <SectionHeading eyebrow="Take care at home" level="h1">
          Products
          <template #lead>
            The products we use and recommend, available to buy in the salon. Ask your stylist
            which suits your hair.
          </template>
        </SectionHeading>
      </div>
    </section>

    <section class="site-section">
      <div class="site-container">
        <p v-if="loading" class="text-center text-sm text-ink-muted">Loading products&hellip;</p>

        <div v-else-if="errorMessage" class="mx-auto max-w-md text-center">
          <p class="text-base text-ink">{{ errorMessage }}</p>
          <button type="button" class="site-btn-outline mt-6" @click="load">Try again</button>
        </div>

        <p v-else-if="!products.length" class="text-center text-sm text-ink-muted">
          Our retail shelf is being restocked &mdash; ask in the salon about what we have in.
        </p>

        <ul v-else class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <li
            v-for="p in products"
            :key="p.productId"
            data-reveal
            class="flex flex-col rounded-2xl border border-ink-line p-6"
          >
            <p v-if="p.category" class="site-eyebrow">{{ p.category }}</p>
            <h2 class="mt-2 text-lg font-medium">{{ p.name }}</h2>
            <p v-if="p.brand" class="mt-1 text-sm text-ink-muted">{{ p.brand }}</p>
            <div class="mt-auto flex items-baseline justify-between gap-4 pt-6">
              <span class="text-lg font-medium tabular-nums">
                {{ p.price != null ? `R${p.price.toFixed(2)}` : 'Ask in salon' }}
              </span>
              <!-- Availability in words, not colour alone. Exact stock counts stay internal. -->
              <span class="text-xs font-semibold uppercase tracking-[0.14em]" :class="p.availability === 'OUT_OF_STOCK' ? 'text-ink-muted' : 'text-ink'">
                {{ availabilityLabel[p.availability] }}
              </span>
            </div>
          </li>
        </ul>

        <p v-if="products.length" class="mt-12 text-center text-sm text-ink-muted">
          Available to buy in the salon &mdash; we don't sell online.
        </p>
      </div>
    </section>
  </PublicLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PublicLayout from '@/components/layout/PublicLayout.vue'
import SectionHeading from '@/components/public/SectionHeading.vue'
import { productsApi, type ShopProduct } from '@/api/products'
import { ApiError } from '@/api/client'

const products = ref<ShopProduct[]>([])
const loading = ref(true)
const errorMessage = ref('')

const availabilityLabel: Record<ShopProduct['availability'], string> = {
  IN_STOCK: 'In stock',
  LOW_STOCK: 'Only a few left',
  OUT_OF_STOCK: 'Out of stock',
}

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    // Public endpoint: retail products only, with no cost price, supplier link or stock count.
    products.value = await productsApi.shop()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load products.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
