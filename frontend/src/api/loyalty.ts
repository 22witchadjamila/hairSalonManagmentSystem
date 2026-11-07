import { api } from './client'
import type { LoyaltyReward } from './types'

/** The programme's numbers, read from the server so the pages never carry a second copy. */
export interface LoyaltyRules {
  /** Rand spent for each point earned. */
  randSpentPerPoint: number
  /** Rand taken off a bill by each point spent. */
  randPerPoint: number
  minRedeemPoints: number
  silverFrom: number
  goldFrom: number
  platinumFrom: number
}

export const loyaltyApi = {
  // Backend getOrCreate semantics: always returns a reward, creating a fresh BRONZE one
  // on first call for a customer.
  getOrCreate: (customerId: string) => api.get<LoyaltyReward>(`/loyalty-rewards/customer/${customerId}`),
  rules: () => api.get<LoyaltyRules>('/loyalty-rewards/rules'),
  /** Staff: every customer's account. */
  list: () => api.get<LoyaltyReward[]>('/loyalty-rewards'),
  /**
   * Staff correction: award points for an amount spent outside the app. Payments recorded in the
   * app award their own points, so don't use this for those.
   */
  addPoints: (customerId: string, amountSpent: number) =>
    api.post<LoyaltyReward>(`/loyalty-rewards/customer/${customerId}/add-points`, { amountSpent }),
  // There is no customer-side "redeem": points are spent when staff record the payment, so a
  // redemption is always tied to a bill and can't be used twice.
}
