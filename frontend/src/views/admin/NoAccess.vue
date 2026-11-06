<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="mx-auto max-w-lg py-16 text-center">
      <h1 class="text-2xl font-semibold text-gray-800">No access yet</h1>
      <p class="mt-3 text-sm text-gray-500">
        You're signed in as <strong>{{ session.user.value?.username }}</strong> with the
        <strong>{{ session.user.value?.role }}</strong> role, which doesn't have any permissions yet.
        Ask an administrator to choose what this role can see on the Users &amp; Roles page.
      </p>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { adminMenu } from '@/config/menus'
import { useSession } from '@/composables/useSession'

const session = useSession()
</script>
