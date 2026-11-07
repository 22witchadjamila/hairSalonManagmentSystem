<template>
  <!-- Admins and stylists only: GET /api/search is restricted to those roles on the server. -->
  <div v-if="canSearch" ref="root" class="relative hidden lg:block">
    <form role="search" @submit.prevent="openActive">
      <div class="relative">
        <span class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2" aria-hidden="true">
          <svg class="fill-gray-500" width="20" height="20" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
            <path
              fill-rule="evenodd"
              clip-rule="evenodd"
              d="M3.04175 9.37363C3.04175 5.87693 5.87711 3.04199 9.37508 3.04199C12.8731 3.04199 15.7084 5.87693 15.7084 9.37363C15.7084 12.8703 12.8731 15.7053 9.37508 15.7053C5.87711 15.7053 3.04175 12.8703 3.04175 9.37363ZM9.37508 1.54199C5.04902 1.54199 1.54175 5.04817 1.54175 9.37363C1.54175 13.6991 5.04902 17.2053 9.37508 17.2053C11.2674 17.2053 13.003 16.5344 14.357 15.4176L17.177 18.238C17.4699 18.5309 17.9448 18.5309 18.2377 18.238C18.5306 17.9451 18.5306 17.4703 18.2377 17.1774L15.418 14.3573C16.5365 13.0033 17.2084 11.2669 17.2084 9.37363C17.2084 5.04817 13.7011 1.54199 9.37508 1.54199Z"
            />
          </svg>
        </span>
        <label for="global-search" class="sr-only">{{ placeholder }}</label>
        <input
          id="global-search"
          ref="input"
          v-model="query"
          type="search"
          :placeholder="placeholder"
          autocomplete="off"
          role="combobox"
          :aria-expanded="open"
          aria-controls="global-search-results"
          :aria-activedescendant="active >= 0 ? `search-result-${active}` : undefined"
          class="h-11 w-full rounded-lg border border-gray-200 bg-transparent py-2.5 pl-12 pr-14 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 xl:w-[430px]"
          @focus="open = query.trim().length > 0"
          @keydown.down.prevent="move(1)"
          @keydown.up.prevent="move(-1)"
          @keydown.esc="reset"
        />
        <!-- Reset: clears the query and the results in one click (Esc does the same). -->
        <button
          v-if="query"
          type="button"
          class="absolute right-2.5 top-1/2 -translate-y-1/2 rounded-md px-2 py-1 text-xs text-gray-500 hover:bg-gray-100 hover:text-gray-700"
          aria-label="Clear search"
          @click="reset"
        >
          Clear
        </button>
        <kbd
          v-else
          class="pointer-events-none absolute right-2.5 top-1/2 inline-flex -translate-y-1/2 items-center gap-0.5 rounded-lg border border-gray-200 bg-gray-50 px-[7px] py-[4.5px] text-xs -tracking-[0.2px] text-gray-500"
          aria-hidden="true"
        >
          <span>{{ isMac ? '⌘' : 'Ctrl' }}</span><span>K</span>
        </kbd>
      </div>
    </form>

    <div
      v-if="open"
      id="global-search-results"
      role="listbox"
      class="absolute z-50 mt-2 max-h-[420px] w-full overflow-y-auto rounded-lg border border-gray-200 bg-white p-2 shadow-theme-lg xl:w-[430px]"
    >
      <p v-if="tooShort" class="px-3 py-2 text-sm text-gray-500" role="status">
        Type at least {{ MIN_LENGTH }} characters to search.
      </p>
      <p v-else-if="loading" class="px-3 py-2 text-sm text-gray-500" role="status">Searching&hellip;</p>
      <p v-else-if="error" class="px-3 py-2 text-sm text-error-600" role="alert">{{ error }}</p>
      <p v-else-if="!results.length" class="px-3 py-2 text-sm text-gray-500" role="status">
        Nothing matches &ldquo;{{ query.trim() }}&rdquo;. Check the spelling, or try fewer words.
      </p>
      <template v-else>
        <button
          v-for="(r, i) in results"
          :id="`search-result-${i}`"
          :key="r.type + r.id"
          type="button"
          role="option"
          :aria-selected="i === active"
          class="flex w-full items-start gap-3 rounded-md px-3 py-2 text-left"
          :class="i === active ? 'bg-gray-100' : 'hover:bg-gray-50'"
          @mouseenter="active = i"
          @click="go(r)"
        >
          <span class="mt-0.5 shrink-0 rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-gray-500">{{ r.type }}</span>
          <span class="min-w-0">
            <span class="block truncate text-sm font-medium text-gray-800">{{ r.title }}</span>
            <span v-if="r.subtitle" class="block truncate text-xs text-gray-500">{{ r.subtitle }}</span>
          </span>
        </button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { searchApi, type SearchResult } from '@/api/search'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'

