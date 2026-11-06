<template>
  <div class="site-public flex min-h-dvh flex-col">
    <a
      href="#main"
      class="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-[100] focus:rounded-lg focus:bg-ink focus:px-4 focus:py-3 focus:text-sm focus:text-cream"
    >
      Skip to content
    </a>

    <header class="sticky top-0 z-50 bg-ink text-cream">
      <div class="site-container flex items-center justify-between gap-6 px-5 py-4 sm:px-8">
        <router-link
          to="/"
          class="text-lg font-bold uppercase tracking-[0.22em]"
          @click="menuOpen = false"
        >
          {{ salon.shortName }}
        </router-link>

        <nav aria-label="Main" class="hidden items-center gap-8 lg:flex">
          <router-link
            v-for="link in publicNav"
            :key="link.path"
            :to="link.path"
            class="text-sm text-cream/70 transition-colors hover:text-cream"
            active-class="text-cream"
            :aria-current="route.path === link.path ? 'page' : undefined"
          >
            {{ link.name }}
          </router-link>
        </nav>

        <div class="flex items-center gap-3">
          <a
            :href="`tel:${salon.phoneHref}`"
            class="hidden items-center gap-2 text-sm text-cream md:flex"
          >
            <Phone class="h-4 w-4" aria-hidden="true" />
            <span>{{ salon.phone }}</span>
          </a>

          <router-link to="/book-appointment" class="site-btn-light hidden sm:inline-flex">
            Book Now
          </router-link>

          <button
            type="button"
            class="-mr-2 flex h-11 w-11 items-center justify-center lg:hidden"
            :aria-expanded="menuOpen"
            aria-controls="mobile-nav"
            :aria-label="menuOpen ? 'Close menu' : 'Open menu'"
            @click="menuOpen = !menuOpen"
          >
            <X v-if="menuOpen" class="h-6 w-6" aria-hidden="true" />
            <Menu v-else class="h-6 w-6" aria-hidden="true" />
          </button>
        </div>
      </div>

      <nav
        v-show="menuOpen"
        id="mobile-nav"
        aria-label="Main"
        class="border-t border-white/10 px-5 pb-6 pt-2 sm:px-8 lg:hidden"
      >
        <router-link
          v-for="link in publicNav"
          :key="link.path"
          :to="link.path"
          class="flex min-h-[48px] items-center border-b border-white/5 text-base text-cream/80"
          active-class="text-cream"
          @click="menuOpen = false"
        >
          {{ link.name }}
        </router-link>
        <router-link
          to="/book-appointment"
          class="site-btn-light mt-5 w-full"
          @click="menuOpen = false"
        >
          Book an Appointment
        </router-link>
        <a :href="`tel:${salon.phoneHref}`" class="mt-4 flex items-center gap-2 text-sm text-cream/70">
          <Phone class="h-4 w-4" aria-hidden="true" />
          {{ salon.phone }}
        </a>
      </nav>
    </header>

    <main id="main" class="flex-1">
      <slot></slot>
    </main>

    <footer class="bg-ink text-cream">
      <div class="site-container px-5 py-16 sm:px-8">
        <p class="max-w-3xl text-xl leading-relaxed sm:text-2xl">
          Your hair is confidence, personality, and the first thing people notice. That's why our
          clients trust us with more than just a haircut.
        </p>

        <div class="mt-14 grid gap-10 border-t border-white/10 pt-10 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <h2 class="site-eyebrow text-cream/65">Explore</h2>
            <ul class="mt-4 space-y-3">
              <li v-for="link in publicNav" :key="link.path">
                <router-link
                  :to="link.path"
                  class="inline-flex min-h-[32px] items-center text-sm text-cream/70 transition-colors hover:text-cream"
                >
                  {{ link.name }}
                </router-link>
              </li>
            </ul>
          </div>

          <div>
            <h2 class="site-eyebrow text-cream/65">Visit</h2>
            <address class="mt-4 text-sm not-italic leading-relaxed text-cream/70">
              {{ salon.addressLine1 }}<br />
              {{ salon.addressLine2 }}
            </address>
          </div>

          <div>
            <h2 class="site-eyebrow text-cream/65">Opening hours</h2>
            <dl class="mt-4 space-y-2 text-sm text-cream/70">
              <div v-for="row in salon.hours" :key="row.days" class="flex justify-between gap-4">
                <dt>{{ row.days }}</dt>
                <dd class="tabular-nums">{{ row.time }}</dd>
              </div>
            </dl>
          </div>

          <div>
            <h2 class="site-eyebrow text-cream/65">Contact</h2>
            <ul class="mt-4 space-y-3 text-sm text-cream/70">
              <li>
                <a :href="`tel:${salon.phoneHref}`" class="inline-flex min-h-[32px] items-center transition-colors hover:text-cream">
                  {{ salon.phone }}
                </a>
              </li>
              <li>
                <a :href="`mailto:${salon.email}`" class="inline-flex min-h-[32px] items-center transition-colors hover:text-cream">
                  {{ salon.email }}
                </a>
              </li>
              <li>
                <router-link to="/login" class="inline-flex min-h-[32px] items-center transition-colors hover:text-cream">
                  Staff login
                </router-link>
              </li>
            </ul>
          </div>
        </div>

        <p class="mt-12 border-t border-white/10 pt-8 text-xs text-cream/65">
          &copy; {{ new Date().getFullYear() }} {{ salon.name }}. Photography from Unsplash.
        </p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Menu, X, Phone } from 'lucide-vue-next'
import { publicNav } from '@/config/menus'
import { salon } from '@/config/site'
import { useScrollReveal } from '@/composables/useScrollReveal'

const route = useRoute()
const menuOpen = ref(false)

// Close the mobile menu on navigation, otherwise it stays open over the new page.
watch(() => route.path, () => { menuOpen.value = false })

useScrollReveal()
</script>
