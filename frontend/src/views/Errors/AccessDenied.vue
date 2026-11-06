<template>
  <div class="relative z-1 flex min-h-screen flex-col items-center justify-center overflow-hidden p-6">
    <common-grid-shape />
    <div class="mx-auto w-full max-w-[472px] text-center">
      <h1 class="mb-4 text-title-md font-bold text-gray-800 xl:text-title-2xl">Access denied</h1>
      <p class="text-base text-gray-700 sm:text-lg">
        You have tried to access a protected area of this application where you don't have rights.
      </p>
      <p v-if="session.user.value" class="mt-3 text-sm text-gray-500">
        You're signed in as <strong class="font-medium text-gray-700">{{ session.user.value.username }}</strong>
        ({{ session.user.value.role }}).
      </p>

      <div class="mt-8 flex flex-wrap items-center justify-center gap-3">
        <router-link
          :to="session.isAuthenticated.value ? session.home.value : '/login'"
          class="inline-flex items-center justify-center rounded-lg bg-brand-500 px-5 py-3.5 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
        >
          {{ session.isAuthenticated.value ? 'Go to my dashboard' : 'Sign in' }}
        </router-link>
        <router-link
          to="/"
          class="inline-flex items-center justify-center rounded-lg border border-gray-300 bg-white px-5 py-3.5 text-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50"
        >
          Back to Home Page
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import CommonGridShape from '@/components/common/CommonGridShape.vue'
import { useSession } from '@/composables/useSession'

// The chapter's accessDenied.jsp. The router sends a signed-in person here when they open a page that
// belongs to another role. Hiding the page is only a courtesy: the server refuses the data regardless.
const session = useSession()
</script>
