<template>
  <PublicLayout>
    <!-- Hero -->
    <section class="relative isolate flex min-h-[86svh] items-end overflow-hidden bg-ink">
      <img
        :src="photoUrl(heroPhoto, 1920)"
        :srcset="photoSrcset(heroPhoto)"
        sizes="100vw"
        :alt="heroPhoto.alt"
        fetchpriority="high"
        decoding="async"
        class="absolute inset-0 -z-10 h-full w-full object-cover"
      />
      <!-- The overlay is what keeps the cream text above 4.5:1 over a busy photo. -->
      <div class="absolute inset-0 -z-10 bg-gradient-to-r from-black/90 from-20% via-black/70 via-65% to-black/30"></div>

      <div class="site-container px-5 pb-20 pt-28 sm:px-8 sm:pb-28">
        <h1
          class="max-w-3xl text-[clamp(2.5rem,7vw,3.75rem)] font-bold leading-[1.1] tracking-[-0.02em] text-cream"
        >
          Judith Hair Salon in Cape Town
        </h1>
        <p class="mt-6 max-w-xl text-lg leading-relaxed text-cream">
          The cut, colour and style that actually works for your hair &mdash; book online in about
          a minute and pay in store when you arrive.
        </p>
        <div class="mt-10 flex flex-wrap gap-4">
          <router-link to="/book-appointment" class="site-btn-light">
            Book your appointment
          </router-link>
          <router-link
            to="/services"
            class="site-btn border border-cream/40 text-cream hover:bg-cream hover:text-ink"
          >
            View price list
          </router-link>
        </div>
      </div>
    </section>

    <!-- About -->
    <section class="site-section">
      <div class="site-container grid items-center gap-12 lg:grid-cols-2 lg:gap-16">
        <div data-reveal>
          <AppImage
            :photo="aboutPhoto"
            ratio="4 / 3"
            class="rounded-2xl"
            sizes="(max-width: 1024px) 100vw, 50vw"
          />
        </div>

        <div>
          <SectionHeading eyebrow="About us">
            A salon built around listening first
          </SectionHeading>

          <dl class="mt-10 space-y-8">
            <div v-for="point in aboutPoints" :key="point.title" data-reveal>
              <dt class="text-lg font-medium">{{ point.title }}</dt>
              <dd class="mt-2 text-base leading-relaxed text-ink-muted">{{ point.body }}</dd>
            </div>
          </dl>

          <router-link to="/about" class="site-btn-outline mt-10" data-reveal>
            Read our story
          </router-link>
        </div>
      </div>
    </section>

    <!--
      Craft band. The photo was shot on a near-white backdrop with the tools rising from the
      bottom, so `mix-blend-multiply` lets that backdrop fall away into the cream page -
      white multiplied by cream is cream - and only the hands and tools remain. No frame,
      no visible photo edge, and the headline sits in the space the shot was composed around.
    -->
    <section class="relative overflow-hidden bg-cream pt-20 md:pt-28">
      <div class="site-container relative z-10 px-5 text-center sm:px-8" data-reveal>
        <p class="site-eyebrow">The craft</p>
        <h2 class="site-h2 mx-auto mt-3 max-w-2xl">Good tools, in hands that know them</h2>
        <p class="mx-auto mt-4 max-w-xl text-base leading-relaxed text-ink-muted">
          Sharp scissors, the right brush, colour mixed properly. The unglamorous part is most
          of why a cut still looks right three weeks later.
        </p>
      </div>

      <!-- -mt pulls the tools up into the headline's airspace so the two read as one image. -->
      <img
        :src="photoUrl(craftPhoto, 1920)"
        :srcset="photoSrcset(craftPhoto)"
        sizes="100vw"
        :alt="craftPhoto.alt"
        loading="lazy"
        decoding="async"
        class="craft-photo mt-10 h-[20rem] w-full object-cover object-bottom sm:h-[24rem] md:h-[30rem]"
      />
    </section>

    <!-- Services (dark band) -->
    <section class="site-section bg-ink text-cream">
      <div class="site-container">
        <SectionHeading eyebrow="What we do" centered on-dark>
          Our services
          <template #lead>
            Expert care, modern styles and treatments that keep hair in good condition between
            visits.
          </template>
        </SectionHeading>

        <p v-if="servicesLoading" class="mt-16 text-center text-sm text-cream/60">
          Loading services&hellip;
        </p>
        <p v-else-if="!services.length" class="mt-16 text-center text-sm text-cream/60">
          Our service list is being updated &mdash; please check back shortly.
        </p>

        <ul v-else class="mt-16 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <li
            v-for="service in services"
            :key="service.serviceId"
            data-reveal
            class="overflow-hidden rounded-2xl bg-white/5 ring-1 ring-white/10"
          >
            <router-link to="/book-appointment" class="block">
              <AppImage
                :photo="serviceCategoryPhoto(service.category)"
                ratio="3 / 2"
                hover-zoom
                sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
              />
              <div class="p-6">
                <h3 class="text-xl font-medium">{{ service.name }}</h3>
                <p
                  v-if="service.description"
                  class="mt-2 line-clamp-2 text-sm leading-relaxed text-cream/60"
                >
                  {{ service.description }}
                </p>
                <p class="mt-5 flex items-baseline justify-between gap-4">
                  <span class="text-lg tabular-nums">R{{ service.price.toFixed(2) }}</span>
                  <span class="text-xs uppercase tracking-[0.14em] text-cream/65">
                    {{ service.durationMinutes }} min
                  </span>
                </p>
              </div>
            </router-link>
          </li>
        </ul>

        <div class="mt-14 text-center">
          <router-link to="/services" class="site-btn-light">See the full price list</router-link>
        </div>
      </div>
    </section>

    <!-- Results: a continuously sliding reel, full-bleed like the reference site -->
    <section class="py-20 md:py-28">
      <div class="site-container px-5 sm:px-8">
        <SectionHeading eyebrow="Our work" centered>Real results on real clients</SectionHeading>
      </div>

      <div class="mt-14">
        <PhotoMarquee :photos="resultPhotos" :duration-seconds="70" ratio="16 / 10" />
      </div>

      <div class="site-container mt-10 px-5 text-center sm:px-8">
        <router-link to="/gallery" class="site-btn-outline">View the gallery</router-link>
      </div>
    </section>

    <!-- Stylists -->
    <section class="site-section bg-cream-dim">
      <div class="site-container">
        <SectionHeading eyebrow="The team" centered>Meet our stylists</SectionHeading>

        <p v-if="stylistsLoading" class="mt-14 text-center text-sm text-ink-muted">
          Loading the team&hellip;
        </p>
        <p v-else-if="!stylists.length" class="mt-14 text-center text-sm text-ink-muted">
          Our team list is being updated &mdash; please check back shortly.
        </p>

        <ul v-else class="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          <li v-for="stylist in stylists" :key="stylist.stylistId" data-reveal>
            <AppImage
              :photo="stylistPhoto(stylist.stylistId)"
              ratio="3 / 4"
              class="rounded-2xl"
              sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
            />
            <h3 class="mt-4 text-lg font-medium">{{ stylist.firstName }} {{ stylist.lastName }}</h3>
            <p class="site-eyebrow mt-1">{{ stylist.speciality }}</p>
          </li>
        </ul>

        <div class="mt-12 text-center">
          <router-link to="/stylists" class="site-btn-outline">Meet the full team</router-link>
        </div>
      </div>
    </section>

    <!-- Reviews -->
    <section v-if="reviews.length" class="site-section">
      <div class="site-container">
        <SectionHeading eyebrow="Reviews" centered>Why clients keep coming back</SectionHeading>

        <p v-if="averageRating" class="mt-8 text-center">
          <span class="text-5xl font-semibold tabular-nums">{{ averageRating }}</span>
          <span class="mt-2 block text-sm text-ink-muted">
            average from {{ reviews.length }} verified
            {{ reviews.length === 1 ? 'review' : 'reviews' }}
          </span>
        </p>

        <ul class="mt-14 grid gap-6 md:grid-cols-3">
          <li
            v-for="review in reviews"
            :key="review.reviewId"
            data-reveal
            class="rounded-2xl border border-ink-line p-8"
          >
            <p class="flex items-center gap-1" :aria-label="'Rated ' + review.rating + ' out of 5'">
              <Star
                v-for="n in 5"
                :key="n"
                class="h-4 w-4"
                :class="n <= review.rating ? 'fill-current' : 'text-ink-line'"
                aria-hidden="true"
              />
            </p>
            <blockquote v-if="review.comment" class="mt-5 text-base leading-relaxed">
              {{ review.comment }}
            </blockquote>
            <p class="mt-5 text-sm text-ink-muted">{{ review.reviewerName }}</p>
          </li>
        </ul>
      </div>
    </section>

    <!-- FAQ -->
    <section class="site-section bg-cream-dim">
      <div class="site-container grid gap-12 lg:grid-cols-[minmax(0,22rem)_1fr] lg:gap-20">
        <SectionHeading eyebrow="Good to know">
          FAQ
          <template #lead>
            Anything we have not covered, just call the salon &mdash; we are happy to talk it
            through before you book.
          </template>
        </SectionHeading>

        <FaqAccordion :items="faqs" />
      </div>
    </section>

    <!-- Closing CTA -->
    <section class="relative isolate overflow-hidden bg-ink">
      <img
        :src="photoUrl(ctaPhoto, 1920)"
        :srcset="photoSrcset(ctaPhoto)"
        sizes="100vw"
        :alt="ctaPhoto.alt"
        loading="lazy"
        decoding="async"
        class="absolute inset-0 -z-10 h-full w-full object-cover opacity-40"
      />
      <div class="site-container site-section text-center">
        <h2 class="site-h2 text-cream">Not sure what suits you best?</h2>
        <p class="mx-auto mt-5 max-w-xl text-base leading-relaxed text-cream/75">
          Book a cut and we will start with a consultation. Bring photos if you have them, and we
          will tell you honestly what will work.
        </p>
        <router-link to="/book-appointment" class="site-btn-light mt-10">
          Book your appointment
        </router-link>
      </div>
    </section>
  </PublicLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Star } from 'lucide-vue-next'
