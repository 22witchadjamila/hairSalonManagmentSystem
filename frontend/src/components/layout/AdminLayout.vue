<template>
  <div class="min-h-screen xl:flex">
    <app-sidebar :menu-groups="visibleMenu" />
    <Backdrop />
    <div
      class="flex-1 transition-all duration-300 ease-in-out"
      :class="[isExpanded || isHovered ? 'lg:ml-[290px]' : 'lg:ml-[90px]']"
    >
      <app-header />
      <div class="p-4 mx-auto max-w-(--breakpoint-2xl) md:p-6">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
import { useSidebar } from '@/composables/useSidebar'
import { useSession } from '@/composables/useSession'
import Backdrop from './Backdrop.vue'

// Each dashboard page passes its own section's menu (customerMenu/stylistMenu/adminMenu
// from src/config/menus.ts) so the sidebar reflects where you actually are.
const props = defineProps({
  menuGroups: {
    type: Array,
    default: undefined,
  },
})

const { isExpanded, isHovered } = useSidebar()
const session = useSession()

// A custom staff role sees only the admin pages its permissions cover. Admins see everything;
// customer and stylist menus carry no module markers, so they pass through unchanged.
const visibleMenu = props.menuGroups && session.isCustomRole.value
  ? props.menuGroups
      .map((group) => ({
        ...group,
        items: group.items.filter((item) => !item.adminOnly && (!item.module || session.can(item.module))),
      }))
      .filter((group) => group.items.length)
  : props.menuGroups
</script>
