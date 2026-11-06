<template>
  <FullScreenLayout>
    <div class="relative p-6 bg-white z-1 sm:p-0">
      <div
        class="relative flex flex-col justify-center w-full h-screen lg:flex-row"
      >
        <div class="flex flex-col flex-1 w-full lg:w-1/2">
          <div class="w-full max-w-md pt-10 mx-auto">
            <router-link
              to="/"
              class="inline-flex items-center text-sm text-gray-500 transition-colors hover:text-gray-700"
            >
              <svg
                class="stroke-current"
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
              >
                <path
                  d="M12.7083 5L7.5 10.2083L12.7083 15.4167"
                  stroke=""
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              Back to home
            </router-link>
          </div>
          <div class="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
            <div>
              <div class="mb-5 sm:mb-8">
                <h1
                  class="mb-2 font-semibold text-gray-800 text-title-sm sm:text-title-md"
                >
                  Log In
                </h1>
                <p class="text-sm text-gray-500">
                  Customers: use the username and password you chose when you signed up.
                  Staff: use the login the salon administrator gave you.
                </p>
              </div>
              <div>
                <form @submit.prevent="handleSubmit">
                  <div class="space-y-5">
                    <!-- Username -->
                    <div>
                      <label for="username" class="mb-1.5 block text-sm font-medium text-gray-700">
                        Username<span class="text-error-500">*</span>
                      </label>
                      <input
                        v-model="username"
                        type="text"
                        id="username"
                        name="username"
                        required
                        autocomplete="username"
                        class=" h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10"
                      />
                    </div>

                    <!-- Password -->
                    <div>
                      <label for="password" class="mb-1.5 block text-sm font-medium text-gray-700">
                        Password<span class="text-error-500">*</span>
                      </label>
                      <input
                        v-model="password"
                        type="password"
                        id="password"
                        name="password"
                        required
                        autocomplete="current-password"
                        class=" h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10"
                      />
                    </div>

                    <label class="flex items-center gap-2 text-sm text-gray-700">
                      <input v-model="rememberMe" type="checkbox" class="h-4 w-4 rounded border-gray-300" />
                      Remember me on this computer
                      <span class="text-xs text-gray-400">(14 days)</span>
                    </label>

                    <div
                      v-if="signedOutNotice && !errorMessage"
                      role="status"
                      class="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-sm text-gray-700"
                    >
                      You were signed out - your session ended, or this account signed in somewhere else.
                      Please sign in again.
                    </div>

                    <div
                      v-if="errorMessage"
                      role="alert"
                      class="rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700"
                    >
                      {{ errorMessage }}
                    </div>

                    <!-- Button -->
                    <div>
                      <button
                        type="submit"
                        :disabled="submitting"
                        class="flex items-center justify-center w-full px-4 py-3 text-sm font-medium text-white transition rounded-lg bg-brand-500 shadow-theme-xs hover:bg-brand-600 disabled:opacity-50"
                      >
                        {{ submitting ? 'Looking up your account…' : 'Continue' }}
                      </button>
                    </div>
                  </div>
                </form>
                <div class="mt-5">
                  <p
                    class="text-sm font-normal text-center text-gray-700 sm:text-start"
                  >
                    Don't have an account?
                    <router-link
                      to="/register"
                      class="text-brand-500 hover:text-brand-600"
                      >Sign Up</router-link
                    >
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div
          class="relative items-center hidden w-full h-full lg:w-1/2 bg-brand-950 lg:grid"
        >
          <div class="flex items-center justify-center z-1">
            <common-grid-shape />
            <div class="flex flex-col items-center max-w-xs">
              <router-link to="/" class="block mb-4">
                <img width="{231}" height="{48}" src="/images/logo/auth-logo.svg" alt="Logo" />
              </router-link>
              <p class="text-center text-gray-400">
                Look good. Feel great.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </FullScreenLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import CommonGridShape from '@/components/common/CommonGridShape.vue'
import FullScreenLayout from '@/components/layout/FullScreenLayout.vue'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'

const router = useRouter()
const route = useRoute()
const session = useSession()

const username = ref('')
const password = ref('')
const rememberMe = ref(false)
// Sent here by the app when the server ended the session (see router/index.ts).
const signedOutNotice = route.query.expired === '1'
const submitting = ref(false)
const errorMessage = ref('')

const handleSubmit = async () => {
  errorMessage.value = ''
  submitting.value = true
  try {
    await session.login(username.value, password.value, rememberMe.value)
    // Back to where the guard stopped them, or else to their own role's dashboard - an admin
    // landing on /customer would just be bounced again.
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : session.home.value
    router.push(redirect)
  } catch (err) {
    // The backend answers 401 with one message for both a wrong password and an unknown
    // username, so this cannot be used to work out which accounts exist.
    errorMessage.value =
      err instanceof ApiError ? err.message : 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