import PublicLayout from '@/components/layout/PublicLayout.vue'
import AppImage from '@/components/public/AppImage.vue'
import SectionHeading from '@/components/public/SectionHeading.vue'
import FaqAccordion from '@/components/public/FaqAccordion.vue'
import PhotoMarquee from '@/components/public/PhotoMarquee.vue'
import { servicesApi } from '@/api/services'
import { stylistsApi, type PublicStylist } from '@/api/stylists'
import { feedbackApi, type PublicReview } from '@/api/feedback'
import type { SalonService } from '@/api/types'
import {
  heroPhoto,
  aboutPhoto,
  craftPhoto,
  ctaPhoto,
  galleryPhotos,
  photoUrl,
  photoSrcset,
  serviceCategoryPhoto,
  stylistPhoto,
} from '@/config/images'
import { aboutPoints, faqs } from '@/config/site'

const services = ref<SalonService[]>([])
const stylists = ref<PublicStylist[]>([])
const reviews = ref<PublicReview[]>([])
const servicesLoading = ref(true)
const stylistsLoading = ref(true)

const resultPhotos = galleryPhotos

const averageRating = computed(() => {
  if (!reviews.value.length) return null
  const total = reviews.value.reduce((sum, review) => sum + review.rating, 0)
  return (total / reviews.value.length).toFixed(1)
})


