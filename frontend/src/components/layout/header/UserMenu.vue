<template>
  <div class="relative" ref="dropdownRef">
    <button
      class="flex items-center text-gray-700"
      @click.prevent="toggleDropdown"
    >
      <span
        class="mr-3 flex h-11 w-11 items-center justify-center overflow-hidden rounded-full bg-brand-500 text-sm font-semibold text-white"
      >
        {{ initials }}
      </span>

      <span class="block mr-1 font-medium text-theme-sm">{{ displayName }}</span>

      <ChevronDownIcon :class="{ 'rotate-180': dropdownOpen }" />
    </button>

    <!-- Dropdown Start -->
    <div
      v-if="dropdownOpen"
      class="absolute right-0 mt-[17px] flex w-[260px] flex-col rounded-2xl border border-gray-200 bg-white p-3 shadow-theme-lg"
    >
      <div>
        <span class="block font-medium text-gray-700 text-theme-sm">
          {{ displayName }}
        </span>
        <span class="mt-0.5 block text-theme-xs text-gray-500">
          {{ displayEmail }}
        </span>
      </div>

      <ul
        v-if="profileLink"
        class="flex flex-col gap-1 pt-4 pb-3 border-b border-gray-200"
      >
        <li>
          <router-link
            :to="profileLink"
            @click="closeDropdown"
            class="flex items-center gap-3 px-3 py-2 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 hover:text-gray-700"
          >
            <UserCircleIcon class="text-gray-500 group-hover:text-gray-700" />
            Edit profile
          </router-link>
        </li>
      </ul>

      <button
        @click="signOut"
        class="flex w-full items-center gap-3 px-3 py-2 mt-3 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 hover:text-gray-700"
      >
        <LogoutIcon class="text-gray-500 group-hover:text-gray-700" />
        {{ signOutLabel }}
      </button>
    </div>
    <!-- Dropdown End -->
  </div>
</template>

<script setup lang="ts">
import { UserCircleIcon, ChevronDownIcon, LogoutIcon } from '@/icons'
import { RouterLink, useRouter } from 'vue-router'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useSession } from '@/composables/useSession'

const router = useRouter()
const session = useSession()

const dropdownOpen = ref(false)
const dropdownRef = ref(null)

// Who is signed in comes from the server session (/api/auth/me) - the same answer for every
// role. This used to infer identity from the URL and a localStorage pseudo-session, and showed
// admins "No login required".
const displayName = computed(() => {
  const u = session.user.value
  return u ? `${u.firstName} ${u.lastName}`.trim() || u.username : 'Guest'
})

const displayEmail = computed(() => session.user.value?.email ?? 'Not signed in')

const initials = computed(() => {
  const name = displayName.value.trim()
  if (!name) return '?'
  const parts = name.split(/\s+/)
  return parts.length > 1 ? `${parts[0][0]}${parts[1][0]}`.toUpperCase() : name[0].toUpperCase()
})

const profileLink = computed(() => {
  if (session.isCustomer.value) return '/customer/profile'
  if (session.isStylist.value) return '/stylist/profile'
  return null
})

// Every role signs out the same way. Previously admins saw 'Back to home', which never logged out.
const signOutLabel = computed(() => 'Sign out')

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value
}

const closeDropdown = () => {
  dropdownOpen.value = false
}

async function signOut() {
  closeDropdown()
  // Ends the server-side session and clears the cookie, for any role. Waiting for it means
  // the next page load cannot still see the old login.
  await session.logout()
  router.push('/login')
}

const handleClickOutside = (event: MouseEvent) => {
  if (dropdownRef.value && !(dropdownRef.value as HTMLElement).contains(event.target as Node)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
