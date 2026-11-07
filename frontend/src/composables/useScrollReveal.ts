import { onMounted, onBeforeUnmount } from 'vue'

/**
 * Fades sections in as they scroll into view.
 *
 * Mounted once by PublicLayout; any element on the page opts in with `data-reveal`.
 * One observer for the whole page rather than one per component.
 *
 * Nothing is hidden until this runs, so a visitor with JS disabled still sees the full page.
 * Respects prefers-reduced-motion: the class is never added, and the stylesheet keeps those
 * elements at their final state anyway (belt and braces).
 */
export function useScrollReveal() {
  let observer: IntersectionObserver | null = null
  let mutationObserver: MutationObserver | null = null

  function reveal(el: Element) {
    el.classList.add('is-visible')
    observer?.unobserve(el)
  }

  function observeAll() {
    if (!observer) return
    document.querySelectorAll('[data-reveal]:not(.is-visible)').forEach((el) => observer!.observe(el))
  }

  onMounted(() => {
    const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches

    if (prefersReduced || typeof IntersectionObserver === 'undefined') {
      // Leave everything visible; never add the class that hides it.
      return
    }

    document.documentElement.classList.add('reveal-ready')

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) reveal(entry.target)
        })
      },
      { rootMargin: '0px 0px -8% 0px', threshold: 0.05 },
    )

    observeAll()

    // Views fetch from the API after mount, so watch for content arriving late.
    mutationObserver = new MutationObserver(() => observeAll())
    mutationObserver.observe(document.body, { childList: true, subtree: true })
  })

  onBeforeUnmount(() => {
    observer?.disconnect()
    mutationObserver?.disconnect()
    document.documentElement.classList.remove('reveal-ready')
  })
}