onMounted(() => {
  // Each block fails on its own: a hiccup loading reviews must not blank the services grid.
  servicesApi
    .listActive()
    .then((data) => {
      services.value = data.slice(0, 6)
    })
    .catch(() => {
      services.value = []
    })
    .finally(() => {
      servicesLoading.value = false
    })

  stylistsApi
    .listActive()
    .then((data) => {
      stylists.value = data.slice(0, 4)
    })
    .catch(() => {
      stylists.value = []
    })
    .finally(() => {
      stylistsLoading.value = false
    })

  // GET /feedback/public: the server already filters to verified reviews with a comment and
  // returns only a first name + initial. The old call fetched every review WITH the reviewer's
  // email and phone number and hid them in the browser - they were still in the response.
  feedbackApi
    .publicReviews()
    .then((data) => {
      reviews.value = data.slice(0, 3)
    })
    .catch(() => {
      reviews.value = []
    })
})
</script>

<style scoped>
.craft-photo {
  /* White backdrop x cream page = cream, so the backdrop disappears instead of
     sitting on the page as a pale rectangle. The brightness nudge cleans up the
     shot's slightly grey studio white so nothing ghosts through. */
  mix-blend-mode: multiply;
  /* Clip the slightly grey studio backdrop to pure white; white x cream = cream, so the
     rectangle vanishes. The tools are dark enough that a 12% lift barely touches them. */
  filter: brightness(1.12) contrast(1.04);
}
</style>
