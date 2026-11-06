<template>
  <PublicLayout>
    <section class="site-section pb-0">
      <div class="site-container max-w-3xl">
        <SectionHeading eyebrow="Our work" level="h1">
          Real results on real clients
          <template #lead>
            Cuts, colour and styling from the salon floor. Bring a photo of anything here to your
            appointment and we will tell you honestly how it will work on your hair.
          </template>
        </SectionHeading>
      </div>
    </section>

    <section class="site-section">
      <div class="site-container">
        <!--
          Masonry-ish grid: the first tile spans two columns on larger screens so the grid does
          not read as a plain uniform matrix. Aspect ratios are fixed per tile, so nothing shifts
          as the photos arrive.
        -->
        <ul class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <li
            v-for="(photo, index) in galleryPhotos"
            :key="photo.id"
            data-reveal
            :class="index === 0 && 'sm:col-span-2'"
          >
            <AppImage
              :photo="photo"
              :ratio="index === 0 ? '16 / 10' : '4 / 5'"
              :eager="index === 0"
              hover-zoom
              class="rounded-2xl"
              sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
            />
          </li>
        </ul>

        <p class="mt-12 text-center text-sm text-ink-muted">
          Photography from Unsplash while we build up our own portfolio.
        </p>
      </div>
    </section>

    <section class="site-section bg-ink text-cream">
      <div class="site-container text-center">
        <h2 class="site-h2">Want something like this?</h2>
        <p class="mx-auto mt-5 max-w-xl text-base leading-relaxed text-cream/75">
          Book a consultation and a cut in one appointment. We will talk through what is achievable
          on your hair before anything happens.
        </p>
        <router-link to="/book-appointment" class="site-btn-light mt-10">
          Book your appointment
        </router-link>
      </div>
    </section>
  </PublicLayout>
</template>

<script setup lang="ts">
import PublicLayout from '@/components/layout/PublicLayout.vue'
import AppImage from '@/components/public/AppImage.vue'
import SectionHeading from '@/components/public/SectionHeading.vue'
import { galleryPhotos } from '@/config/images'
</script>
