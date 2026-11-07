<template>
  <div class="relative" ref="dropdownRef">
    <button
      class="relative flex items-center justify-center text-gray-500 transition-colors bg-white border border-gray-200 rounded-full hover:text-dark-900 h-11 w-11 hover:bg-gray-100 hover:text-gray-700"
      @click="toggleDropdown"
    >
      <span
        :class="{ hidden: !hasUnread, flex: hasUnread }"
        class="absolute right-0 top-0.5 z-1 h-2 w-2 rounded-full bg-orange-400"
      >
        <span
          class="absolute inline-flex w-full h-full bg-orange-400 rounded-full opacity-75 -z-1 animate-ping"
        ></span>
      </span>
      <svg
        class="fill-current"
        width="20"
        height="20"
        viewBox="0 0 20 20"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M10.75 2.29248C10.75 1.87827 10.4143 1.54248 10 1.54248C9.58583 1.54248 9.25004 1.87827 9.25004 2.29248V2.83613C6.08266 3.20733 3.62504 5.9004 3.62504 9.16748V14.4591H3.33337C2.91916 14.4591 2.58337 14.7949 2.58337 15.2091C2.58337 15.6234 2.91916 15.9591 3.33337 15.9591H4.37504H15.625H16.6667C17.0809 15.9591 17.4167 15.6234 17.4167 15.2091C17.4167 14.7949 17.0809 14.4591 16.6667 14.4591H16.375V9.16748C16.375 5.9004 13.9174 3.20733 10.75 2.83613V2.29248ZM14.875 14.4591V9.16748C14.875 6.47509 12.6924 4.29248 10 4.29248C7.30765 4.29248 5.12504 6.47509 5.12504 9.16748V14.4591H14.875ZM8.00004 17.7085C8.00004 18.1228 8.33583 18.4585 8.75004 18.4585H11.25C11.6643 18.4585 12 18.1228 12 17.7085C12 17.2943 11.6643 16.9585 11.25 16.9585H8.75004C8.33583 16.9585 8.00004 17.2943 8.00004 17.7085Z"
          fill=""
        />
      </svg>
    </button>

    <!-- Dropdown Start -->
    <div
      v-if="dropdownOpen"
      class="absolute -right-[240px] mt-[17px] flex max-h-[480px] w-[350px] flex-col rounded-2xl border border-gray-200 bg-white p-3 shadow-theme-lg sm:w-[361px] lg:right-0"
    >
      <div
        class="flex items-center justify-between pb-3 mb-3 border-b border-gray-100"
      >
        <h5 class="text-lg font-semibold text-gray-800">Notifications</h5>

        <button @click="closeDropdown" class="text-gray-500">
          <svg
            class="fill-current"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fill-rule="evenodd"
              clip-rule="evenodd"
              d="M6.21967 7.28131C5.92678 6.98841 5.92678 6.51354 6.21967 6.22065C6.51256 5.92775 6.98744 5.92775 7.28033 6.22065L11.999 10.9393L16.7176 6.22078C17.0105 5.92789 17.4854 5.92788 17.7782 6.22078C18.0711 6.51367 18.0711 6.98855 17.7782 7.28144L13.0597 12L17.7782 16.7186C18.0711 17.0115 18.0711 17.4863 17.7782 17.7792C17.4854 18.0721 17.0105 18.0721 16.7176 17.7792L11.999 13.0607L7.28033 17.7794C6.98744 18.0722 6.51256 18.0722 6.21967 17.7794C5.92678 17.4865 5.92678 17.0116 6.21967 16.7187L10.9384 12L6.21967 7.28131Z"
              fill=""
            />
          </svg>
        </button>
      </div>

      <p v-if="!isCustomerSection" class="py-6 text-center text-sm text-gray-400">
        Notifications are only available on the customer dashboard right now.
      </p>
      <p v-else-if="loading" class="py-6 text-center text-sm text-gray-400">Loading&hellip;</p>
      <p v-else-if="!notifications.length" class="py-6 text-center text-sm text-gray-400">
        No notifications yet.
      </p>

      <ul v-else class="flex flex-col overflow-y-auto custom-scrollbar">
        <li v-for="n in notifications" :key="n.notificationId">
          <div
            class="flex flex-col gap-1 rounded-lg border-b border-gray-100 px-4.5 py-3"
          >
            <span class="text-theme-sm text-gray-700">{{ n.message }}</span>
            <span class="text-theme-xs text-gray-400">
              {{ (n.sentAt ?? n.scheduledAt).replace('T', ' ').split('.')[0] }}
            </span>
          </div>
        </li>
      </ul>
    </div>
    <!-- Dropdown End -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useSession } from '@/composables/useSession'
import { notificationsApi } from '@/api/notifications'
import { useRefresh } from '@/composables/useRefresh'
import type { Notification } from '@/api/types'

const route = useRoute()
const session = useSession()

const dropdownOpen = ref(false)
const dropdownRef = ref(null)
const notifications = ref<Notification[]>([])
const loading = ref(false)

const isCustomerSection = computed(
  () => !route.path.startsWith('/admin') && !route.path.startsWith('/stylist'),
)

// "Unread" = newer than the last time this customer opened the bell. Kept in this browser only
// (a convenience, not a record), so a new device simply shows existing messages as unread once.
const seenKey = computed(() => `salon.notifications.seen.${session.customer.value?.customerId ?? ''}`)
const lastSeen = ref('')
function readSeen() {
  try {
    lastSeen.value = localStorage.getItem(seenKey.value) ?? ''
  } catch {
    lastSeen.value = ''
  }
}
const stamp = (n: Notification) => n.sentAt ?? n.scheduledAt
const newest = computed(() => notifications.value.map(stamp).sort().pop() ?? '')
const hasUnread = computed(
  () => isCustomerSection.value && notifications.value.some((n) => stamp(n) > lastSeen.value),
)
function markSeen() {
  if (!newest.value) return
  lastSeen.value = newest.value
  try {
    localStorage.setItem(seenKey.value, newest.value)
  } catch {
    /* private window or blocked storage: the dot just reappears next visit */
  }
}

/** quiet = a background refresh: no "Loading..." flash, and a failed refresh keeps what is shown. */
async function load(quiet = false) {
  const customerId = session.customer.value?.customerId
  if (!isCustomerSection.value || !customerId) {
    notifications.value = []
    return
  }
  if (!quiet) loading.value = true
  try {
    notifications.value = await notificationsApi.byCustomer(customerId)
    // Already looking at the list: whatever just arrived is seen.
    if (dropdownOpen.value) markSeen()
  } catch {
    if (!quiet) notifications.value = []
  } finally {
    loading.value = false
  }
}

// Picks up a confirmation made while the customer has a page open.
useRefresh(() => load(true))

watch(
  () => session.customer.value?.customerId,
  () => {
    readSeen()
    load()
  },
  { immediate: true },
)
watch(() => route.path, () => load(true))

const toggleDropdown = async () => {
  dropdownOpen.value = !dropdownOpen.value
  if (dropdownOpen.value) {
    await load()
    markSeen()
  }
}

const closeDropdown = () => {
  dropdownOpen.value = false
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
