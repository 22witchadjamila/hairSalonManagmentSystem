<template>
  <PublicLayout>
    <section class="site-section pb-0">
      <div class="site-container max-w-3xl">
        <SectionHeading eyebrow="Get in touch" level="h1">
          Contact us
          <template #lead>
            Call during opening hours, or book online any time &mdash; it takes about a minute and
            we will confirm shortly afterwards.
          </template>
        </SectionHeading>
      </div>
    </section>

    <section class="site-section">
      <div class="site-container grid gap-12 lg:grid-cols-2 lg:gap-16">
        <div data-reveal>
          <AppImage
            :photo="contactPhoto"
            ratio="4 / 3"
            class="rounded-2xl"
            sizes="(max-width: 1024px) 100vw, 50vw"
            eager
          />
        </div>

        <div class="space-y-10">
          <div data-reveal>
            <h2 class="site-eyebrow">Visit</h2>
            <address class="mt-3 text-lg not-italic leading-relaxed">
              {{ salon.addressLine1 }}<br />
              {{ salon.addressLine2 }}
            </address>
          </div>

          <div data-reveal>
            <h2 class="site-eyebrow">Opening hours</h2>
            <dl class="mt-3 max-w-xs space-y-2 text-base">
              <div v-for="row in salon.hours" :key="row.days" class="flex justify-between gap-6">
                <dt class="text-ink-muted">{{ row.days }}</dt>
                <dd class="tabular-nums">{{ row.time }}</dd>
              </div>
            </dl>
          </div>

          <div data-reveal>
            <h2 class="site-eyebrow">Reach us</h2>
            <ul class="mt-3 space-y-2 text-lg">
              <li>
                <a
                  :href="'tel:' + salon.phoneHref"
                  class="inline-flex min-h-[44px] items-center gap-3 underline-offset-4 hover:underline"
                >
                  <Phone class="h-5 w-5 text-ink-muted" aria-hidden="true" />
                  {{ salon.phone }}
                </a>
              </li>
              <li>
                <a
                  :href="'mailto:' + salon.email"
                  class="inline-flex min-h-[44px] items-center gap-3 underline-offset-4 hover:underline"
                >
                  <Mail class="h-5 w-5 text-ink-muted" aria-hidden="true" />
                  {{ salon.email }}
                </a>
              </li>
            </ul>
          </div>

          <div data-reveal>
            <router-link to="/book-appointment" class="site-btn-dark">
              Book an appointment
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="site-section bg-ink text-cream">
      <div class="site-container text-center">
        <h2 class="site-h2">Prefer to book yourself?</h2>
        <p class="mx-auto mt-5 max-w-xl text-base leading-relaxed text-cream/75">
          Choose your service, stylist and time online. You pay in store on the day.
        </p>
        <router-link to="/book-appointment" class="site-btn-light mt-10">
          Book your appointment
        </router-link>
      </div>
    </section>
  </PublicLayout>
</template>

<script setup lang="ts">
import { Phone, Mail } from 'lucide-vue-next'
import PublicLayout from '@/components/layout/PublicLayout.vue'
import AppImage from '@/components/public/AppImage.vue'
import SectionHeading from '@/components/public/SectionHeading.vue'
import { contactPhoto } from '@/config/images'
import { salon } from '@/config/site'
</script>
