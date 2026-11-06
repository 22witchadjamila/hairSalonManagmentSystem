<template>
  <AdminLayout :menu-groups="stylistMenu">
    <h1 class="text-2xl font-semibold text-gray-800">Profile</h1>

    <!-- Identity comes from the server session. A stylist login without a linked stylist
         record (e.g. the record was removed) is the only way to land here. -->
    <p v-if="!session.stylist.value" class="mt-4 text-sm text-gray-500">
      This login isn't linked to a stylist record. Ask the salon administrator to check your
      email address on the Stylists page.
    </p>

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
        <label class="mb-1.5 block text-sm font-medium text-gray-700">Speciality</label>
        <input v-model="form.speciality" type="text" required :class="inputClass" />
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
import { reactive, ref, onMounted, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import ChangePasswordForm from '@/components/account/ChangePasswordForm.vue'
import { stylistMenu } from '@/config/menus'
import { stylistsApi } from '@/api/stylists'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import type { Stylist } from '@/api/types'

const session = useSession()

const inputClass =
  'h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'


const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  speciality: '',
})

const saving = ref(false)
const saved = ref(false)
const errorMessage = ref('')

function fillForm() {
  const stylist = session.stylist.value
  if (!stylist) return
  Object.assign(form, {
    firstName: stylist.firstName,
    lastName: stylist.lastName,
    email: stylist.email,
    phoneNumber: stylist.phoneNumber,
    speciality: stylist.speciality,
  })
}

onMounted(async () => {
  fillForm()
})

watch(() => session.stylist.value?.stylistId, fillForm)

async function save() {
  const stylistId = session.stylist.value?.stylistId
  if (!stylistId) return
  saving.value = true
  saved.value = false
  errorMessage.value = ''
  try {
    const updated = await stylistsApi.update(stylistId, { ...form })
    // Re-read the session so the header shows the new name straight away.
    await session.refresh()
    saved.value = true
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not save your profile.'
  } finally {
    saving.value = false
  }
}
</script>
