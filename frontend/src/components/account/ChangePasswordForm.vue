<template>
  <section class="mt-10 max-w-lg rounded-xl border border-gray-200 p-5" aria-labelledby="change-pw-title">
    <h2 id="change-pw-title" class="text-sm font-semibold text-gray-800">Change my password</h2>
    <p class="mt-1 text-xs text-gray-500">
      At least {{ minLength }} characters, with a letter and a number (or a passphrase of 16 or more).
      Changing it signs out every other device.
    </p>

    <form class="mt-4 space-y-3" @submit.prevent="submit">
      <div>
        <label for="pw-current" class="mb-1 block text-xs text-gray-500">Current password</label>
        <input id="pw-current" v-model="current" type="password" autocomplete="current-password" required :class="inputClass" />
      </div>
      <div>
        <label for="pw-new" class="mb-1 block text-xs text-gray-500">New password</label>
        <input id="pw-new" v-model="next" type="password" autocomplete="new-password" required :class="inputClass" />
      </div>
      <div>
        <label for="pw-confirm" class="mb-1 block text-xs text-gray-500">Type the new password again</label>
        <input id="pw-confirm" v-model="confirm" type="password" autocomplete="new-password" required :class="inputClass" />
      </div>

      <p v-if="error" role="alert" class="text-sm text-error-600">{{ error }}</p>
      <p v-if="done" role="status" class="text-sm text-success-700">
        Password changed. Any other device you were signed in on has been signed out.
      </p>

      <button
        type="submit"
        :disabled="submitting"
        class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
      >
        {{ submitting ? 'Changing…' : 'Change password' }}
      </button>
    </form>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'

const session = useSession()
// Same minimums as the server (AuthService): customers 8, everyone who works at the salon 12.
const minLength = computed(() => (session.isCustomer.value ? 8 : 12))

const inputClass =
  'h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

const current = ref('')
const next = ref('')
const confirm = ref('')
const submitting = ref(false)
const error = ref('')
const done = ref(false)

/** Quick checks so obvious mistakes show at once; the server applies the full policy and has the last word. */
function check(): string {
  if (next.value !== confirm.value) return "The two new passwords don't match."
  if (next.value === current.value) return 'Choose a password that is different from your current one.'
  if (next.value.length < minLength.value) return `Password must be at least ${minLength.value} characters.`
  const mixed = /[A-Za-z]/.test(next.value) && /[0-9]/.test(next.value)
  if (!mixed && next.value.length < 16) return 'Password must include a letter and a number (or be 16+ characters long).'
  return ''
}

async function submit() {
  done.value = false
  error.value = check()
  if (error.value) return
  submitting.value = true
  try {
    await authApi.changePassword(current.value, next.value)
    current.value = next.value = confirm.value = ''
    done.value = true
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Could not change your password.'
  } finally {
    submitting.value = false
  }
}
</script>
