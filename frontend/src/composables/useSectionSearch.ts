// Section search: filters ONE admin table in the browser, as you type.
//
// It follows the same rules as the global header search (SearchService on the server) so the
// two never disagree about what matches:
//   - trimmed and case-insensitive; blank shows everything
//   - split into words, and EVERY word must appear in the row's searchable fields, in any order
//   - best matches first: whole field > start of field > start of a word > anywhere, and the
//     row's first (primary) field counts double. With no query the table keeps its own order.
//
// The query lives in the URL (?q=), so a global-search result can open a section already
// filtered, and a refresh or the Back button keeps the filter.
//
// Filtering here is safe because the page already holds only rows the server let this user see.
import { computed, ref, watch, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

type Field = string | number | null | undefined

function fieldScore(word: string, field: string): number {
  if (!field) return 0
  if (field === word) return 100
  if (field.startsWith(word)) return 60
  if (field.split(/[\s@._-]+/).some((part) => part.startsWith(word))) return 40
  return field.includes(word) ? 10 : 0
}

export function scoreRow(words: string[], fields: Field[]): number {
  const normalised = fields.map((f) => (f == null ? '' : String(f).trim().toLowerCase()))
  let total = 0
  for (const w of words) {
    let best = 0
    normalised.forEach((f, i) => {
      best = Math.max(best, fieldScore(w, f) * (i === 0 ? 2 : 1))
    })
    if (best === 0) return 0
    total += best
  }
  return total
}

export function useSectionSearch<T>(rows: Ref<T[]>, fieldsOf: (row: T) => Field[]) {
  const route = useRoute()
  const router = useRouter()
  const query = ref(typeof route.query.q === 'string' ? route.query.q : '')

  const words = computed(() => query.value.trim().toLowerCase().split(/\s+/).filter(Boolean))
  const isFiltering = computed(() => words.value.length > 0)

  const results = computed<T[]>(() => {
    if (!isFiltering.value) return rows.value
    return rows.value
      .map((row, index) => ({ row, index, score: scoreRow(words.value, fieldsOf(row)) }))
      .filter((r) => r.score > 0)
      .sort((a, b) => b.score - a.score || a.index - b.index)
      .map((r) => r.row)
  })

  // Keep ?q= in step without adding a history entry per keystroke.
  let timer: ReturnType<typeof setTimeout> | undefined
  watch(query, (q) => {
    clearTimeout(timer)
    timer = setTimeout(() => {
      const next = q.trim()
      if ((route.query.q ?? '') === next) return
      router.replace({ query: { ...route.query, q: next || undefined } })
    }, 300)
  })
  // Arriving from a global-search result while already on this page.
  watch(
    () => route.query.q,
    (q) => {
      const next = typeof q === 'string' ? q : ''
      if (next.trim() !== query.value.trim()) query.value = next
    },
  )

  function reset() {
    query.value = ''
  }

  return { query, results, isFiltering, reset }
}