/**
 * Header search. This used to be TailAdmin demo markup only: the input had no model or handler,
 * and Enter submitted an empty form that reloaded the page.
 *
 * Results come from GET /api/search, which scopes them by role on the SERVER - an admin searches
 * everything, a stylist only their own appointments and the service list, a custom role only the
 * modules it can view. They arrive ranked best-first.
 *
 * This is the GLOBAL search (across sections). Each result opens its section page with ?q= set,
 * where that page's own search box - the SECTION search, which filters only that table - takes
 * over, so you land on the record rather than on a full list.
 */
const MIN_LENGTH = 2
const DEBOUNCE_MS = 250

const router = useRouter()
const session = useSession()
// Custom staff roles can search too - the server limits results to the modules they can view.
const canSearch = computed(
  () =>
    session.isAdmin.value ||
    session.isStylist.value ||
    (session.isCustomRole.value && (session.user.value?.permissions ?? []).some((p) => p.endsWith(':VIEW'))),
)
const placeholder = computed(() =>
  session.isAdmin.value
    ? 'Search customers, bookings, services…'
    : session.isStylist.value
      ? 'Search your appointments…'
      : 'Search…',
)
const isMac = typeof navigator !== 'undefined' && /Mac|iPhone|iPad/.test(navigator.platform)

const root = ref<HTMLElement | null>(null)
const input = ref<HTMLInputElement | null>(null)
const query = ref('')
const results = ref<SearchResult[]>([])
const loading = ref(false)
const error = ref('')
const open = ref(false)
const active = ref(-1)

let timer: ReturnType<typeof setTimeout> | undefined
let requestId = 0

const tooShort = computed(() => {
  const n = query.value.trim().length
  return n > 0 && n < MIN_LENGTH
})

watch(query, (q) => {
  clearTimeout(timer)
  const term = q.trim()
  if (term.length < MIN_LENGTH) {
    // Blank or 1 character: no request. A single character would match nearly everything.
    requestId++
    loading.value = false
    results.value = []
    open.value = term.length > 0
    return
  }
  open.value = true
  loading.value = true
  timer = setTimeout(() => run(term), DEBOUNCE_MS)
})

async function run(term: string) {
  // Ignore answers to older keystrokes that arrive after newer ones.
  const id = ++requestId
  error.value = ''
  try {
    const found = await searchApi.search(term)
    if (id !== requestId) return
    results.value = found
    active.value = found.length ? 0 : -1
  } catch (err) {
    if (id !== requestId) return
    results.value = []
    error.value = err instanceof ApiError ? err.message : 'Search is unavailable right now.'
  } finally {
    if (id === requestId) loading.value = false
  }
}

function move(step: number) {
  if (!results.value.length) return
  open.value = true
  active.value = (active.value + step + results.value.length) % results.value.length
}

function go(r: SearchResult) {
  close()
  query.value = ''
  router.push(r.link)
}

function openActive() {
  const r = results.value[active.value] ?? results.value[0]
  if (r) go(r)
}

function reset() {
  query.value = ''
  results.value = []
  error.value = ''
  close()
}

function close() {
  open.value = false
  active.value = -1
}

function onKey(e: KeyboardEvent) {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    input.value?.focus()
  }
}

function onClickOutside(e: MouseEvent) {
  if (root.value && !root.value.contains(e.target as Node)) close()
}

onMounted(() => {
  document.addEventListener('keydown', onKey)
  document.addEventListener('mousedown', onClickOutside)
})
onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKey)
  document.removeEventListener('mousedown', onClickOutside)
  clearTimeout(timer)
})
</script>
