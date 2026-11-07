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
          <!-- Form -->
          <div class="flex flex-col justify-center flex-1 w-full max-w-md mx-auto py-8">
            <div class="mb-5 sm:mb-8">
              <h1
                class="mb-2 font-semibold text-gray-800 text-title-sm sm:text-title-md"
              >
                Sign Up
              </h1>
              <p class="text-sm text-gray-500">
                Create an account so we can keep track of your appointments and loyalty points.
              </p>
            </div>
            <div>
              <form @submit.prevent="handleSubmit">
                <div class="space-y-5">
                  <div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
                    <div>
                      <label class="mb-1.5 block text-sm font-medium text-gray-700">
                        First name<span class="text-error-500">*</span>
                      </label>
                      <input v-model="form.firstName" type="text" required :class="inputClass" />
                    </div>
                    <div>
                      <label class="mb-1.5 block text-sm font-medium text-gray-700">
                        Last name<span class="text-error-500">*</span>
                      </label>
                      <input v-model="form.lastName" type="text" required :class="inputClass" />
                    </div>
                  </div>
                  <div>
                    <label class="mb-1.5 block text-sm font-medium text-gray-700">
                      Email<span class="text-error-500">*</span>
                    </label>
                    <input v-model="form.email" type="email" required :class="inputClass" />
                  </div>
                  <div>
                    <label class="mb-1.5 block text-sm font-medium text-gray-700">
                      Phone number<span class="text-error-500">*</span>
                    </label>
                    <input v-model="form.phoneNumber" type="tel" required :class="inputClass" />
                  </div>

                  <div>
                    <label class="mb-1.5 block text-sm font-medium text-gray-700">
                      Username<span class="text-error-500">*</span>
                    </label>
                    <input
                      v-model="form.username"
                      type="text"
                      required
                      autocomplete="username"
                      :class="inputClass"
                    />
                  </div>

                  <div>
                    <label class="mb-1.5 block text-sm font-medium text-gray-700">
                      Password<span class="text-error-500">*</span>
                    </label>
                    <input
                      v-model="form.password"
                      type="password"
                      required
                      minlength="8"
                      autocomplete="new-password"
                      :class="inputClass"
                    />
                    <p class="mt-1.5 text-xs text-gray-500">At least 8 characters, with a letter and a number.</p>
                  </div>

                  <div
                    v-if="errorMessage"
                    class="rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700"
                  >
                    {{ errorMessage }}
                  </div>

                  <div>
                    <button
                      type="submit"
                      :disabled="submitting"
                      class="flex items-center justify-center w-full px-4 py-3 text-sm font-medium text-white transition rounded-lg bg-brand-500 shadow-theme-xs hover:bg-brand-600 disabled:opacity-50"
                    >
                      {{ submitting ? 'Creating your account…' : 'Sign Up' }}
                    </button>
                  </div>
                </div>
              </form>
              <div class="mt-5">
                <p class="text-sm font-normal text-center text-gray-700 sm:text-start">
                  Already have an account?
                  <router-link
                    to="/login"
                    class="text-brand-500 hover:text-brand-600"
                    >Sign In</router-link
                  >
                </p>
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
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import CommonGridShape from '@/components/common/CommonGridShape.vue'
import FullScreenLayout from '@/components/layout/FullScreenLayout.vue'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'

const router = useRouter()
const route = useRoute()
const session = useSession()

const inputClass =
  ' h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  username: '',
  password: '',
})

const submitting = ref(false)
const errorMessage = ref('')

const handleSubmit = async () => {
  errorMessage.value = ''
  submitting.value = true
  try {
    // Creates the login AND the customer record, then signs in - see AuthService.
    await session.register({ ...form })
    // Came here from a guarded page? Go back to it. Otherwise the customer dashboard.
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/customer'
    router.push(redirect)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
