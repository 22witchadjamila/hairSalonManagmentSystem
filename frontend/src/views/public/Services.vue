<template>
  <PublicLayout>
    <section class="site-section pb-0">
      <div class="site-container max-w-3xl">
        <SectionHeading eyebrow="Price list" level="h1">
          Our services
          <template #lead>
            Expert care, modern styles and treatments that keep hair in good condition between
            visits. Everything is payable in store when you arrive &mdash; no card details needed
            to book.
          </template>
        </SectionHeading>
      </div>
    </section>

    <section class="site-section">
      <div class="site-container">
        <p v-if="loading" class="text-center text-sm text-ink-muted">
          Loading the price list&hellip;
        </p>

        <!-- Error gets a retry, not a dead end. -->
        <div v-else-if="errorMessage" class="mx-auto max-w-md text-center">
          <p class="text-base text-ink">{{ errorMessage }}</p>
          <button type="button" class="site-btn-outline mt-6" @click="load">Try again</button>
        </div>

        <p v-else-if="!services.length" class="text-center text-sm text-ink-muted">
          Our price list is being updated &mdash; please check back shortly, or call the salon.
        </p>

        <div v-else class="space-y-20">
          <section v-for="group in groupedServices" :key="group.category">
            <div class="grid gap-10 lg:grid-cols-[18rem_1fr] lg:gap-14">
              <div data-reveal>
                <AppImage
                  :photo="serviceCategoryPhoto(group.category)"
                  ratio="4 / 3"
                  class="rounded-2xl"
                  sizes="(max-width: 1024px) 100vw, 18rem"
                />
                <h2 class="site-h2 mt-6 text-2xl sm:text-3xl">{{ formatCategory(group.category) }}</h2>
              </div>

              <ul class="divide-y divide-ink-line border-t border-ink-line">
                <li
                  v-for="service in group.services"
                  :key="service.serviceId"
                  class="flex flex-wrap items-baseline justify-between gap-x-6 gap-y-3 py-6"
                >
                  <div class="min-w-[12rem] flex-1">
                    <h3 class="text-lg font-medium">{{ service.name }}</h3>
                    <p
                      v-if="service.description"
                      class="mt-1 text-sm leading-relaxed text-ink-muted"
                    >
                      {{ service.description }}
                    </p>
                    <p class="mt-1 text-sm text-ink-muted">
                      {{ service.durationMinutes }} min
                    </p>
                  </div>

                  <div class="flex items-center gap-6">
                    <span class="text-lg font-medium tabular-nums">
                      R{{ service.price.toFixed(2) }}
                    </span>
                    <router-link
                      to="/book-appointment"
                      class="site-btn-outline px-5 py-3"
                      :aria-label="'Book ' + service.name"
                    >
                      Book
                    </router-link>
                  </div>
                </li>
              </ul>
            </div>
          </section>
        </div>
      </div>
    </section>

    <section class="site-section bg-ink text-cream">
      <div class="site-container text-center">
        <h2 class="site-h2">Not sure which one you need?</h2>
        <p class="mx-auto mt-5 max-w-xl text-base leading-relaxed text-cream/75">
          Book a cut. Every appointment opens with a consultation, so we can change the plan before
          we start if something else suits you better.
        </p>
        <router-link to="/book-appointment" class="site-btn-light mt-10">
          Book an appointment
        </router-link>
      </div>
    </section>
  </PublicLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PublicLayout from '@/components/layout/PublicLayout.vue'
import AppImage from '@/components/public/AppImage.vue'
import SectionHeading from '@/components/public/SectionHeading.vue'
import { servicesApi } from '@/api/services'
import { ApiError } from '@/api/client'
import type { SalonService } from '@/api/types'
import { serviceCategoryPhoto } from '@/config/images'

const services = ref<SalonService[]>([])
const loading = ref(true)
const errorMessage = ref('')

const groupedServices = computed(() => {
  const byCategory = new Map<string, SalonService[]>()
  for (const service of services.value) {
    const list = byCategory.get(service.category) ?? []
    list.push(service)
    byCategory.set(service.category, list)
  }
  return Array.from(byCategory.entries()).map(([category, services]) => ({ category, services }))
})

function formatCategory(category: string) {
  return category.charAt(0) + category.slice(1).toLowerCase()
}

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    services.value = await servicesApi.listActive()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load the price list.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
