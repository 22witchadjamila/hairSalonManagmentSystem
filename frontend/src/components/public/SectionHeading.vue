<template>
  <div data-reveal :class="centered ? 'text-center' : 'text-left'">
    <p v-if="eyebrow" class="site-eyebrow" :class="onDark && 'text-cream/60'">
      {{ eyebrow }}
    </p>
    <component
      :is="level"
      class="site-h2 mt-3"
      :class="onDark ? 'text-cream' : 'text-ink'"
    >
      <slot />
    </component>
    <p
      v-if="$slots.lead"
      class="mt-4 max-w-2xl text-base leading-relaxed"
      :class="[onDark ? 'text-cream/70' : 'text-ink-muted', centered && 'mx-auto']"
    >
      <slot name="lead" />
    </p>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    eyebrow?: string
    centered?: boolean
    onDark?: boolean
    /** h1 on the page's own title, h2 for sections - keeps the outline sequential. */
    level?: 'h1' | 'h2'
  }>(),
  { eyebrow: '', centered: false, onDark: false, level: 'h2' },
)
</script>
