<template>
  <AdminLayout :menu-groups="customerMenu">
    <h1 class="text-2xl font-semibold text-gray-800">Profile</h1>

    <div v-if="!session.isCustomer.value" class="mt-4 rounded-lg border border-gray-200 p-6 text-sm text-gray-500">
      We don't know who you are yet &mdash;
      <router-link to="/login" class="text-brand-500 hover:text-brand-600">log in</router-link>
      to edit your profile.
    </div>

    <form v-else @submit.prevent="save" class="mt-6 max-w-lg space-y-4">
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <label class="mb-1.5 block text-sm font-medium text-gray-700">First name</label>
          <input v-model="form.firstName" type="text" required :class="inputClass" />
        </div>
        <div>
          <label class="mb-1.5 block text-sm font-medium text-gray-700">Last name</label>
          <input v-model="form.lastName" type="text" required :class="inputClass" />
        </div>
      </div>
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700">Email</label>
        <input v-model="form.email" type="email" required :class="inputClass" />
      </div>
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700">Phone number</label>
        <input v-model="form.phoneNumber" type="tel" required :class="inputClass" />
      </div>
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700">Date of birth (optional)</label>
        <input v-model="form.dateOfBirth" type="date" :class="inputClass" />
      </div>

      <p v-if="errorMessage" class="text-sm text-error-600">{{ errorMessage }}</p>
      <p v-if="saved" class="text-sm text-success-600">Saved.</p>

      <button
        type="submit"
        :disabled="saving"
        class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
      >
        {{ saving ? 'Saving…' : 'Save Changes' }}
      </button>
    </form>

    <ChangePasswordForm v-if="session.isAuthenticated.value" />
  </AdminLayout>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import ChangePasswordForm from '@/components/account/ChangePasswordForm.vue'
import { customerMenu } from '@/config/menus'
import { customersApi } from '@/api/customers'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'

const session = useSession()

const inputClass =
  'h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  dateOfBirth: '' as string | null,
})

const saving = ref(false)
const saved = ref(false)
const errorMessage = ref('')

onMounted(() => {
  const customer = session.customer.value
  if (!customer) return
  Object.assign(form, {
    firstName: customer.firstName,
    lastName: customer.lastName,
    email: customer.email,
    phoneNumber: customer.phoneNumber,
    dateOfBirth: customer.dateOfBirth,
  })
})

async function save() {
  const customerId = session.customer.value?.customerId
  if (!customerId) return
  saving.value = true
  saved.value = false
  errorMessage.value = ''
  try {
    const updated = await customersApi.update(customerId, { ...form, dateOfBirth: form.dateOfBirth || null })
    session.setCustomer(updated)
    saved.value = true
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not save your profile.'
  } finally {
    saving.value = false
  }
}
</script>
