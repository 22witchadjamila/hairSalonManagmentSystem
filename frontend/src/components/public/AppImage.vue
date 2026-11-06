<template>
  <div class="overflow-hidden bg-cream-dim" :style="{ aspectRatio: ratio }">
    <img
      :src="photoUrl(photo, width)"
      :srcset="photoSrcset(photo, widths)"
      :sizes="sizes"
      :alt="decorative ? '' : photo.alt"
      :loading="eager ? 'eager' : 'lazy'"
      :fetchpriority="eager ? 'high' : 'auto'"
      decoding="async"
      class="h-full w-full object-cover transition-[opacity,transform] duration-700 ease-out"
      :class="[loaded ? 'opacity-100' : 'opacity-0', hoverZoom && 'hover:scale-[1.04]']"
      @load="loaded = true"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { photoUrl, photoSrcset, type SitePhoto } from '@/config/images'

/**
 * Responsive Unsplash image.
 *
 * The wrapper holds the aspect ratio so the slot is reserved before the file arrives -
 * that's what keeps the page from jumping as photos load (CLS).
 */
withDefaults(
  defineProps<{
    photo: SitePhoto
    /** CSS aspect-ratio for the reserved box, e.g. '4 / 5'. */
    ratio?: string
    /** Fallback width for browsers without srcset support. */
    width?: number
    widths?: number[]
    sizes?: string
    /** Set on the hero only - everything else lazy-loads. */
    eager?: boolean
    hoverZoom?: boolean
    /** Duplicate of an image already on the page (e.g. a marquee's second pass): empty alt. */
    decorative?: boolean
  }>(),
  {
    ratio: '4 / 3',
    width: 1280,
    widths: () => [640, 960, 1280, 1920],
    sizes: '(max-width: 768px) 100vw, 50vw',
    eager: false,
    hoverZoom: false,
    decorative: false,
  },
)

const loaded = ref(false)
</script>
