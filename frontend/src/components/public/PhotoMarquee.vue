<template>
  <div class="relative">
    <!--
      Seamless loop: the same photos are rendered twice and the track slides exactly
      -50% of its own width. At that point copy 2 sits precisely where copy 1 started,
      so the jump back to 0 is invisible. Each slide carries its gap as margin-right
      rather than using `gap`, otherwise -50% would not line up.
    -->
    <div class="overflow-hidden" @mouseenter="hovering = true" @mouseleave="hovering = false">
      <ul
        class="marquee-track flex w-max"
        :class="{ 'is-paused': !running }"
        :style="{ animationDuration: durationSeconds + 's' }"
      >
        <li
          v-for="slide in slides"
          :key="slide.key"
          class="mr-4 shrink-0 basis-[78vw] sm:basis-[52vw] lg:basis-[34rem]"
          :aria-hidden="slide.duplicate ? 'true' : undefined"
        >
          <AppImage
            :photo="slide.photo"
            :ratio="ratio"
            :decorative="slide.duplicate"
            class="rounded-2xl"
            sizes="(max-width: 640px) 78vw, (max-width: 1024px) 52vw, 34rem"
          />
        </li>
      </ul>
    </div>

    <!--
      WCAG 2.2.2: motion that starts on its own and runs past 5s needs a way to stop it.
      Hover pauses too, but hover is not available to every visitor, so this is the real control.
    -->
    <div class="site-container mt-6 px-5 sm:px-8">
      <button
        type="button"
        class="inline-flex min-h-[44px] items-center gap-2 text-xs font-semibold uppercase tracking-[0.14em] text-ink-muted transition-colors hover:text-ink"
        @click="paused = !paused"
      >
        <Pause v-if="running" class="h-4 w-4" aria-hidden="true" />
        <Play v-else class="h-4 w-4" aria-hidden="true" />
        {{ running ? 'Pause' : 'Play' }}
        <span class="sr-only">the photo reel</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Pause, Play } from 'lucide-vue-next'
import AppImage from './AppImage.vue'
import type { SitePhoto } from '@/config/images'

const props = withDefaults(
  defineProps<{
    photos: SitePhoto[]
    /** Seconds for one full pass. Longer = slower, calmer. */
    durationSeconds?: number
    ratio?: string
  }>(),
  { durationSeconds: 60, ratio: '16 / 10' },
)

const paused = ref(false)
const hovering = ref(false)
const reducedMotion = ref(false)

const running = computed(() => !paused.value && !hovering.value && !reducedMotion.value)

/** Two passes of the same list; the second is hidden from screen readers. */
const slides = computed(() =>
  [0, 1].flatMap((copy) =>
    props.photos.map((photo, index) => ({
      key: `${copy}-${index}-${photo.id}`,
      photo,
      duplicate: copy === 1,
    })),
  ),
)

let media: MediaQueryList | null = null
function syncMotionPreference(event: MediaQueryList | MediaQueryListEvent) {
  reducedMotion.value = event.matches
}

onMounted(() => {
  media = window.matchMedia('(prefers-reduced-motion: reduce)')
  syncMotionPreference(media)
  media.addEventListener('change', syncMotionPreference)
})

onBeforeUnmount(() => media?.removeEventListener('change', syncMotionPreference))
</script>

<style scoped>
.marquee-track {
  animation: marquee linear infinite;
}

/* Hover, the explicit control and reduced motion all land on the same switch. */
.marquee-track.is-paused {
  animation-play-state: paused;
}

@keyframes marquee {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-50%);
  }
}

@media (prefers-reduced-motion: reduce) {
  .marquee-track {
    animation: none;
  }
}
</style>
