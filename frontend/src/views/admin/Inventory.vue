<template>
  <AdminLayout :menu-groups="adminMenu">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-gray-800">Inventory</h1>
      <button v-if="session.can('INVENTORY', 'CREATE')" @click="showForm ? closeForm() : openCreateForm()" class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
        {{ showForm ? 'Cancel' : '+ Add Product' }}
      </button>
    </div>
    <SectionSearch v-model="searchQuery" class="mt-4" :total="products.length" :shown="shownProducts.length" placeholder="Search name, brand or category…" />


    <form v-if="showForm" @submit.prevent="save" class="mt-4 grid grid-cols-1 gap-3 rounded-lg border border-gray-200 p-4 sm:grid-cols-2 lg:grid-cols-4">
      <input v-model="form.name" placeholder="Name" required :class="inputClass" />
      <input v-model="form.brand" placeholder="Brand" :class="inputClass" />
      <input v-model="form.category" placeholder="Category" :class="inputClass" />
      <div>
        <input
          v-model.number="form.stockQuantity"
          type="number" min="0" placeholder="Stock quantity" required
          :disabled="!!editingId"
          :class="[inputClass, editingId ? 'opacity-50' : '']"
        />
        <p v-if="editingId" class="mt-1 text-xs text-gray-400">Use +10 / -1 on the list to adjust stock instead.</p>
      </div>
      <input v-model.number="form.reorderLevel" type="number" min="0" placeholder="Reorder level" required :class="inputClass" />
      <input v-model.number="form.costPrice" type="number" min="0" step="0.01" placeholder="Cost price (R)" required :class="inputClass" />
      <input v-model.number="form.sellingPrice" type="number" min="0.01" step="0.01" placeholder="Selling price (R)" required :class="inputClass" />
      <button type="submit" :disabled="submitting" class="col-span-full rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50">
        {{ submitting ? 'Saving…' : editingId ? 'Save Changes' : 'Add Product' }}
      </button>
    </form>

    <p v-if="errorMessage" class="mt-3 text-sm text-error-600">{{ errorMessage }}</p>
    <p v-if="loading" class="mt-4 text-sm text-gray-400">Loading&hellip;</p>

    <div v-else class="mt-6 overflow-x-auto rounded-lg border border-gray-200">
      <table class="min-w-full divide-y divide-gray-200 text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Product</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Stock</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500">Selling price</th>
            <th class="px-4 py-3 text-left font-medium text-gray-500" title="Retail products appear on the public Products page. Internal stock stays hidden.">On Products page</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="p in shownProducts" :key="p.productId">
            <td class="px-4 py-3 font-medium text-gray-800">{{ p.name }} <span class="font-normal text-gray-400">{{ p.brand }}</span></td>
            <td class="px-4 py-3">
              <span :class="p.stockQuantity <= p.reorderLevel ? 'text-warning-600' : 'text-gray-600'">
                {{ p.stockQuantity }} (reorder at {{ p.reorderLevel }})
              </span>
            </td>
            <td class="px-4 py-3 text-gray-600">R{{ p.sellingPrice.toFixed(2) }}</td>
            <td class="px-4 py-3">
              <label class="inline-flex cursor-pointer items-center gap-2 text-gray-600">
                <input type="checkbox" :checked="p.forSale" @change="toggleForSale(p)" class="h-4 w-4" />
                {{ p.forSale ? 'Shown to customers' : 'Internal only' }}
              </label>
            </td>
            <td class="px-4 py-3 text-right space-x-3">
              <button v-if="session.can('INVENTORY', 'EDIT')" @click="adjust(p, 10)" class="text-success-600 hover:text-success-700">+10</button>
              <button v-if="session.can('INVENTORY', 'EDIT')" @click="adjust(p, -1)" :disabled="p.stockQuantity < 1" class="text-gray-600 hover:text-gray-800 disabled:opacity-40">-1</button>
              <button v-if="session.can('INVENTORY', 'EDIT')" @click="openEditForm(p)" class="font-medium text-brand-600 hover:text-brand-700">Edit</button>
              <button v-if="session.can('INVENTORY', 'DELETE')" @click="remove(p)" class="text-error-600 hover:text-error-700">Delete</button>
            </td>
          </tr>
          <tr v-if="!shownProducts.length">
            <td colspan="5" class="px-4 py-6 text-center text-gray-400">
              <template v-if="isFiltering">No products match &ldquo;{{ searchQuery.trim() }}&rdquo;. <button type="button" class="ml-1 font-medium text-brand-600 hover:text-brand-700" @click="resetSearch">Clear search</button></template>
              <template v-else>No products yet.</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { adminMenu } from '@/config/menus'
import { productsApi } from '@/api/products'
import { ApiError } from '@/api/client'
import type { ProductInventory } from '@/api/types'
import SectionSearch from '@/components/admin/SectionSearch.vue'
import { useSectionSearch } from '@/composables/useSectionSearch'
import { useSession } from '@/composables/useSession'

const inputClass =
  'h-10 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10'

const products = ref<ProductInventory[]>([])
const session = useSession()
// Section search - this table only. The first field is the primary one and ranks highest.
const { query: searchQuery, results: shownProducts, isFiltering, reset: resetSearch } = useSectionSearch(products, (p) => [
  p.name, p.brand, p.category,
])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const editingId = ref<string | null>(null)
const errorMessage = ref('')

const emptyForm = { name: '', brand: '', category: '', stockQuantity: 0, reorderLevel: 5, costPrice: 0, sellingPrice: 0 }
const form = reactive({ ...emptyForm })

onMounted(load)

async function load() {
  loading.value = true
  try {
    products.value = await productsApi.list()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not load inventory.'
  } finally {
    loading.value = false
  }
}

function openCreateForm() {
  editingId.value = null
  Object.assign(form, emptyForm)
  showForm.value = true
}

function openEditForm(p: ProductInventory) {
  editingId.value = p.productId
  Object.assign(form, {
    name: p.name,
    brand: p.brand ?? '',
    category: p.category ?? '',
    stockQuantity: p.stockQuantity,
    reorderLevel: p.reorderLevel,
    costPrice: p.costPrice,
    sellingPrice: p.sellingPrice,
  })
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
}

async function save() {
  submitting.value = true
  errorMessage.value = ''
  try {
    if (editingId.value) {
      await productsApi.update(editingId.value, { ...form })
    } else {
      await productsApi.create({ ...form })
    }
    closeForm()
    await load()
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not save this product.'
  } finally {
    submitting.value = false
  }
}

/** Show or hide a product on the public Products page. New products start hidden. */
async function toggleForSale(p: ProductInventory) {
  try {
    const updated = await productsApi.setForSale(p.productId, !p.forSale)
    const index = products.value.findIndex((x) => x.productId === updated.productId)
    if (index !== -1) products.value[index] = updated
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not update this product.'
  }
}

async function adjust(p: ProductInventory, delta: number) {
  try {
    const updated = await productsApi.adjustStock(p.productId, delta)
    const index = products.value.findIndex((x) => x.productId === updated.productId)
    if (index !== -1) products.value[index] = updated
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not adjust stock.'
  }
}

async function remove(p: ProductInventory) {
  if (!confirm(`Delete ${p.name}?`)) return
  try {
    await productsApi.delete(p.productId)
    products.value = products.value.filter((x) => x.productId !== p.productId)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : 'Could not delete this product.'
  }
}
</script>
