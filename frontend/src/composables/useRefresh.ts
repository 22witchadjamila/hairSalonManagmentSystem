// Runs a refresh function on an interval while the tab is visible, and again whenever the tab
// regains focus - so a page left open (a customer's dashboard, say) picks up changes made
// elsewhere, like staff confirming a booking, without a manual reload. Stops on unmount.
import { onMounted, onBeforeUnmount } from 'vue'

export function useRefresh(refresh: () => unknown, everyMs = 30_000) {
  let timer: ReturnType<typeof setInterval> | undefined

  function whenVisible() {
    if (document.visibilityState === 'visible') refresh()
  }

  onMounted(() => {
    timer = setInterval(whenVisible, everyMs)
    document.addEventListener('visibilitychange', whenVisible)
    window.addEventListener('focus', whenVisible)
  })
  onBeforeUnmount(() => {
    clearInterval(timer)
    document.removeEventListener('visibilitychange', whenVisible)
    window.removeEventListener('focus', whenVisible)
  })
}
