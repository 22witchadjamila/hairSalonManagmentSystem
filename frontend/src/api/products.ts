import { api } from './client'
import type { ProductInventory } from './types'

export interface ProductRequest {
  name: string
  brand?: string | null
  category?: string | null
  stockQuantity: number
  reorderLevel: number
  costPrice: number
  sellingPrice: number
  serviceId?: string | null
}

export interface ShopProduct {
  productId: string
  name: string
  brand: string | null
  category: string | null
  price: number | null
  availability: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK'
}

export const productsApi = {
  list: () => api.get<ProductInventory[]>('/products'),
  lowStock: () => api.get<ProductInventory[]>('/products/low-stock'),
  create: (payload: ProductRequest) => api.post<ProductInventory>('/products', payload),
  update: (id: string, payload: ProductRequest) => api.put<ProductInventory>(`/products/${id}`, payload),
  adjustStock: (id: string, delta: number) =>
    api.post<ProductInventory>(`/products/${id}/adjust-stock`, { delta }),
  delete: (id: string) => api.delete<void>(`/products/${id}`),
  setForSale: (id: string, forSale: boolean) =>
    api.post<ProductInventory>(`/products/${id}/for-sale`, { forSale }),
  /** Public: retail products only, no cost prices or stock counts. */
  shop: () => api.get<ShopProduct[]>('/products/shop'),
}
