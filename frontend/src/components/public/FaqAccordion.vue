<template>
  <ul class="divide-y divide-ink-line border-y border-ink-line">
    <li v-for="(item, index) in items" :key="item.question">
      <h3>
        <button
          :id="`faq-trigger-${index}`"
          type="button"
          class="flex w-full items-center justify-between gap-6 py-6 text-left"
          :aria-expanded="open === index"
          :aria-controls="`faq-panel-${index}`"
          @click="toggle(index)"
        >
          <span class="text-lg font-medium">{{ item.question }}</span>
          <Plus
            class="h-5 w-5 shrink-0 transition-transform duration-300"
            :class="open === index && 'rotate-45'"
            aria-hidden="true"
          />
        </button>
      </h3>
      <div
        :id="`faq-panel-${index}`"
        role="region"
        :aria-labelledby="`faq-trigger-${index}`"
        :hidden="open !== index"
      >
        <p class="max-w-2xl pb-6 text-base leading-relaxed text-ink-muted">
          {{ item.answer }}
        </p>
      </div>
    </li>
  </ul>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus } from 'lucide-vue-next'

export interface FaqItem {
  question: string
  answer: string
}

defineProps<{ items: FaqItem[] }>()

// First question starts open so the pattern is obvious without a click.
const open = ref<number | null>(0)

function toggle(index: number) {
  open.value = open.value === index ? null : index
}
</script>
