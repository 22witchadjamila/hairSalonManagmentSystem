<template>
  <PublicLayout>
    <section class="site-section pb-0">
      <div class="site-container max-w-3xl">
        <SectionHeading eyebrow="The team" level="h1">
          Meet our stylists
          <template #lead>
            Pick a favourite when you book, or leave it to us and we will match you to whoever
            suits your hair and your time slot best.
          </template>
        </SectionHeading>
      </div>
    </section>

    <section class="site-section">
      <div class="site-container">
        <p v-if="loading" class="text-center text-sm text-ink-muted">
          Loading the team&hellip;
        </p>

        <div v-else-if="errorMessage" class="mx-auto max-w-md text-center">
          <p class="text-base text-ink">{{ errorMessage }}</p>
          <button type="button" class="site-btn-outline mt-6" @click="load">Try again</button>
        </div>

        <p v-else-if="!stylists.length" class="text-center text-sm text-ink-muted">
          Our team list is being updated &mdash; please check back shortly, or call the salon.
        </p>

        <ul v-else class="grid gap-x-6 gap-y-12 sm:grid-cols-2 lg:grid-cols-3">
          <li v-for="stylist in stylists" :key="stylist.stylistId" data-reveal>
            <AppImage
              :photo="stylistPhoto(stylist.stylistId)"
              ratio="3 / 4"
              hover-zoom
              class="rounded-2xl"
              sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
            />

            <h2 class="mt-5 text-xl font-medium">
              {{ stylist.firstName }} {{ stylist.lastName }}
            </h2>
            <p class="site-eyebrow mt-1">{{ stylist.speciality }}</p>

            <router-link
              to="/book-appointment"
              class="site-btn-outline mt-5"
              :aria-label="'Book with ' + stylist.firstName + ' ' + stylist.lastName"
            >
              Book with {{ stylist.firstName }}
            </router-link>
          </li>
        </ul>

        <p v-if="stylists.length" class="mt-14 text-center text-sm text-ink-muted">
          Portraits are placeholders from Unsplash until we photograph the team.
        </p>
      </div>
    </section>
  </PublicLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PublicLayout from '@/components/layout/PublicLayout.vue'
import AppImage from '@/components/public/AppImage.vue'
import SectionHeading from '@/components/public/SectionHeading.vue'
import { stylistsApi, type PublicStylist } from '@/api/stylists'
import { ApiError } from '@/api/client'
import { stylistPhoto } from '@/config/images'

const stylists = ref<PublicStylist[]>([])
const loading = ref(true)
const errorMessage = ref('')

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    stylists.value = await stylistsApi.listActive()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load the team.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
