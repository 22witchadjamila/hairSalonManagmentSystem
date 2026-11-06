<template>
  <!-- Filters the table on this page only. The header search looks across every section. -->
  <div class="flex flex-wrap items-center gap-3">
    <div class="relative w-full sm:w-72">
      <label :for="id" class="sr-only">{{ label }}</label>
      <input
        :id="id"
        :value="modelValue"
        type="search"
        :placeholder="placeholder"
        autocomplete="off"
        class="h-10 w-full rounded-lg border border-gray-300 bg-transparent pl-3 pr-16 text-sm text-gray-800 placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10"
        @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        @keydown.esc="emit('update:modelValue', '')"
      />
      <button
        v-if="modelValue"
        type="button"
        class="absolute right-1.5 top-1/2 -translate-y-1/2 rounded-md px-2 py-1 text-xs text-gray-500 hover:bg-gray-100 hover:text-gray-700"
        @click="emit('update:modelValue', '')"
      >
        Clear
      </button>
    </div>
    <p v-if="modelValue.trim()" class="text-xs text-gray-500" role="status" aria-live="polite">
      {{ shown }} of {{ total }} shown
    </p>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue: string
    total: number
    shown: number
    placeholder?: string
    label?: string
    id?: string
  }>(),
  { placeholder: 'Search this list…', label: 'Search this list', id: 'section-search' },
)
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
</script>
