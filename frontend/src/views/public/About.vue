<template>
  <PublicLayout>
    <!-- Page header -->
    <section class="site-section pb-0">
      <div class="site-container max-w-3xl">
        <SectionHeading eyebrow="Our story" level="h1">
          A salon built around listening first
          <template #lead>
            We are a small team of stylists who believe a good haircut starts with actually
            listening &mdash; to what you want, and to what will suit you.
          </template>
        </SectionHeading>
      </div>
    </section>

    <!-- Lead image -->
    <section class="px-5 pt-14 sm:px-8">
      <div class="site-container" data-reveal>
        <AppImage
          :photo="aboutPhoto"
          ratio="16 / 9"
          class="rounded-2xl"
          sizes="(max-width: 1152px) 100vw, 1152px"
          eager
        />
      </div>
    </section>

    <!-- Story -->
    <section class="site-section">
      <div class="site-container grid gap-12 lg:grid-cols-2 lg:gap-16">
        <div data-reveal>
          <h2 class="site-h2">No rush, no upselling</h2>
        </div>
        <div class="space-y-5 text-base leading-relaxed text-ink-muted" data-reveal>
          <p>
            Every appointment starts with a short consultation. We talk about what you want, what
            your hair will actually do, and how much time you realistically have in the morning.
            Then we cut for that &mdash; not for the photo on the wall.
          </p>
          <p>
            Booking is deliberately simple. Pick a service and a stylist online, and pay in store
            when you arrive. We do not take card details to hold an appointment, so there is
            nothing stored and nothing to cancel.
          </p>
          <p>
            Your booking reaches us as pending and our team confirms it shortly afterwards, so you
            always know where you stand before you travel.
          </p>
        </div>
      </div>
    </section>

    <!-- Promises -->
    <section class="site-section bg-ink text-cream">
      <div class="site-container">
        <SectionHeading eyebrow="What you can expect" centered on-dark>
          Three things we hold ourselves to
        </SectionHeading>

        <ol class="mt-16 grid gap-10 md:grid-cols-3">
          <li v-for="(point, index) in aboutPoints" :key="point.title" data-reveal>
            <p class="text-sm tabular-nums text-cream/65">0{{ index + 1 }}</p>
            <h3 class="mt-4 text-xl font-medium">{{ point.title }}</h3>
            <p class="mt-3 text-base leading-relaxed text-cream/70">{{ point.body }}</p>
          </li>
        </ol>
      </div>
    </section>

    <!-- CTA -->
    <section class="site-section">
      <div class="site-container text-center">
        <h2 class="site-h2">Come and see us</h2>
        <p class="mx-auto mt-5 max-w-xl text-base leading-relaxed text-ink-muted">
          Pick a time that suits you. If you are not sure which service you need, book a cut and we
          will work it out together in the chair.
        </p>
        <div class="mt-10 flex flex-wrap justify-center gap-4">
          <router-link to="/book-appointment" class="site-btn-dark">
            Book an appointment
          </router-link>
          <router-link to="/stylists" class="site-btn-outline">Meet the team</router-link>
        </div>
      </div>
    </section>
  </PublicLayout>
</template>

<script setup lang="ts">
import PublicLayout from '@/components/layout/PublicLayout.vue'
import AppImage from '@/components/public/AppImage.vue'
import SectionHeading from '@/components/public/SectionHeading.vue'
import { aboutPhoto } from '@/config/images'
import { aboutPoints } from '@/config/site'
</script>
